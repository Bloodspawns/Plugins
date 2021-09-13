package net.runelite.client.plugins.blexternalmanager;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.blexternalmanager.beans.Artifact;
import net.runelite.client.plugins.blexternalmanager.beans.Manifest;
import net.runelite.client.plugins.blexternalmanager.beans.Settings;
import net.runelite.client.plugins.blexternalmanager.swing.ExternalHubPanel;
import net.runelite.client.plugins.blexternalmanager.swing.components.InstalledArtifact;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.VerificationException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@PluginDescriptor(
	name = "BlueLite External Manager",
	hidden = true // set to false to manually refresh externals from settings file by starting the plugin. useful until you have a good externals panel to control this.
)
@Slf4j
@Singleton
public class BLExternalManager extends Plugin
{
	@Inject
	private PluginManager pluginManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private ClientToolbar clientToolbar;

	private NavigationButton navButton;

	private static final Gson GSON = new Gson();
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.DAYS, new LinkedBlockingQueue<>());

	private static final File PLUGIN_DIR = new File(RUNELITE_DIR, "bpluginrepository");
	public static final File SETTINGS_FILE;
	private static final ArrayListMultimap<InstalledArtifact, Plugin> pluginsInFlight = ArrayListMultimap.create();
	private static final PluginClassLoader PLUGIN_CLASS_LOADER = new PluginClassLoader(new URL[]{});

	static
	{
		PLUGIN_DIR.mkdirs();
		// we dont want to use the runelite config since logged in ppl will have this stored on rl servers where rl ppl can simply follow the urls to our repos or even fuck with the config.
		SETTINGS_FILE = new File(PLUGIN_DIR, "settings.json");
		createSettingsFile(false);
	}

	public static Set<InstalledArtifact> getInstalledArtifacts()
	{
		return pluginsInFlight.keySet();
	}

	@Override
	protected void startUp()
	{
		// async to not hang the client
		executor.execute(() ->
		{
			Settings settings = readSettings();
			if (settings == null || settings.getInstalledPlugins() == null)
			{
				createSettingsFile(true);
				settings = new Settings(new HashMap<>());
			}

			ArrayList<InstalledArtifact> installedArtifacts = fromSettings(settings);

			clean(installedArtifacts);

			installPlugins(installedArtifacts.toArray(new InstalledArtifact[0]));
			log.debug("Installed plugins {}", Arrays.toString(pluginsInFlight.keySet().stream().map(i -> i.getArtifact().getName()).toArray()));

			SwingUtilities.invokeLater(this::initializePanel);
		});
	}

	private void initializePanel()
	{
		ExternalHubPanel panel = injector.getInstance(ExternalHubPanel.class);

		BufferedImage icon = ImageUtil.loadImageResource(BLExternalManager.class, "icon.png");
		navButton = NavigationButton.builder()
			.tooltip("BlueLite External Manager")
			.icon(icon)
			.priority(1)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
	}

	public static void createSettingsFile(boolean overwrite)
	{
		if (overwrite || !SETTINGS_FILE.exists())
		{
			log.debug("Creating bl external settings file");
			writeSettingsMap(new HashMap<>());
		}
	}

	public void installPlugins(InstalledArtifact[] _artifacts)
	{
		ArrayList<InstalledArtifact> artifacts = new ArrayList<>();
		for (InstalledArtifact artifact : _artifacts)
		{
			if (!pluginsInFlight.containsKey(artifact))
			{
				artifacts.add(artifact);
			}
		}

		ArrayList<File> files = new ArrayList<>();
		for (InstalledArtifact artifact : artifacts)
		{
			File file = download(artifact);
			if (file != null)
			{
				files.add(file);
			}
		}

		ArrayListMultimap<InstalledArtifact, Class<?>> classes = ArrayListMultimap.create();
		HashSet<Class<?>> pluginManagerLoadedClass = new HashSet<>();
		pluginManager.getPlugins().forEach(p -> pluginManagerLoadedClass.add(p.getClass()));

		for (File file : files)
		{
			try
			{
				PLUGIN_CLASS_LOADER.addURL(file.toURI().toURL());
			}
			catch (MalformedURLException ignored)
			{
			}
		}

		for (InstalledArtifact artifact : artifacts)
		{
			try
			{
				for (String plugin : artifact.getArtifact().getPlugins())
				{
					Class<?> clazz = PLUGIN_CLASS_LOADER.loadClass(plugin);
					if (clazz.getSuperclass() == Plugin.class)
					{
						if (!pluginManagerLoadedClass.contains(clazz))
						{
							classes.put(artifact, clazz);
						}
						else
						{
							log.debug("{} is already loaded", clazz.getName());
						}
					}
				}
			}
			catch (ClassNotFoundException cnf)
			{
				log.debug("Unable to load class", cnf);
			}
		}

		for (InstalledArtifact artifact : classes.keySet())
		{
			try
			{
				SwingUtilities.invokeAndWait(() ->
				{
					try
					{
						List<Plugin> plugins = pluginManager.loadPlugins(List.copyOf(classes.get(artifact)), null);
						pluginManager.loadDefaultPluginConfiguration(plugins);
						for (Plugin plugin : plugins)
						{
							pluginManager.startPlugin(plugin);
							pluginsInFlight.put(artifact, plugin);
						}
						// hack to make the config panel update
						eventBus.post(new ExternalPluginsChanged(new ArrayList<>()));
					}
					catch (PluginInstantiationException e)
					{
						log.warn("Unable to load or start plugins", e);
					}
				});
			}
			catch (InterruptedException | InvocationTargetException e)
			{
				log.warn("Unable to start plugin for artifact \"{}\"", artifact.getArtifact().getName(), e);
			}
		}
	}

	public boolean installPlugin(InstalledArtifact artifact)
	{
		if (pluginsInFlight.containsKey(artifact))
		{
			log.debug("{} is already installed", artifact.getArtifact().getName());
			return true;
		}

		File file = download(artifact);
		if (file == null)
		{
			return false;
		}

		ArrayList<Class<?>> classes = new ArrayList<>();
		HashSet<Class<?>> pluginManagerLoadedClass = new HashSet<>();
		pluginManager.getPlugins().forEach(p -> pluginManagerLoadedClass.add(p.getClass()));
		try
		{
			PLUGIN_CLASS_LOADER.addURL(file.toURI().toURL());
			for (String plugin : artifact.getArtifact().getPlugins())
			{
				if (Strings.isNullOrEmpty(plugin))
				{
					continue;
				}
				Class<?> clazz = PLUGIN_CLASS_LOADER.loadClass(plugin);
				if (clazz.getSuperclass() == Plugin.class)
				{
					if (!pluginManagerLoadedClass.contains(clazz))
					{
						classes.add(clazz);
					}
					else
					{
						log.debug("{} is already loaded", clazz.getName());
					}
				}
			}
		}
		catch (ClassNotFoundException | IOException ignored)
		{
			log.debug("Unable to load class", ignored);
			return false;
		}

		try
		{
			SwingUtilities.invokeAndWait(() ->
			{
				try
				{
					List<Plugin> plugins = pluginManager.loadPlugins(List.copyOf(classes), null);
					pluginManager.loadDefaultPluginConfiguration(plugins);
					for (Plugin plugin : plugins)
					{
						pluginManager.startPlugin(plugin);
						pluginsInFlight.put(artifact, plugin);
					}
					// hack to make the config panel update
					eventBus.post(new ExternalPluginsChanged(new ArrayList<>()));
				}
				catch (PluginInstantiationException e)
				{
					log.warn("Unable to load or start plugins", e);
				}
			});
		}
		catch (InterruptedException | InvocationTargetException e)
		{
			log.warn("Unable to start plugin for artifact \"{}\"", artifact.getArtifact().getName(), e);
			return false;
		}

		return true;
	}

	public boolean uninstallPlugin(InstalledArtifact artifact)
	{
		Collection<Plugin> pluginsToUnload = pluginsInFlight.get(artifact);
		if (pluginsToUnload == null)
		{
			return false;
		}
		for (Plugin plugin : pluginsToUnload)
		{
			try
			{
				SwingUtilities.invokeAndWait(() ->
				{
					try
					{
						pluginManager.stopPlugin(plugin);
						pluginManager.remove(plugin);
						if (plugin.getClass().getClassLoader() instanceof URLClassLoader)
						{
							try
							{
								((URLClassLoader) plugin.getClass().getClassLoader()).close();
							}
							catch (IOException e)
							{
								log.warn("Couldn't close classloader for {}", plugin.getName());
							}
						}
						// hack to make the config panel update
						eventBus.post(new ExternalPluginsChanged(new ArrayList<>()));
					}
					catch (PluginInstantiationException e)
					{
						log.warn("Couldn't stop plugin: {}", plugin.getName());
					}
				});
			}
			catch (InterruptedException | InvocationTargetException e)
			{
				log.warn("Unable to stop external plugin \"{}\"", plugin.getClass().getName(), e);
				return false;
			}
		}

		pluginsInFlight.removeAll(artifact);
		return true;
	}

	public ArrayList<InstalledArtifact> fromSettings(Settings settings)
	{
		ArrayList<InstalledArtifact> artifacts = new ArrayList<>();
		for (String manifestUrl : settings.getInstalledPlugins().keySet())
		{
			HashSet<String> fileName = new HashSet<>(settings.getInstalledPlugins().get(manifestUrl));
			ArrayList<Artifact> _artifacts = new ArrayList<>();
			try
			{
				Manifest manifest = Manifest.getManifest(manifestUrl);
				if (manifest != null)
				{
					Collections.addAll(_artifacts, manifest.getArtifacts());
				}
			}
			catch (IOException e)
			{
				log.warn("Failed to fetch manifest {}", manifestUrl);
			}
			_artifacts.stream().filter(a -> fileName.contains(a.getName()) && a.isValid()).forEach(artifact -> artifacts.add(new InstalledArtifact(manifestUrl, artifact)));
		}

		return artifacts;
	}

	// calculate the hash of a file
	private static String hash(File file) throws IOException
	{
		HashFunction sha256 = Hashing.sha256();
		return Files.asByteSource(file).hash(sha256).toString();
	}

	// calculate the hash of a string
	private static String hash(String string) throws IOException
	{
		HashFunction sha256 = Hashing.sha256();
		return ByteSource.wrap(string.getBytes()).hash(sha256).toString();
	}

	// download a file and check if it matches the given hash
	private static byte[] download(String path, String hash) throws IOException, VerificationException
	{
		HashFunction hashFunction = Hashing.sha256();
		Hasher hasher = hashFunction.newHasher();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.getResponseCode();

		InputStream err = conn.getErrorStream();
		if (err != null)
		{
			err.close();
			throw new IOException("Unable to download " + path + " - " + conn.getResponseMessage());
		}

		try (InputStream in = conn.getInputStream())
		{
			int i;
			byte[] buffer = new byte[1024 * 1024];
			while ((i = in.read(buffer)) != -1)
			{
				byteArrayOutputStream.write(buffer, 0, i);
				hasher.putBytes(buffer, 0, i);
			}
		}

		HashCode hashCode = hasher.hash();
		if (!hash.equals(hashCode.toString()))
		{
			throw new VerificationException("Unable to verify resource " + path + " - expected " + hash + " got " + hashCode.toString());
		}

		return byteArrayOutputStream.toByteArray();
	}

	private static File download(InstalledArtifact installedArtifact)
	{
		Artifact artifact = installedArtifact.getArtifact();
		String dirName = "";
		try
		{
			dirName = hash(installedArtifact.getManifestUrl());
		}
		catch (IOException ignored)
		{
		}
		File dir = new File(PLUGIN_DIR, dirName);
		if (!dir.exists())
		{
			dir.mkdir();
		}

		File dest = new File(dir, artifact.getName());

		String hash;
		try
		{
			hash = hash(dest);
		}
		catch (IOException ex)
		{
			hash = null;
		}

		if (Objects.equals(hash, artifact.getHash()))
		{
			log.debug("Hash for {} up to date", artifact.getName());
			return dest;
		}

		if (dest.exists())
		{
			boolean locked = !dest.delete();
			if (locked)
			{
				return dest;
			}
		}

		try
		{
			final byte[] jar = download(installedArtifact.getArtifact().getPath(), installedArtifact.getArtifact().getHash());
			try (FileOutputStream fout = new FileOutputStream(dest))
			{
				fout.write(jar);
			}
			return dest;
		}
		catch (VerificationException | IOException e)
		{
			log.warn("unable to verify jar {}", installedArtifact.getArtifact().getName(), e);
		}
		return null;
	}

	// clean the plugin dir from any file that is not a settings file or 1 of the artifacts we know we want.
	private static void clean(Collection<InstalledArtifact> artifacts)
	{
		File[] existingDirs = PLUGIN_DIR.listFiles();

		if (existingDirs == null)
		{
			return;
		}

		Set<String> artifactNames = new HashSet<>();
		for (InstalledArtifact artifact : artifacts)
		{
			artifactNames.add(artifact.getArtifact().getName());
		}

		Set<String> repos = artifacts.stream().map(artifact ->
		{
			try
			{
				return hash(artifact.getManifestUrl());
			}
			catch (IOException ignored)
			{
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toSet());

		for (File dir : existingDirs)
		{
			if (dir.isDirectory())
			{
				if (!repos.contains(dir.getName()))
				{
					boolean result = dir.delete();
					if (!result)
					{
						for (File file : dir.listFiles())
						{
							if (file.isFile() && !artifactNames.contains(file.getName()))
							{
								if (file.delete())
								{
									log.debug("Deleted old artifact {}", file);
								}
								else
								{
									log.warn("Unable to delete old artifact {}", file);
								}
							}
						}
					}
				}
			}
			else if (!dir.equals(SETTINGS_FILE))
			{
				dir.delete();
			}
		}
	}

	public void stopAllBLExternalPlugins()
	{
		for (Plugin plugin : pluginsInFlight.values())
		{
			Runnable r = () ->
			{
				try
				{
					pluginManager.setPluginEnabled(plugin, false);
					pluginManager.stopPlugin(plugin);
				}
				catch (PluginInstantiationException e)
				{
					log.warn("Couldn't stop external plugin: {}", plugin.getName());
				}
			};
			if (SwingUtilities.isEventDispatchThread())
			{
				r.run();
			}
			else
			{
				SwingUtilities.invokeLater(r);
			}
		}
	}

	private static Settings readSettings()
	{
		try (FileReader fr = new FileReader(SETTINGS_FILE))
		{
			return GSON.fromJson(fr, Settings.class);
		}
		catch (IOException | JsonSyntaxException e)
		{
			log.warn("error parsing settings map");
			return null;
		}
	}

	private static void writeSettingsMap(Map<String, Collection<String>> plugins)
	{
		Settings settings = new Settings(plugins);

		try (FileWriter fileWriter = new FileWriter(SETTINGS_FILE))
		{
			fileWriter.write(GSON.toJson(settings));
		}
		catch (IOException e)
		{
			log.warn("couldn't write to bl externals file");
		}
	}

	public void writeSettings()
	{
		ArrayListMultimap<String, String> installedPlugins = ArrayListMultimap.create();
		for (InstalledArtifact installedArtifact : pluginsInFlight.keySet())
		{
			installedPlugins.put(installedArtifact.getManifestUrl(), installedArtifact.getArtifact().getName());
		}

		writeSettingsMap(installedPlugins.asMap());
	}
}

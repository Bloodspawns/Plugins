package net.runelite.client.plugins.blexternalmanager.swing;

import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.blexternalmanager.BLExternalManager;
import net.runelite.client.plugins.blexternalmanager.beans.Artifact;
import net.runelite.client.plugins.blexternalmanager.beans.Manifest;
import net.runelite.client.plugins.blexternalmanager.swing.components.AbstractTabPanel;
import net.runelite.client.plugins.blexternalmanager.swing.components.InstalledArtifact;
import net.runelite.client.plugins.blexternalmanager.swing.components.TabItem;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Slf4j
public class RepositoryTabPanel extends AbstractTabPanel
{
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.DAYS, new LinkedBlockingQueue<>());
	Manifest manifest = null;
	String url = "";

	public RepositoryTabPanel(BLExternalManager blExternalManager, ExternalHubPanel externalHubPanel)
	{
		super(blExternalManager, externalHubPanel);
	}

	public void clear()
	{
		manifest = null;
		url = "";
		panel.removeAll();
		revalidate();
	}

	public void update()
	{
		if (manifest == null || Strings.isNullOrEmpty(url))
		{
			return;
		}

		List<Pair<InstalledArtifact, Boolean>> artifacts = sort(build(manifest, url));

		Runnable runnable = () ->
		{
			panel.removeAll();

			artifacts.stream()
				.filter(Objects::nonNull)
				.map(e -> new TabItem(e.getLeft(), e.getRight(), externalHubPanel.installEventHandler, externalHubPanel.uninstallEventHandler))
				.forEach(panel::add);

			revalidate();
		};

		if (!SwingUtilities.isEventDispatchThread())
		{
			try
			{
				SwingUtilities.invokeAndWait(runnable);
			}
			catch (InterruptedException | InvocationTargetException e)
			{
				log.warn("Error updating repo panel items", e);
			}
		}
		else
		{
			runnable.run();
		}
	}

	void update(String manifestURL)
	{
		executor.execute(() ->
		{
			try
			{
				manifest = Manifest.getManifest(manifestURL);
				url = manifestURL;
			}
			catch (IOException e)
			{
				clear();
				log.debug("couldn't get manifest from url for: {}", e.getMessage());
				return;
			}

			update();
		});
	}

	private static List<Pair<InstalledArtifact, Boolean>> sort(Map<InstalledArtifact, Boolean> map)
	{
		if (map.isEmpty())
		{
			return Collections.emptyList();
		}

		List<Map.Entry<InstalledArtifact, Boolean>> list = new ArrayList<>(map.entrySet());
		list.sort(Comparator.comparingInt(new ToIntFunction<Map.Entry<InstalledArtifact, Boolean>>()
		{
			@Override
			public int applyAsInt(Map.Entry<InstalledArtifact, Boolean> e)
			{
				return e.getValue() ? 0 : 1;
			}
		}).thenComparing(a -> a.getKey().getArtifact().getName(), Comparator.naturalOrder()));

		return list.stream()
			.map(e -> new ImmutablePair<>(e.getKey(), e.getValue()))
			.collect(Collectors.toUnmodifiableList());
	}

	private static Map<InstalledArtifact, Boolean> build(Manifest manifest, String url)
	{
		Map<InstalledArtifact, Boolean> panelMap = new HashMap<>();
		try
		{
			if (manifest != null)
			{
				for (Artifact artifact : manifest.getArtifacts())
				{
					if (!artifact.isValid())
					{
						continue;
					}

					panelMap.put(new InstalledArtifact(url, artifact), false);
				}
			}
		}
		catch (RuntimeException ex)
		{
			log.debug("couldn't get manifest from url for: {}", ex.getMessage());
			return Collections.emptyMap();
		}

		Collection<InstalledArtifact> _installedArtifacts = BLExternalManager.getInstalledArtifacts();
		Set<Pair<String, String>> installedArtifacts = _installedArtifacts.stream().map(installedArtifact -> ImmutablePair.of(installedArtifact.getManifestUrl(), installedArtifact.getArtifact().getName())).collect(Collectors.toSet());
		if (!installedArtifacts.isEmpty())
		{
			Set<InstalledArtifact> keys = new HashSet<>(panelMap.keySet());
			for (InstalledArtifact artifact : keys)
			{
				Pair<String, String> pair = ImmutablePair.of(artifact.getManifestUrl(), artifact.getArtifact().getName());
				if (!installedArtifacts.contains(pair))
				{
					continue;
				}

				panelMap.replace(artifact, true);
			}
		}

		return panelMap;
	}
}
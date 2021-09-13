package net.runelite.client.plugins.blexternalmanager.swing;

import joptsimple.internal.Strings;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.blexternalmanager.BLExternalManager;
import net.runelite.client.plugins.blexternalmanager.beans.Artifact;
import net.runelite.client.plugins.blexternalmanager.swing.components.AbstractTabPanel;
import net.runelite.client.plugins.blexternalmanager.swing.components.InstalledArtifact;
import net.runelite.client.plugins.blexternalmanager.swing.components.TabItem;
import net.runelite.client.ui.FontManager;

import javax.inject.Inject;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;

public class InstalledTabPanel extends AbstractTabPanel
{
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.DAYS, new LinkedBlockingQueue<>());
	private static final File BEXTERNALPLUGINS_DIR = new File(RuneLite.RUNELITE_DIR, "bexternalplugins");
	public static final String BEXTERNALPLUGINS_DIR_PATH = "%USERPROFILE%/.runelite/bexternalplugins";
	private final HashMap<InstalledArtifact, TabItem> items = new HashMap<>();
	private final JLabel refreshMessage;

	@Inject
	InstalledTabPanel(BLExternalManager blExternalManager, ExternalHubPanel externalHubPanel)
	{
		super(blExternalManager, externalHubPanel);

		refreshMessage = new JLabel("Refresh to show all installed external plugins");
		refreshMessage.setFont(FontManager.getRunescapeSmallFont());
		refreshMessage.setForeground(Color.LIGHT_GRAY);
		refreshMessage.setHorizontalAlignment(JLabel.CENTER);
		refreshMessage.setVerticalAlignment(JLabel.CENTER);
		panel.add(refreshMessage);
	}

	public void update()
	{
		refreshMessage.setVisible(false);

		executor.execute(() ->
		{
			ArrayList<File> bexternalplugins = getBExternalPlugins();
			ArrayList<InstalledArtifact> artifacts = new ArrayList<>(BLExternalManager.getInstalledArtifacts());
			bexternalplugins.forEach(b -> artifacts.add(new InstalledArtifact(BEXTERNALPLUGINS_DIR_PATH, new Artifact(b.getName(), new String[0], "", "", "", ""))));
			artifacts.sort(Comparator.comparingInt(new ToIntFunction<InstalledArtifact>()
			{
				@Override
				public int applyAsInt(InstalledArtifact a)
				{
					return (!Strings.isNullOrEmpty(a.getManifestUrl()) && !InstalledTabPanel.BEXTERNALPLUGINS_DIR_PATH.equals(a.getManifestUrl())) ? 0 : 1;
				}
			}).thenComparing(installedArtifact -> installedArtifact.getArtifact().getName(), Comparator.naturalOrder()));

			panel.removeAll();
			SwingUtilities.invokeLater(() ->
			{
				for (InstalledArtifact artifact : artifacts)
				{
					TabItem item = new TabItem(artifact, externalHubPanel.uninstallEventHandler);
					panel.add(item);
					items.put(artifact, item);
				}
				revalidate();
			});
		});
	}

	private static ArrayList<File> getBExternalPlugins()
	{
		if (!BEXTERNALPLUGINS_DIR.exists())
		{
			return new ArrayList<>();
		}

		File[] files = BEXTERNALPLUGINS_DIR.listFiles();

		if (files == null || files.length == 0)
		{
			return new ArrayList<>();
		}

		ArrayList<File> fileList = new ArrayList<>();

		for (File file : files)
		{
			if (file.isDirectory())
			{
				continue;
			}

			if (file.getName().endsWith(".jar"))
			{
				fileList.add(file);
			}
		}

		return fileList;
	}
}

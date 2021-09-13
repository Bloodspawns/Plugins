package net.runelite.client.plugins.blexternalmanager.swing.components;

import joptsimple.internal.Strings;
import net.runelite.client.plugins.blexternalmanager.beans.Artifact;
import net.runelite.client.plugins.blexternalmanager.swing.InstalledTabPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.SwingUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import static net.runelite.client.plugins.blexternalmanager.swing.components.AbstractTabPanel.ADD;
import static net.runelite.client.plugins.blexternalmanager.swing.components.AbstractTabPanel.ADD_HOVER;
import static net.runelite.client.plugins.blexternalmanager.swing.components.AbstractTabPanel.PLUGIN;
import static net.runelite.client.plugins.blexternalmanager.swing.components.AbstractTabPanel.REMOVE;
import static net.runelite.client.plugins.blexternalmanager.swing.components.AbstractTabPanel.REMOVE_HOVER;

public class TabItem extends JPanel
{
	// installed
	public TabItem(InstalledArtifact installedArtifact, Consumer<Triple<ActionEvent, JButton, InstalledArtifact>> uninstallActionListener)
	{
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setOpaque(true);
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		Artifact artifact = installedArtifact.getArtifact();

		JLabel fileName = new JLabel(truncate(artifact.getName(), 30));
		fileName.setToolTipText(artifact.getName());
		fileName.setForeground(Color.WHITE);
		fileName.setFont(FontManager.getRunescapeFont());

		JLabel fileLocation = new JLabel(truncate(installedArtifact.getManifestUrl(), 30));
		fileLocation.setToolTipText(installedArtifact.getManifestUrl());
		fileLocation.setVerticalAlignment(JLabel.CENTER);
		fileLocation.setForeground(Color.LIGHT_GRAY);
		fileLocation.setFont(FontManager.getRunescapeSmallFont());

		JButton rb = new JButton();
		if (!Strings.isNullOrEmpty(installedArtifact.getManifestUrl()) && !InstalledTabPanel.BEXTERNALPLUGINS_DIR_PATH.equals(installedArtifact.getManifestUrl()))
		{
			rb.setIcon(REMOVE);
			rb.setRolloverIcon(REMOVE_HOVER);
			rb.setToolTipText("Uninstall Plugin");
			rb.addActionListener(l ->
			{
				uninstallActionListener.accept(ImmutableTriple.of(l, rb, installedArtifact));
			});
		}
		SwingUtil.removeButtonDecorations(rb);

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(5)
			.addComponent(fileName)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(fileLocation, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(rb, 18, 18, 18))
		);
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addGap(5)
				.addComponent(fileName, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
			.addGroup(layout.createSequentialGroup()
				.addGap(5)
				.addComponent(fileLocation, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(rb, 18, 18, 18)
				.addGap(5))
		);

		revalidate();
	}

	// repo
	public TabItem(InstalledArtifact installedArtifact, boolean installed, Consumer<Triple<ActionEvent, JButton, InstalledArtifact>> installActionListener, Consumer<Triple<ActionEvent, JButton, InstalledArtifact>> uninstallActionListener)
	{
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setOpaque(true);
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		Artifact artifact = installedArtifact.getArtifact();

		JLabel pluginIcon = new JLabel();
		pluginIcon.setVerticalAlignment(JLabel.CENTER);
		pluginIcon.setHorizontalAlignment(JLabel.CENTER);
		pluginIcon.setIcon(PLUGIN);

		String name = artifact.getName();
		JLabel pluginName = new JLabel(truncate(name, 25));
		pluginName.setToolTipText(name);
		pluginName.setForeground(Color.WHITE);
		pluginName.setFont(FontManager.getRunescapeFont());

		// just because i don't trust people to <br> and cause the plugin list to go brr all the way to the right
		String desc = artifact.getDescription();
		String description = String.format("<html><div style=\"width:%dpx;\">%s</div></html>", 140, desc == null ? "" : Text.removeTags(desc));
		JLabel pluginDescription = new JLabel(truncate(description.trim(), 95));
		pluginDescription.setVerticalAlignment(JLabel.TOP);
		if (desc != null && !desc.isEmpty())
		{
			pluginDescription.setToolTipText(description);
		}
		pluginDescription.setFont(FontManager.getRunescapeSmallFont());
		final int h = pluginDescription.getFontMetrics(pluginDescription.getFont()).getHeight();
		pluginDescription.setPreferredSize(new Dimension(160, h * 2));

		String version = Text.removeTags(artifact.getVersion());
		JLabel pluginVersion = new JLabel(version);
		pluginVersion.setToolTipText(version);
		pluginVersion.setVerticalAlignment(JLabel.CENTER);
		pluginVersion.setForeground(Color.CYAN);
		pluginVersion.setFont(FontManager.getRunescapeSmallFont());

		JButton addrm = new JButton();
		if (!installed)
		{
			addrm.setIcon(ADD);
			addrm.setRolloverIcon(ADD_HOVER);
			addrm.setToolTipText("Install " + name);
			addrm.addActionListener(l ->
			{
				installActionListener.accept(ImmutableTriple.of(l, addrm, installedArtifact));
			});
		}
		else
		{
			addrm.setIcon(REMOVE);
			addrm.setRolloverIcon(REMOVE_HOVER);
			addrm.setToolTipText("Uninstall " + name);
			addrm.addActionListener(l ->
			{
				uninstallActionListener.accept(ImmutableTriple.of(l, addrm, installedArtifact));
			});
		}
		addrm.setFocusPainted(false);
		addrm.setVerticalAlignment(JButton.CENTER);
		SwingUtil.removeButtonDecorations(addrm);

		layout.setVerticalGroup(layout.createParallelGroup()
			.addComponent(pluginIcon, 80, GroupLayout.DEFAULT_SIZE, HEIGHT + (h * 2))
			.addGroup(layout.createSequentialGroup()
				.addGap(5)
				.addComponent(pluginName)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(pluginDescription, h, GroupLayout.PREFERRED_SIZE, h * 2 + (h / 2))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(pluginVersion, 18, 18, 18)
					.addComponent(addrm, 18, 18, 18))
				.addGap(5))
		);
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(pluginIcon, 48, 48, 48)
			.addGap(5)
			.addGroup(layout.createParallelGroup()
				.addComponent(pluginName, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(pluginDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
					.addComponent(pluginVersion, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(addrm, 18, 18, 18)
					.addGap(5)))
		);

		revalidate();
	}

	private static String truncate(String string, int len)
	{
		if (string.length() < len)
		{
			return string;
		}

		return string.substring(0, len) + "...";
	}
}
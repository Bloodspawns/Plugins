package net.runelite.client.plugins.blexternalmanager.swing;

import joptsimple.internal.Strings;
import net.runelite.client.plugins.blexternalmanager.BLExternalManager;
import net.runelite.client.plugins.blexternalmanager.Controls;
import net.runelite.client.plugins.blexternalmanager.swing.components.InstalledArtifact;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import org.apache.commons.lang3.tuple.Triple;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.runelite.client.plugins.blexternalmanager.swing.components.AbstractTabPanel.WAIT;
import static net.runelite.client.plugins.blexternalmanager.swing.components.AbstractTabPanel.WAIT_HOVER;

@Singleton
public class ExternalHubPanel extends PluginPanel
{
	private boolean stopping = false;
	private final BLExternalManager blExternalManager;
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.DAYS, new LinkedBlockingQueue<>());
	private final RepositoryTabPanel repositoryTabPanel;
	private final InstalledTabPanel installedTabPanel;
	public final Consumer<Triple<ActionEvent, JButton, InstalledArtifact>> installEventHandler = triple -> installListener(triple.getLeft(), triple.getMiddle(), triple.getRight());
	public final Consumer<Triple<ActionEvent, JButton, InstalledArtifact>> uninstallEventHandler = triple -> uninstallListener(triple.getLeft(), triple.getMiddle(), triple.getRight());

	@Inject
	ExternalHubPanel(BLExternalManager blExternalManager)
	{
		super(false);
		this.blExternalManager = blExternalManager;
		repositoryTabPanel = new RepositoryTabPanel(blExternalManager, this);
		installedTabPanel = new InstalledTabPanel(blExternalManager, this);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel warning1 = new JLabel("<html>External plugins are not verified. " +
			"Make sure you trust the person in control of the url. " +
			"The plugins can automatically update!</html>"
		);
		warning1.setBackground(new Color(0xFFBB33));
		warning1.setForeground(Color.BLACK);
		warning1.setBorder(new EmptyBorder(5, 5, 5, 2));
		warning1.setOpaque(true);

		JLabel warning2 = new JLabel("Install and use at your own risk!");
		warning2.setHorizontalAlignment(JLabel.CENTER);
		warning2.setFont(FontManager.getRunescapeBoldFont());
		warning2.setBackground(warning1.getBackground());
		warning2.setForeground(warning1.getForeground());
		warning2.setBorder(new EmptyBorder(0, 5, 5, 5));
		warning2.setOpaque(true);

		FlatTextField repoField = new FlatTextField();
		repoField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		repoField.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		String def = "enter manifest url here...";
		repoField.setText(def);
		repoField.getTextField().addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				if (repoField.getText().equals(def))
				{
					repoField.setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e)
			{
				if (Strings.isNullOrEmpty(repoField.getText()))
				{
					repoField.setText(def);
				}
			}
		});
		repoField.addActionListener(e ->
		{
			if (Strings.isNullOrEmpty(repoField.getText()))
			{
				repositoryTabPanel.clear();
				return;
			}
			repositoryTabPanel.update(repoField.getText());
		});

		MaterialTabGroup controlTabGroup = new MaterialTabGroup();
		controlTabGroup.setBorder(new EmptyBorder(0, 2, 0, 2));
		controlTabGroup.setLayout(new GridLayout(0, 4, 2, 0));
		populateControlTabGroup(controlTabGroup);

		JPanel tabDisplay = new JPanel();
		MaterialTabGroup tabGroup = new MaterialTabGroup(tabDisplay);

		MaterialTab rt = new MaterialTab("Repository", tabGroup, repositoryTabPanel);
		rt.setToolTipText("Online Plugin Repository");
		tabGroup.addTab(rt);

		MaterialTab it = new MaterialTab("Installed Externals", tabGroup, installedTabPanel);
		it.setToolTipText("Installed External Plugins");
		tabGroup.addTab(it);
		tabGroup.select(rt);

		final int h = repoField.getFontMetrics(repoField.getFont()).getHeight() * 2;
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(warning1)
			.addComponent(warning2)
			.addGap(2)
			.addComponent(repoField, h, h, h)
			.addGap(2)
			.addComponent(controlTabGroup)
			.addComponent(tabGroup)
			.addComponent(tabDisplay)
		);

		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(warning1, 0, Short.MAX_VALUE, Short.MAX_VALUE)
			.addComponent(warning2, 0, Short.MAX_VALUE, Short.MAX_VALUE)
			.addGroup(layout.createSequentialGroup()
				.addGap(2)
				.addComponent(repoField)
				.addGap(2))
			.addComponent(controlTabGroup)
			.addComponent(tabGroup)
			.addComponent(tabDisplay)
		);

		repositoryTabPanel.update();
		installedTabPanel.update();
		revalidate();
	}

	private void populateControlTabGroup(MaterialTabGroup group)
	{
		for (Controls control : Controls.values())
		{
			MaterialTab tab = new MaterialTab(control.getIcon(), group, null);
			tab.setToolTipText(control.getTooltip());

			if (control != Controls.POWER)
			{
				tab.addMouseListener(control.getMouseAdapter());
			}
			else
			{
				tab.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent e)
					{
						final int r = JOptionPane.showConfirmDialog(
								null,
								"<html><p>You're about to disable all BlueLite External Plugins</p><strong>" +
										"Are you sure you want to do this?</strong></html>",
								"RuneLite",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);

						if (r != JOptionPane.YES_OPTION || stopping)
						{
							return;
						}

						stopping = true;
						blExternalManager.stopAllBLExternalPlugins();
						stopping = false;
					}
				});
			}

			group.add(tab);
		}
	}

	private void uninstallListener(ActionEvent event, JButton button, InstalledArtifact installedArtifact)
	{
		button.setEnabled(false); // limit the user to only one click
		button.setIcon(WAIT);
		button.setRolloverIcon(WAIT_HOVER);
		button.setToolTipText("Uninstalling " + installedArtifact.getArtifact().getName());

		// Weaken the match on uninstall in case miss matching versions
		if (!BLExternalManager.getInstalledArtifacts().contains(installedArtifact))
		{
			for (InstalledArtifact artifact : BLExternalManager.getInstalledArtifacts())
			{
				if (artifact.getManifestUrl().equals(installedArtifact.getManifestUrl()) &&
					artifact.getArtifact().getName().equals(installedArtifact.getArtifact().getName()))
				{
					installedArtifact = artifact;
					break;
				}
			}
		}

		final InstalledArtifact finalInstalledArtifact = installedArtifact;
		executor.execute(() ->
		{
			blExternalManager.uninstallPlugin(finalInstalledArtifact);
			blExternalManager.writeSettings();
			repositoryTabPanel.update();
			installedTabPanel.update();
		});
	}

	private void installListener(ActionEvent event, JButton button, InstalledArtifact installedArtifact)
	{
		button.setEnabled(false); // limit the user to only one click
		button.setIcon(WAIT);
		button.setRolloverIcon(WAIT_HOVER);
		button.setToolTipText("Installing " + installedArtifact.getArtifact().getName());

		executor.execute(() ->
		{
			blExternalManager.installPlugin(installedArtifact);
			blExternalManager.writeSettings();
			repositoryTabPanel.update();
			installedTabPanel.update();
		});
	}
}

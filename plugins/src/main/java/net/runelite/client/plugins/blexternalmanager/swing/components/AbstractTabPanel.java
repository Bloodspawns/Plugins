package net.runelite.client.plugins.blexternalmanager.swing.components;

import net.runelite.client.plugins.blexternalmanager.BLExternalManager;
import net.runelite.client.plugins.blexternalmanager.swing.ExternalHubPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public abstract class AbstractTabPanel extends JPanel
{
//	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.DAYS, new LinkedBlockingQueue<>());
	public static final ImageIcon PLUGIN;
	public static final ImageIcon ADD;
	public static final ImageIcon ADD_HOVER;
	public static final ImageIcon REMOVE;
	public static final ImageIcon REMOVE_HOVER;
	public static final ImageIcon WAIT;
	public static final ImageIcon WAIT_HOVER;

	static
	{
		BufferedImage missingIcon = ImageUtil.loadImageResource(BLExternalManager.class, "plugin_icon.png");
		PLUGIN = new ImageIcon(missingIcon);

		BufferedImage addIcon = ImageUtil.loadImageResource(BLExternalManager.class, "install.png");
		ADD = new ImageIcon(addIcon);
		ADD_HOVER = new ImageIcon(ImageUtil.alphaOffset(addIcon, -100));

		BufferedImage removeIcon = ImageUtil.loadImageResource(BLExternalManager.class, "uninstall.png");
		REMOVE = new ImageIcon(removeIcon);
		REMOVE_HOVER = new ImageIcon(ImageUtil.alphaOffset(removeIcon, -100));

		BufferedImage waitIcon = ImageUtil.loadImageResource(BLExternalManager.class, "wait.png");
		WAIT = new ImageIcon(waitIcon);
		WAIT_HOVER = new ImageIcon(ImageUtil.alphaOffset(waitIcon, -100));
	}

	protected final BLExternalManager blExternalManager;
	protected final ExternalHubPanel externalHubPanel;

	protected final JPanel panel;
	protected final JLabel loading;

	public abstract void update();

	@Inject
	public AbstractTabPanel(BLExternalManager blExternalManager, ExternalHubPanel externalHubPanel)
	{
		super(true);
		this.blExternalManager = blExternalManager;
		this.externalHubPanel = externalHubPanel;

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		panel.setLayout(new DynamicGridLayout(0, 1, 0, 2));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);

		loading = new JLabel("Loading...");
		loading.setHorizontalAlignment(JLabel.CENTER);

		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BorderLayout());
		wrapper.add(panel, BorderLayout.NORTH);
		wrapper.add(loading, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(wrapper);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0x7000, 0x7000));
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 2));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(5)
				.addComponent(scrollPane)
				.addGap(2)
		);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(scrollPane)
		);

		revalidate();
		loading.setVisible(false);
	}
}
package net.runelite.client.plugins.accountmanager;

import com.microsoft.alm.secret.Credential;
import com.microsoft.alm.storage.SecretStore;
import com.microsoft.alm.storage.StorageProvider;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AccountManagerPluginPanel extends PluginPanel
{
	private static final SecretStore<Credential> store;
	private static final String OSRS_KEY_PREFIX = "OSRS_ACCOUNT_";

	private static final ImageIcon SECTION_EXPAND_ICON;
	private static final ImageIcon SECTION_EXPAND_ICON_HOVER;
	private static final ImageIcon SECTION_RETRACT_ICON;
	private static final ImageIcon SECTION_RETRACT_ICON_HOVER;

	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_ICON_HOVER;
	private static final ImageIcon REMOVE_ICON;
	private static final ImageIcon REMOVE_ICON_HOVER;
	private static final ImageIcon UPDATE_ICON;
	private static final ImageIcon UPDATE_ICON_HOVER;
	private static final ImageIcon DEFAULT_ICON;
	private static final ImageIcon DEFAULT_ICON_SELECTED;
	private static final ImageIcon DEFAULT_ICON_HOVER;
	private static final ImageIcon LOGIN_ICON;
	private static final ImageIcon LOGIN_ICON_HOVER;

	private static final Dimension BUTTON_SIZE = new Dimension(16,16);

	static
	{
		store = StorageProvider.getCredentialStorage(true, StorageProvider.SecureOption.MUST);

		BufferedImage sectionRetractIcon = ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "/util/arrow_right.png");
		sectionRetractIcon = ImageUtil.luminanceOffset(sectionRetractIcon, -121);
		SECTION_EXPAND_ICON = new ImageIcon(sectionRetractIcon);
		SECTION_EXPAND_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(sectionRetractIcon, -100));
		final BufferedImage sectionExpandIcon = ImageUtil.rotateImage(sectionRetractIcon, Math.PI / 2);
		SECTION_RETRACT_ICON = new ImageIcon(sectionExpandIcon);
		SECTION_RETRACT_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(sectionExpandIcon, -100));

		ADD_ICON = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "addNormal.png"));
		ADD_ICON_HOVER = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "addHovered.png"));
		REMOVE_ICON = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "removeNormal.png"));
		REMOVE_ICON_HOVER = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "removeHovered.png"));
		UPDATE_ICON = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "updateNormal.png"));
		UPDATE_ICON_HOVER = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "updateHovered.png"));
		DEFAULT_ICON = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "defaultNormal.png"));
		DEFAULT_ICON_SELECTED = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "defaultSelected.png"));
		DEFAULT_ICON_HOVER = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "defaultHovered.png"));
		LOGIN_ICON = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "loginNormal.png"));
		LOGIN_ICON_HOVER = new ImageIcon(ImageUtil.loadImageResource(AccountManagerPluginPanel.class, "loginHovered.png"));
	}

	private final Client client;
	private final ClientThread clientThread;
	private final HashSet<Integer> accountIndices;
	private final AccountManagerConfig config;
	private JPanel accountSection;
	private final HashMap<JComponent, Boolean> collapsableComponentState = new HashMap<>();
	private ScheduledExecutorService executorService;

	@Inject
	public AccountManagerPluginPanel(AccountManagerConfig config, Client client, ClientThread clientThread, ScheduledExecutorService executorService)
	{
		this.config = config;
		this.client = client;
		this.clientThread = clientThread;
		this.executorService = executorService;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		accountIndices = parseIndices(config.getCredentialIndices());

		setupLayout();
	}

	private void setupLayout()
	{
		JPanel parent = new JPanel(new GridBagLayout());
		add(parent);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(2, 5, 2, 5);
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.CENTER;

		JLabel header = new JLabel("Account management");
		var headerConstraints = c.clone();
		parent.add(header, headerConstraints);
		c.gridy++;

		JLabel addAccountLabel = new JLabel("Add account");
		var addAccountConstraints = c.clone();
		parent.add(addAccountLabel, addAccountConstraints);
		c.gridy++;

		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.LINE_START;

		JLabel usernameLabel = new JLabel("Username");
		GridBagConstraints usernameLabelConstraints = (GridBagConstraints) c.clone();
		usernameLabelConstraints.gridwidth = 1;
		parent.add(usernameLabel, usernameLabelConstraints);
		c.gridy++;

		JTextField username = new JTextField();
		var usernameFieldConstraints = c.clone();
		parent.add(username, usernameFieldConstraints);
		c.gridy++;

		JLabel passwordLabel = new JLabel("Password");
		var passwordLabelConstraints = c.clone();
		parent.add(passwordLabel, passwordLabelConstraints);
		c.gridy++;

		JPasswordField password = new JPasswordField();
		var passwordFieldConstraints = c.clone();
		parent.add(password, passwordFieldConstraints);
		c.gridy++;

		JButton addAccountButton = new JButton();
		GridBagConstraints addAccountButtonConstraints = (GridBagConstraints) usernameLabelConstraints.clone();
		addAccountButtonConstraints.gridx = 1;
		addAccountButtonConstraints.anchor = GridBagConstraints.LINE_END;
		addAccountButtonConstraints.fill = GridBagConstraints.NONE;
		addAccountButton.setIcon(ADD_ICON);
		addAccountButton.setRolloverIcon(ADD_ICON_HOVER);
		addAccountButton.setPreferredSize(BUTTON_SIZE);
		addAccountButton.setToolTipText("Add account");
		SwingUtil.removeButtonDecorations(addAccountButton);
		addAccountButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				addAccountButtonClicked(parent, username, password);
			}
		});
		parent.add(addAccountButton, addAccountButtonConstraints);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		accountSection = new JPanel(new GridBagLayout());
		var accountSectionConstraints = c.clone();
		parent.add(accountSection, accountSectionConstraints);
		c.gridy++;
		setAccountSection();
	}

	private void addAccountButtonClicked(JComponent parent, JTextField username, JPasswordField password)
	{
		boolean result = add(username.getText(), password.getPassword());
		if (!result)
		{
			JOptionPane.showMessageDialog(parent,
					"Could not save account!");
		}
		else
		{
			username.setText("");
			password.setText("");
		}
	}

	private void setAccountSection()
	{
		accountSection.removeAll();
		GridBagConstraints cAccounts = new GridBagConstraints();
		cAccounts.gridx = 0;
		cAccounts.gridy = 0;
		cAccounts.weightx = 1;
		cAccounts.weighty = 0;
		cAccounts.ipady = 5;
		cAccounts.fill = GridBagConstraints.BOTH;
		cAccounts.anchor = GridBagConstraints.LINE_START;
		ArrayList<Integer> badIndices = new ArrayList<>();
		var credentials = getOsrsCredentials(accountIndices, badIndices);
		badIndices.forEach(accountIndices::remove);
		for (OsrsCredential credential : credentials)
		{
			boolean isDefault = config.getDefaultAccountIndex() == credential.getIndex();
			var item = createItem(isDefault, credential);
			accountSection.add(item, cAccounts);
			cAccounts.gridy++;
		}
		accountSection.revalidate();
		accountSection.repaint();
	}

	private JComponent createItem(boolean isDefault, OsrsCredential credential)
	{
		final JPanel section = new JPanel();
		section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

		final JPanel sectionHeader = new JPanel();
		sectionHeader.setLayout(new BorderLayout());
		// For whatever reason, the header extends out by a single pixel when closed. Adding a single pixel of
		// border on the right only affects the width when closed, fixing the issue.
		sectionHeader.setBorder(new CompoundBorder(
				new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR),
				new EmptyBorder(0, 0, 3, 1)));
		section.add(sectionHeader);

		final boolean isOpen = collapsableComponentState.getOrDefault(section, false);
		final JButton sectionToggle = new JButton(isOpen ? SECTION_RETRACT_ICON : SECTION_EXPAND_ICON);
		sectionToggle.setRolloverIcon(isOpen ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
		sectionToggle.setPreferredSize(new Dimension(18, 0));
		sectionToggle.setBorder(new EmptyBorder(0, 0, 0, 5));
		sectionToggle.setToolTipText(isOpen ? "Retract" : "Expand");
		SwingUtil.removeButtonDecorations(sectionToggle);
		sectionHeader.add(sectionToggle, BorderLayout.WEST);

		String username = credential.getCredential().Username;
		String usernameLabelText = username.substring(0, Math.min(3, username.length())) + "... (" + credential.getIndex() + ")";
		final JLabel sectionName = new JLabel(usernameLabelText);
		sectionHeader.add(sectionName, BorderLayout.CENTER);

		JPanel buttonSection = new JPanel(new GridBagLayout());
		sectionHeader.add(buttonSection, BorderLayout.EAST);

		final JPanel sectionContents = new JPanel();
		sectionContents.setLayout(new GridBagLayout());
		sectionContents.setBorder(new CompoundBorder(
				new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR),
				new EmptyBorder(BORDER_OFFSET, 0, BORDER_OFFSET, 0)));
		sectionContents.setVisible(isOpen);
		section.add(sectionContents);

		// Add listeners to each part of the header so that it's easier to toggle them
		final MouseAdapter adapter = new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				toggleSection(section, sectionToggle, sectionContents);
			}
		};
		sectionToggle.addActionListener(actionEvent -> toggleSection(section, sectionToggle, sectionContents));
		sectionName.addMouseListener(adapter);
		sectionHeader.addMouseListener(adapter);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(2, 0, 2, 0);
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.LINE_START;

		JLabel usernameLabel = new JLabel("Username");
		var usernameLabelConstraints = c.clone();
		sectionContents.add(usernameLabel, usernameLabelConstraints);
		c.gridy++;

		JTextField usernameField = new JTextField(credential.getCredential().Username);
		var usernameFieldConstraints = c.clone();
		sectionContents.add(usernameField, usernameFieldConstraints);
		c.gridy++;

		JLabel passwordLabel = new JLabel("Password");
		var passwordLabelConstraints = c.clone();
		sectionContents.add(passwordLabel, passwordLabelConstraints);
		c.gridy++;

		JPasswordField passwordField = new JPasswordField(credential.getCredential().Password);
		var passwordFieldConstraints = c.clone();
		sectionContents.add(passwordField, passwordFieldConstraints);

		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.gridx = 0;
		buttonConstraints.gridy = 0;
		buttonConstraints.weightx = 1;
		buttonConstraints.insets = new Insets(0, 1, 0, 1);
		buttonConstraints.fill = GridBagConstraints.NONE;
		buttonConstraints.anchor = GridBagConstraints.LINE_END;

		JButton loginButton = new JButton();
		loginButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				setLogin(credential);
			}
		});
		loginButton.setIcon(LOGIN_ICON);
		loginButton.setRolloverIcon(LOGIN_ICON_HOVER);
		loginButton.setPreferredSize(BUTTON_SIZE);
		loginButton.setToolTipText("Set login to this account");
		SwingUtil.removeButtonDecorations(loginButton);
		var loginButtonConstraints = buttonConstraints.clone();
		buttonSection.add(loginButton, loginButtonConstraints);
		buttonConstraints.gridx++;

		JButton defaultButton = new JButton();
		defaultButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				setDefault(credential.getIndex());
			}
		});
		defaultButton.setIcon(isDefault ? DEFAULT_ICON_SELECTED : DEFAULT_ICON);
		defaultButton.setRolloverIcon(DEFAULT_ICON_HOVER);
		defaultButton.setPreferredSize(BUTTON_SIZE);
		defaultButton.setToolTipText("Set account as default");
		SwingUtil.removeButtonDecorations(defaultButton);
		var defaultButtonConstraints = buttonConstraints.clone();
		buttonSection.add(defaultButton, defaultButtonConstraints);
		buttonConstraints.gridx++;

		JButton updateButton = new JButton();
		updateButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				Credential _credential = new Credential(usernameField.getText(), new String(passwordField.getPassword()));
				credential.setCredential(_credential);
				update(credential);
			}
		});
		updateButton.setIcon(UPDATE_ICON);
		updateButton.setRolloverIcon(UPDATE_ICON_HOVER);
		updateButton.setPreferredSize(BUTTON_SIZE);
		updateButton.setToolTipText("Store this account with edited info");
		SwingUtil.removeButtonDecorations(updateButton);
		var updateButtonConstraints = buttonConstraints.clone();
		buttonSection.add(updateButton, updateButtonConstraints);
		buttonConstraints.gridx++;

		JButton removeButton = new JButton();
		removeButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				remove(credential);
			}
		});
		removeButton.setIcon(REMOVE_ICON);
		removeButton.setRolloverIcon(REMOVE_ICON_HOVER);
		removeButton.setPreferredSize(BUTTON_SIZE);
		removeButton.setToolTipText("Remove this account");
		SwingUtil.removeButtonDecorations(removeButton);
		var removeButtonConstraints = buttonConstraints.clone();
		buttonSection.add(removeButton, removeButtonConstraints);
		return section;
	}

	private void toggleSection(JComponent section, JButton button, JPanel contents)
	{
		boolean newState = !contents.isVisible();
		contents.setVisible(newState);
		button.setIcon(newState ? SECTION_RETRACT_ICON : SECTION_EXPAND_ICON);
		button.setRolloverIcon(newState ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
		button.setToolTipText(newState ? "Retract" : "Expand");
		collapsableComponentState.put(section, newState);
		contents.revalidate();
	}

	private static ArrayList<OsrsCredential> getOsrsCredentials(HashSet<Integer> indices, ArrayList<Integer> badIndices)
	{
		ArrayList<OsrsCredential> credentials = new ArrayList<>();

		for (Integer index : indices)
		{
			var credential = getOsrsCredential(index);
			if (credential != null)
			{
				credentials.add(credential);
			}
			else if (badIndices != null)
			{
				badIndices.add(index);
			}
		}

		return credentials;
	}

	private static OsrsCredential getOsrsCredential(int index)
	{
		String key = OSRS_KEY_PREFIX + index;
		var credential = store.get(key);
		if (credential != null)
		{
			return new OsrsCredential(index, credential);
		}
		return null;
	}

	private static HashSet<Integer> parseIndices(String indicesInConfig)
	{
		HashSet<Integer> indices = new HashSet<>();
		String[] split = indicesInConfig.split(",");
		for (String s : split)
		{
			if ("".equals(s))
			{
				continue;
			}
			int index = Integer.parseInt(s);
			indices.add(index);
		}
		return indices;
	}

	private static int firstFreeIndex(HashSet<Integer> indices)
	{
		int index = 0;
		if (!indices.contains(index))
		{
			return index;
		}
		for (Integer _index : indices)
		{
			if (!indices.contains(_index + 1))
			{
				return _index + 1;
			}
		}
		throw new RuntimeException("Couldnt find free index");
	}

	private boolean add(String username, char[] password)
	{
		int index = firstFreeIndex(accountIndices);
		Credential credential = new Credential(username, new String(password));
		String key = OSRS_KEY_PREFIX + index;
		boolean result = store.add(key, credential);
		if (!result)
		{
			return false;
		}
		accountIndices.add(index);

		saveToConfig();
		setAccountSection();
		return true;
	}

	private boolean update(OsrsCredential credential)
	{
		String key = OSRS_KEY_PREFIX + credential.getIndex();
		boolean resultDelete = store.delete(key);
		if (!resultDelete)
		{
			return false;
		}

		return store.add(key, credential.getCredential());
	}

	private boolean remove(OsrsCredential credential)
	{
		String key = OSRS_KEY_PREFIX + credential.getIndex();
		boolean result = store.delete(key);

		if (!result)
		{
			return false;
		}

		accountIndices.remove(credential.getIndex());

		saveToConfig();
		setAccountSection();
		return true;
	}

	private void setDefault(int index)
	{
		config.setDefaultAccountIndex(index);
		setAccountSection();
	}

	private void saveToConfig()
	{
		StringBuilder indices = new StringBuilder();
		for (Integer accountIndex : accountIndices)
		{
			indices.append(accountIndex).append(",");
		}
		// Strip leading ","
		String configString = indices.length() > 0 ? indices.substring(0, indices.length() - 1) : "";
		config.setCredentialIndices(configString);
	}

	private void setLogin(final OsrsCredential credential)
	{
		if (client.getGameState() != GameState.LOGIN_SCREEN)
		{
			return;
		}

		clientThread.invokeLater(() ->
		{
			client.setUsername(credential.getCredential().Username);
			client.setPassword(credential.getCredential().Password);

			if (config.pressEnter())
			{
				KeyEvent keyPress1 = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
				executorService.schedule(() -> SwingUtilities.invokeLater(() -> client.getCanvas().dispatchEvent(keyPress1)), 100, TimeUnit.MILLISECONDS);
				executorService.schedule(() -> SwingUtilities.invokeLater(() -> client.getCanvas().dispatchEvent(keyPress1)), 200, TimeUnit.MILLISECONDS);
			}
		});
	}

	public void setLoginDefault()
	{
		if (config.getDefaultAccountIndex() < 0)
		{
			return;
		}

		int index = config.getDefaultAccountIndex();
		var credential = getOsrsCredential(index);

		setLogin(credential);
	}
}

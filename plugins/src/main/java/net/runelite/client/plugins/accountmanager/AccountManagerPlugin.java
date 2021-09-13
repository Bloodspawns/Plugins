package net.runelite.client.plugins.accountmanager;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;


@PluginDescriptor(
		name = "Account Manager",
		description = "Easily log into the game",
		tags = {"Login"},
		enabledByDefault = false
)
@Slf4j
@Singleton
public class AccountManagerPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private KeyManager keyManager;

	@Inject
	private AccountManagerConfig config;

	private AccountManagerPluginPanel panel;
	private NavigationButton button;
	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.hotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			panel.setLoginDefault();
		}
	};

	@Provides
	AccountManagerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AccountManagerConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel = injector.getInstance(AccountManagerPluginPanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

		button = NavigationButton.builder()
				.tooltip("Account Manager")
				.icon(icon)
				.priority(1)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(button);

		hotkeyListener.setEnabledOnLoginScreen(true);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(button);
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			keyManager.registerKeyListener(hotkeyListener);
		}
		else
		{
			keyManager.unregisterKeyListener(hotkeyListener);
		}
	}
}

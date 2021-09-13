package net.runelite.client.plugins.accountmanager;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("blAccountManagement")
public interface AccountManagerConfig extends Config
{
	@ConfigItem(
			keyName = "defaultAccount",
			name = "",
			description = "",
			hidden = true
	)
	default int getDefaultAccountIndex()
	{
		return -1;
	}

	@ConfigItem(
			keyName = "defaultAccount",
			name = "",
			description = "",
			hidden = true
	)
	default void setDefaultAccountIndex(int index)
	{
	}

	@ConfigItem(
			keyName = "credentialIndices",
			name = "",
			description = "",
			hidden = true
	)
	default String getCredentialIndices()
	{
		return "";
	}

	@ConfigItem(
			keyName = "credentialIndices",
			name = "",
			description = "",
			hidden = true
	)
	default void setCredentialIndices(String indices)
	{
	}

	@ConfigItem(
			keyName = "pressEnter",
			name = "Press enter for login",
			description = "Presses enter for the login",
			position = 0
	)
	default boolean pressEnter()
	{
		return false;
	}

	@ConfigItem(
			keyName = "hotkey",
			name = "Login hotkey",
			description = "When you press this key you will be logged in for the default account",
			position = 1
	)
	default Keybind hotkey()
	{
		return Keybind.NOT_SET;
	}
}

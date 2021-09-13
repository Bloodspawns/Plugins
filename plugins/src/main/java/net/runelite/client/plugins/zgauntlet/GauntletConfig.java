package net.runelite.client.plugins.zgauntlet;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.FontType;

import java.awt.Color;

@ConfigGroup("Gauntlet")

public interface GauntletConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "hunleffTickCounter",
			name = "Hunleff Tick Counter",
			description = ""
	)
	default boolean hunleffTickCounter()
	{
		return false;
	}

	@ConfigItem(
			position = 1,
			keyName = "hunleffAttackCounter",
			name = "Hunleff Attack Counter",
			description = ""
	)
	default boolean hunleffAttackCounter()
	{
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "playerAttackCounter",
			name = "Player Attack Counter",
			description = ""
	)
	default boolean playerAttackCounter()
	{
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "nadoTickCounter",
			name = "Nado Tick Counter",
			description = ""
	)
	default boolean nadoTickCounter()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "nadoLocation",
		name = "Nado location",
		description = ""
	)
	default boolean nadoLocation()
	{
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "resourceTracker",
			name = "Resource Tracker",
			description = ""
	)
	default boolean resourceTracker()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "resourceDrawDistance",
			name = "Resource draw distance",
			description = "0 is disabled"
	)
	default int resourceDrawDistance()
	{
		return 0;
	}

	@ConfigItem(
			position = 5,
			keyName = "prayerPanel",
			name = "Hunleff prayer panel",
			description = ""
	)
	default boolean showPrayerPanel()
	{
		return false;
	}

	@ConfigItem(
			position = 6,
			keyName = "fontTypeOverwrite",
			name = "Font overwrite",
			description = ""
	)
	default FontType fontTypeOverwrite()
	{
		return FontType.SMALL;
	}

	@ConfigItem(
			position = 6,
			keyName = "gauntletTimerPanel",
			name = "Gauntlet Timer Overlay",
			description = ""
	)
	default boolean gauntletTimerOverlay()
	{
		return true;
	}

	@ConfigItem(
			position = 7,
			keyName = "verticalResourceOverlay",
			name = "Vertical Resource Overlay",
			description = ""
	)
	default boolean verticalResourceOverlay()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "treeResourceColor",
			name = "Tree resource color",
			description = ""
	)
	default Color treeResourceColor()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "rockResourceColor",
			name = "Rock resource color",
			description = ""
	)
	default Color rockResourceColor()
	{
		return Color.MAGENTA;
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "plantResourceColor",
			name = "Plant resource color",
			description = ""
	)
	default Color plantResourceColor()
	{
		return Color.ORANGE;
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "linumResourceColor",
			name = "Linum resource color",
			description = ""
	)
	default Color linumResourceColor()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "fishResourceColor",
			name = "Fish resource color",
			description = ""
	)
	default Color fishResourceColor()
	{
		return Color.CYAN;
	}

	//------------REGULAR----------------------------------------------------------------------------------------------

	@ConfigItem(
			position = 9,
			keyName = "teleportCrystalCount",
			name = "Amount of Teleport crystals",
			description = "Amount of teleport crystals you expect to need"
	)
	default int teleportCrystalCount()
	{
		return 0;
	}

	@ConfigItem(
			position = 10,
			keyName = "potionCount",
			name = "Amount of potions",
			description = "Amount of potions you expect to need"
	)
	default int potionCount()
	{
		return 0;
	}

	@ConfigItem(
			position = 10,
			keyName = "fishCount",
			name = "Amount of fish",
			description = "Amount of fish you expect to need"
	)
	default int fishCount()
	{
		return 0;
	}

	@ConfigItem(
			position = 11,
			keyName = "helmetTier",
			name = "Helmet you want",
			description = ""
	)
	default GauntletTier helmetTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
			position = 12,
			keyName = "bodyTier",
			name = "Body you want",
			description = ""
	)
	default GauntletTier bodyTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
			position = 13,
			keyName = "legsTier",
			name = "Legs you want",
			description = ""
	)
	default GauntletTier legsTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
			position = 14,
			keyName = "halberdTier",
			name = "halberd you want",
			description = ""
	)
	default GauntletTier halberdTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
			position = 15,
			keyName = "bowTier",
			name = "bow you want",
			description = ""
	)
	default GauntletTier bowTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
			position = 16,
			keyName = "staffTier",
			name = "staff you want",
			description = ""
	)
	default GauntletTier staffTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
		position = 17,
		keyName = "extraShards",
		name = "extra shards you want",
		description = ""
	)
	default int extraShards()
	{
		return 0;
	}

	//-------------CORRUPTED-----------------------------------------------------------------------------------------------

	@ConfigItem(
			position = 17,
			keyName = "corruptedTeleportCrystalCount",
			name = "(C) Amount of Teleport crystals",
			description = "Amount of teleport crystals you expect to need"
	)
	default int corruptedTeleportCrystalCount()
	{
		return 0;
	}

	@ConfigItem(
			position = 18,
			keyName = "corruptedPotionCount",
			name = "(C) Amount of potions",
			description = "Amount of potions you expect to need"
	)
	default int corruptedPotionCount()
	{
		return 0;
	}

	@ConfigItem(
			position = 18,
			keyName = "corruptedFishCount",
			name = "(C) Amount of fish",
			description = "Amount of fish you expect to need"
	)
	default int corruptedFishCount()
	{
		return 0;
	}

	@ConfigItem(
			position = 19,
			keyName = "corruptedHelmetTier",
			name = "(C) Helmet you want",
			description = ""
	)
	default GauntletTier corruptedHelmetTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
			position = 20,
			keyName = "corruptedBodyTier",
			name = "(C) Body you want",
			description = ""
	)
	default GauntletTier corruptedBodyTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
			position = 21,
			keyName = "corruptedLegsTier",
			name = "(C) Legs you want",
			description = ""
	)
	default GauntletTier corruptedLegsTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
			position = 22,
			keyName = "corruptedHalberdTier",
			name = "(C) halberd you want",
			description = ""
	)
	default GauntletTier corruptedHalberdTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
			position = 23,
			keyName = "corruptedBowTier",
			name = "(C) bow you want",
			description = ""
	)
	default GauntletTier corruptedBowTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
			position = 24,
			keyName = "corruptedStaffTier",
			name = "(C) staff you want",
			description = ""
	)
	default GauntletTier corruptedStaffTier()
	{
		return GauntletTier.NONE;
	}

	@ConfigItem(
		position = 25,
		keyName = "corruptedExtraShards",
		name = "(C) extra shards you want",
		description = ""
	)
	default int corruptedExtraShards()
	{
		return 0;
	}

	enum GauntletTier
	{
		NONE("None"),
		TIER1("Tier 1"),
		TIER2("Tier 2"),
		TIER3("Tier 3");

		public String name;

		GauntletTier(String name)
		{
			this.name = name;
		}
	}
}

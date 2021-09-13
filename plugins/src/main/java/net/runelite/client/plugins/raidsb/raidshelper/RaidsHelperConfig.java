package net.runelite.client.plugins.raidsb.raidshelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("raidsHelper")
public interface RaidsHelperConfig extends Config
{
    @ConfigItem(
        position = 10,
        keyName = "olmTpPortals",
        name = "Display tp portal location",
        description = ""
    )
    default boolean olmTpPortals()
    {
        return true;
    }

    @ConfigItem(
        position = 11,
        keyName = "olmFallingCrystals",
        name = "Display olm falling crystals",
        description = ""
    )
    default boolean olmFallingCrystals()
    {
        return true;
    }

    @ConfigItem(
            position = 12,
            keyName = "olmHealingPools",
            name = "Display olm healing pools",
            description = ""
    )
    default boolean olmHealingPools()
    {
        return true;
    }
}

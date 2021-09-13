package net.runelite.client.plugins.entityhiderextension;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ehextended")
public interface EntityHiderExtendedConfig extends Config
{

    @ConfigItem(
        position = 1,
        keyName = "hideDeadNPCs",
        name = "Hide any dead NPC",
        description = "Configures whether or not NPCs that just died are hidden"
    )
    default boolean hideDeadNPCs()
    {
        return false;
    }

    @ConfigItem(
        position = 15,
        keyName = "hideNPCsNames",
        name = "Hide NPCs names",
        description = "Configures which NPCs to hide"
    )
    default String hideNPCsNames()
    {
        return "";
    }

    @ConfigItem(
        position = 17,
        keyName = "hideNPCsOnDeath",
        name = "Hide NPCs on death",
        description = "Configures which NPCs to hide when they die"
    )
    default String hideNPCsOnDeath()
    {
        return "";
    }

    @ConfigItem(
            position = 18,
            keyName = "hideNPCsByID",
            name = "Hide NPCs by ID",
            description = "Configures which NPCs to hide by their ID"
    )
    default String hideNPCsByID()
    {
        return "";
    }

    @ConfigItem(
            position = 19,
            keyName = "hideNPCsByAnimationId",
            name = "Hide NPCs by animation ID",
            description = "Configures which NPCs to hide by their animation ID"
    )
    default String hideNPCsByAnimationId()
    {
        return "";
    }
}

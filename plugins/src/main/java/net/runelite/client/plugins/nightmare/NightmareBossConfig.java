package net.runelite.client.plugins.nightmare;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("NightmareBoss")
public interface NightmareBossConfig extends Config
{
//    @ConfigItem(
//        keyName = "test",
//        name = "",
//        description = ""
//    )
//    default boolean test(){ return false; }

    @ConfigItem(
        position = 0,
        keyName = "prayers",
        name = "Swap the prayers at p2",
        description = ""
    )
    default boolean nightmarePrayers(){ return false; }

    @ConfigItem(
            position = 0,
            keyName = "Overlay",
            name = "Show what to pray",
            description = ""
    )
    default boolean nightmarePrayerOverlay(){ return false; }

    @ConfigItem(
            position = 1,
            keyName = "parasites",
            name = "Countdown for parasites",
            description = ""
    )
    default boolean nightmareParasites(){ return false; }

    @ConfigItem(
            position = 2,
            keyName = "hands",
            name = "Show overlay for black hands",
            description = ""
    )
    default boolean nightmareHands(){ return false; }

    @Alpha
    @ConfigItem(
        position = 3,
        keyName = "handsColorOutline",
        name = "Outline color for overlay for black hands",
        description = ""
    )
    default Color handsColorOutline(){ return Color.CYAN; }

    @Alpha
    @ConfigItem(
        position = 4,
        keyName = "handsColorFill",
        name = "Fill color for overlay for black hands",
        description = ""
    )
    default Color handsColorFill(){ return new Color(0, 255, 255, 128); }
}

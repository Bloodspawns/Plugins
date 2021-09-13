package net.runelite.client.plugins.tobstats;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("StatisticsPlugin")

public interface TobstatsConfig extends Config
{
    @ConfigItem(
            position = 1,
            keyName = "chatMessage",
            name = "Phase timer chat message",
            description = "Phase timer chat message"
    )
    default boolean chatMessagePhase(){ return false; }

    @ConfigItem(
            position = 2,
            keyName = "precisionTimers",
            name = "Shows 100th of a second",
            description = ""
    )
    default boolean precisionTimers(){ return false; }

    @ConfigItem(
            position = 3,
            keyName = "chatTimerSplits",
            name = "Add splits to the chat timer",
            description = "Add splits to the chat timer"
    )
    default boolean chatTimerSplits(){ return false; }

    @ConfigItem(
            position = 4,
            keyName = "timerOverlay",
            name = "Add timer overlay to the screen",
            description = "Add timer overlay to the screen"
    )
    default boolean timerOverlay(){ return false; }

    @ConfigItem(
            position = 5,
            keyName = "simpleTimerOverlay",
            name = "Simple timer overlay mode",
            description = "Change the timer overlay to a simpler one"
    )
    default boolean simpleTimerOverlay(){ return false; }
}

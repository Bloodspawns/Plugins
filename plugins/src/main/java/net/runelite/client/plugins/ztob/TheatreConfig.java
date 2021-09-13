/*
 * THIS SOFTWARE WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI
 * No rights reserved. Use, redistribute, and modify at your own discretion,
 * and in accordance with Yagex and RuneLite guidelines.
 * However, aforementioned monkey would prefer if you don't sell this plugin for profit.
 * Good luck on your raids!
 */

package net.runelite.client.plugins.ztob;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("Theatre")

public interface TheatreConfig extends Config
{
    @ConfigItem(
            position = 0,
            keyName = "maidenBlood",
            name = "Maiden blood attack",
            description = ""
    )
    default boolean maidenBlood(){ return false; }

    @ConfigItem(
            position = 1,
            keyName = "maidenSpawns",
            name = "Maiden blood spawns",
            description = ""
    )
    default boolean maidenSpawns(){ return false; }

    @ConfigItem(
        position = 2,
        keyName = "MaidenTickCounter",
        name = "Maiden tick counter",
        description = ""
    )
    default boolean maidenTickCounter()
    {
        return false;
    }

    @ConfigItem(
            position = 10,
            keyName = "bloatIndicator",
            name = "Bloat indicator",
            description = ""
    )
    default boolean bloatIndicator(){ return false; }

    @ConfigItem(
            position = 11,
            keyName = "bloatHands",
            name = "Bloat hands",
            description = ""
    )
    default BloatHandsSetting bloatHands(){ return BloatHandsSetting.DISABLED; }

    @ConfigItem(
            position = 12,
            keyName = "bloatUpTimer",
            name = "Bloat timer",
            description = ""
    )
    default boolean bloatUpTimer(){ return false; }

    @ConfigItem(
            position = 13,
            keyName = "nyloPillars",
            name = "Nylocas pillar health",
            description = ""
    )
    default boolean nyloPillars(){ return false; }

    @ConfigItem(
            position = 14,
            keyName = "nyloBlasts",
            name = "Nylocas explosions",
            description = ""
    )
    default boolean nyloBlasts(){ return false; }

    @ConfigItem(
            position = 15,
            keyName = "nyloTimeAlive",
            name = "Nylocas time alive",
            description = ""
    )
    default boolean nyloTimeAlive(){ return false; }

    @ConfigItem(
            position = 16,
            keyName = "nyloRecolorMenu",
            name = "Nylocas recolor menu",
            description = ""
    )
    default boolean nyloRecolorMenu(){ return false; }

    @ConfigItem(
            position = 16,
            keyName = "nyloRecolorMenuBig",
            name = "Nylocas recolor big different",
            description = ""
    )
    default boolean nyloRecolorBigDifferent(){ return false; }

    @ConfigItem(
            position = 17,
            keyName = "nyloOverlay",
            name = "Nylocas overlay",
            description = ""
    )
    default boolean nyloOverlay(){ return false; }

    @ConfigItem(
        position = 18,
        keyName = "nyloAliveCounter",
        name = "Nylocas alive counter panel",
        description = ""
    )
    default boolean nyloAlivePanel(){ return false; }

    @ConfigItem(
        position = 19,
        keyName = "nyloAggressiveOverlay",
        name = "Highlight aggressive nylocas",
        description = ""
    )
    default boolean nyloAggressiveOverlay()
    {
        return false;
    }

    @ConfigItem(
        position = 20,
        keyName = "nyloInstanceTimer",
        name = "Nylo instance timer",
        description = ""
    )
    default boolean nyloInstanceTimer()
    {
        return false;
    }

    @ConfigItem(
            position = 21,
            keyName = "nyloStallMessage",
            name = "Nylo stall chat message",
            description = ""
    )
    default boolean nyloStallMessage()
    {
        return false;
    }

    @ConfigItem(
            position = 30,
            keyName = "sotetsegMaze1",
            name = "Sotetseg maze",
            description = ""
    )
    default boolean sotetsegMaze(){ return false; }

    @ConfigItem(
        position = 31,
        keyName = "sotetsegMazeDiscord",
        name = "Sotetseg maze send discord",
        description = ""
    )
    default boolean sotetsegMazeDiscord(){ return false; }

    @ConfigItem(
            position = 32,
            keyName = "SotetsegAttacks",
            name = "Sotetseg small attack orbs",
            description = ""
    )
    default boolean sotetsetAttacks() { return false; }

    @Range(min = 0, max = 100)
    @ConfigItem(
            position = 32,
            keyName = "SotetsegAttacksBrightness",
            name = "Sotetseg small attack orbs brightness",
            description = ""
    )
    default int sotetsetAttacksBrightness() { return 15; }

    @ConfigItem(
            position = 33,
            keyName = "SotetsegAttacks1",
            name = "Sotetseg big AOE orbs",
            description = ""
    )
    default boolean sotetsetAttacks1() { return true; }

    @ConfigItem(
            position = 34,
            keyName = "SotetsegAttacksSounds",
            name = "Sotetseg big AOE sound",
            description = ""
    )
    default boolean sotetsetAttacksSound() { return false; }

    @Range(max = 100)
    @ConfigItem(
            position = 35,
            keyName = "SotetsegAttacksSoundsVolume",
            name = "Sotetseg big AOE sound volume",
            description = ""
    )
    default int sotetsetAttacksSoundVolume() { return 80; }

    @ConfigItem(
            position = 36,
            keyName = "SotetsegAttacks1Animation",
            name = "Sotetseg big AOE animation",
            description = ""
    )
    default boolean sotetsetAttacksAnimation() { return true; }

    @Alpha
    @ConfigItem(
            position = 37,
            keyName = "SotetsegColor",
            name = "Sotetseg discord maze color",
            description = ""
    )
    default Color sotetsegDiscordMazeColor()
    {
        return Color.MAGENTA;
    }

    @ConfigItem(
            position = 42,
            keyName = "xarpusInstanceTimer",
            name = "Xarpus instance timer",
            description = ""
    )
    default boolean xarpusInstanceTimer(){ return false; }

    @ConfigItem(
            position = 43,
            keyName = "xarpusExhumed",
            name = "Xarpus exhumed",
            description = ""
    )
    default boolean xarpusExhumed(){ return false; }

    @ConfigItem(
            position = 44,
            keyName = "xarpusExhumedTimers",
            name = "Xarpus exhumed timers",
            description = ""
    )
    default boolean xarpusExhumedTimers(){ return false; }

    @ConfigItem(
            position = 45,
            keyName = "xarpusTickp2",
            name = "Xarpus tick p2",
            description = ""
    )
    default boolean xarpusTick1(){ return false; }

    @ConfigItem(
        position = 46,
        keyName = "xarpusTickp3",
        name = "Xarpus tick p3",
        description = ""
    )
    default boolean xarpusTick2(){ return false; }

    @ConfigItem(
            position = 56,
            keyName = "VerzikMeleeLocation",
            name = "Verzik Melee Location",
            description = ""
    )
    default boolean verzikMelee(){ return true; }

    @ConfigItem(
            position = 57,
            keyName = "VerzikRedHP",
            name = "Verzik Redcrab HP %",
            description = ""
    )
    default boolean verzikReds(){ return true; }

    @ConfigItem(
            position = 58,
            keyName = "VerzikCounter1",
            name = "Verzik tick counter",
            description = ""
    )
    default boolean verzikWheelchairMode1(){ return true; }

    @ConfigItem(
            position = 59,
            keyName = "VerzikCounter2",
            name = "Verzik attack counter",
            description = ""
    )
    default boolean verzikWheelchairMode2(){ return true; }

    @ConfigItem(
            position = 60,
            keyName = "VerzikCounter3",
            name = "Verzik total tick counter",
            description = ""
    )
    default boolean verzikWheelchairMode3(){ return true; }

    @ConfigItem(
            position = 61,
            keyName = "VerzikNyloAggro",
            name = "Verzik nylo aggro warning",
            description = ""
    )
    default boolean verzikNyloAggroWarning(){ return true; }

    @ConfigItem(
            position = 62,
            keyName = "VerzikNyloExplode",
            name = "Verzik nylo explode range",
            description = ""
    )
    default VerzikNyloSetting verzikNyloExplodeRange(){ return VerzikNyloSetting.EVERY_CRAB; }

    @ConfigItem(
            keyName = "highlightMelee",
            name = "",
            description = "",
            hidden = true
    )
    default boolean getHighlightMeleeNylo()
    {
        return false;
    }

    @ConfigItem(
        keyName = "highlightMelee",
        name = "",
        description = "",
        hidden = true
    )
    void setHighlightMeleeNylo(boolean set);

    @ConfigItem(
            keyName = "highlightMage",
            name = "",
            description = "",
            hidden = true
    )
    default boolean getHighlightMageNylo()
    {
        return false;
    }

    @ConfigItem(
        keyName = "highlightMage",
        name = "",
        description = "",
        hidden = true
    )
    void setHighlightMageNylo(boolean set);

    @ConfigItem(
            keyName = "highlightRange",
            name = "",
            description = "",
            hidden = true
    )
    default boolean getHighlightRangeNylo()
    {
        return false;
    }

    @ConfigItem(
        keyName = "highlightRange",
        name = "",
        description = "",
        hidden = true
    )
    void setHighlightRangeNylo(boolean set);

    enum BloatHandsSetting
    {
        DISABLED,
        NORMAL,
        RAVE
    }

    enum VerzikNyloSetting
    {
        DISABLED,
        AGGRO_CRAB,
        EVERY_CRAB
    }
}

package net.runelite.client.plugins.aoewarnings;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("aoewarnings")
public interface AoeWarningsConfig extends Config
{
    @ConfigItem(
            position = 2,
            keyName = "overlayColor",
            name = "Overlay Color",
            description = "Configures the color of the AoE Projectile Warnings overlay"
    )
    default Color overlayColor()
    {
        return new Color(0, 150, 200);
    }

    @Alpha
    @ConfigItem(
            position = 3,
            keyName = "overlayColorOutline",
            name = "Overlay Outline Color",
            description = "Configures the color of the outline of the AoE Projectile Warnings overlay"
    )
    default Color overlayOutlineColor()
    {
        return new Color(0, 150, 200);
    }

    @ConfigItem(
            keyName = "delay",
            name = "Fade Delay",
            description = "Configures the amount of time in milliseconds that the warning lingers for after the projectile has touched the ground",
            position = 4
    )
    default int delay()
    {
        return 300;
    }

    @ConfigItem(
            keyName = "fade",
            name = "Fade Warnings",
            description = "Configures whether or not AoE Projectile Warnings fade over time",
            position = 5
    )
    default boolean isFadeEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "tickTimers",
            name = "Tick Timers",
            description = "Configures whether or not AoE Projectile Warnings has tick timers overlaid as well.",
            position = 6
    )
    default boolean tickTimers()
    {
        return true;
    }

    @ConfigItem(
            keyName = "lizardmanaoe",
            name = "Lizardman Shamans",
            description = "Configures whether or not AoE Projectile Warnings for Lizardman Shamans is displayed",
            position = 13
    )
    default boolean isShamansEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "archaeologistaoe",
            name = "Crazy Archaeologist",
            description = "Configures whether or not AoE Projectile Warnings for Archaeologist is displayed",
            position = 16
    )
    default boolean isArchaeologistEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "icedemon",
            name = "Ice Demon",
            description = "Configures whether or not AoE Projectile Warnings for Ice Demon is displayed",
            position = 19
    )
    default boolean isIceDemonEnabled()
    {
        return true;
    }


    @ConfigItem(
            keyName = "vasa",
            name = "Vasa",
            description = "Configures whether or not AoE Projectile Warnings for Vasa is displayed",
            position = 22
    )
    default boolean isVasaEnabled()
    {
        return true;
    }


    @ConfigItem(
            keyName = "tekton",
            name = "Tekton",
            description = "Configures whether or not AoE Projectile Warnings for Tekton is displayed",
            position = 25
    )
    default boolean isTektonEnabled()
    {
        return true;
    }


    @ConfigItem(
            keyName = "vorkath",
            name = "Vorkath",
            description = "Configures whether or not AoE Projectile Warnings for Vorkath are displayed",
            position = 28
    )
    default boolean isVorkathEnabled()
    {
        return true;
    }


    @ConfigItem(
            keyName = "galvek",
            name = "Galvek",
            description = "Configures whether or not AoE Projectile Warnings for Galvek are displayed",
            position = 31
    )
    default boolean isGalvekEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "gargboss",
            name = "Gargoyle Boss",
            description = "Configs whether or not AoE Projectile Warnings for Dawn/Dusk are displayed",
            position = 34
    )
    default boolean isGargBossEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "vetion",
            name = "Vet'ion",
            description = "Configures whether or not AoE Projectile Warnings for Vet'ion are displayed",
            position = 37
    )
    default boolean isVetionEnabled()
    {
        return true;
    }


    @ConfigItem(
            keyName = "chaosfanatic",
            name = "Chaos Fanatic",
            description = "Configures whether or not AoE Projectile Warnings for Chaos Fanatic are displayed",
            position = 40
    )
    default boolean isChaosFanaticEnabled()
    {
        return true;
    }


    @ConfigItem(
            keyName = "olm",
            name = "Olm",
            description = "Configures whether or not AoE Projectile Warnings for The Great Olm are displayed",
            position = 43
    )
    default boolean isOlmEnabled()
    {
        return true;
    }


    @ConfigItem(
            keyName = "bombDisplay",
            name = "Olm Bombs",
            description = "Display a timer and colour-coded AoE for Olm's crystal-phase bombs.",
            position = 46
    )
    default boolean bombDisplay()
    {
        return true;
    }


    @ConfigItem(
            keyName = "lightning",
            name = "Olm Lightning Trails",
            description = "Show Lightning Trails",
            position = 49
    )
    default boolean LightningTrail()
    {
        return true;
    }


    @ConfigItem(
            keyName = "corp",
            name = "Corporeal Beast",
            description = "Configures whether or not AoE Projectile Warnings for the Corporeal Beast are displayed",
            position = 52
    )
    default boolean isCorpEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "wintertodt",
            name = "Wintertodt Snow Fall",
            description = "Configures whether or not AOE Projectile Warnings for the Wintertodt snow fall are displayed",
            position = 55
    )
    default boolean isWintertodtEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "isXarpusEnabled",
            name = "Xarpus",
            description = "Configures whether or not AOE Projectile Warnings for Xarpus are displayed",
            position = 58
    )
    default boolean isXarpusEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "addyDrags",
            name = "Addy Drags",
            description = "Show Bad Areas",
            position = 61
    )
    default boolean addyDrags()
    {
        return true;
    }

    @ConfigItem(
            keyName = "drake",
            name = "Drakes Breath",
            description = "Configures if Drakes Breath tile markers are displayed",
            position = 64
    )
    default boolean isDrakeEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "cerbFire",
            name = "Cerberus Fire",
            description = "Configures if Cerberus fire tile markers are displayed",
            position = 67
    )
    default boolean isCerbFireEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "demonicGorilla",
            name = "Demonic Gorilla",
            description = "Configures if Demonic Gorilla boulder tile markers are displayed",
            position = 70
    )
    default boolean isDemonicGorillaEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "nightmareSpores",
            name = "Nightmare Spores",
            description = "Configures if nightmare's spores are displayed",
            position = 71
    )
    default boolean isNightmareSporesEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "verzikcakes",
            name = "Verzik cupcakes",
            description = "Configures if verzik's cupcakes are displayed",
            position = 72
    )
    default boolean isVerzikCakesEnabled()
    {
        return true;
    }

    @ConfigItem(
        keyName = "verzikPoison",
        name = "Verzik poison splats",
        description = "Configures if verzik's poison splats of range attacks are displayed",
        position = 73
    )
    default boolean isVerzikPoisonEnabled()
    {
        return true;
    }

    @ConfigItem(
        keyName = "verzikRocks",
        name = "Verzik rocks p1",
        description = "Configures if rocks shadows are displayed",
        position = 74
    )
    default boolean isVerzikRocksEnabled()
    {
        return true;
    }
}

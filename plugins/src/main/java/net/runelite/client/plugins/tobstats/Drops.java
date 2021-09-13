package net.runelite.client.plugins.tobstats;

import lombok.Getter;

public enum Drops
{
    FACEGUARD(1, "Faceguard"),
    LEGGUARDS(2, "Legguards"),
    CHESTGUARD(3, "Chestguard"),
    HILT(4, "Avernic hilt"),
    SANGUINESTI_STAFF(5, "Sanguinesti staff"),
    RAPIER(6, "Rapier"),
    SCYTHE(7, "Scythe"),
    PET(8, "Lil'Zik");

    @Getter
    String name;

    @Getter
    int id;

    Drops(int id, String name)
    {
        this.id = id;
        this.name = name;
    }
}
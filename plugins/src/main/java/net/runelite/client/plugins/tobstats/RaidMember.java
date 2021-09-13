package net.runelite.client.plugins.tobstats;

import lombok.Data;

@Data
public class RaidMember
{
    private String name;
    private int raid_id;

    static final String UPDATE_VALUES_QUERY = "insert into Raid_Member (name, raid_id) values (:name, :raid_id)";

    public RaidMember(String name, int id)
    {
        this.name = name;
        this.raid_id = id;
    }
}

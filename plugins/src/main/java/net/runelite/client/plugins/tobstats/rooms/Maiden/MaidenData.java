package net.runelite.client.plugins.tobstats.rooms.Maiden;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.client.plugins.tobstats.RoomData;

@Data
@EqualsAndHashCode(callSuper = true)
public class MaidenData extends RoomData
{
    private Integer p1 = null;
    private Integer p2 = null;
    private Integer p3 = null;

    private int lowestBossHp = 1000;

    static final String UPDATE_VALUES_QUERY = "insert into Maiden (id, p1, p2, p3) values (:id, :p1, :p2, :p3)";
}

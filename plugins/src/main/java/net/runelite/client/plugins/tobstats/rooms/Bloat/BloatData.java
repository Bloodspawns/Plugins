package net.runelite.client.plugins.tobstats.rooms.Bloat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.client.plugins.tobstats.RoomData;

@Data
@EqualsAndHashCode(callSuper = true)
public class BloatData extends RoomData
{
    static final String UPDATE_VALUES_QUERY = "insert into Bloat (id) values (:id)";
}

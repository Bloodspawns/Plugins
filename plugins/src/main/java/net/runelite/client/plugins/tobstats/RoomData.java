package net.runelite.client.plugins.tobstats;

import lombok.Data;

@Data
public class RoomData
{
	protected int id;
	protected Integer millis = 0;
	protected int incomplete = 0;

	private static final String UPDATE_VALUES_QUERY = "insert into Room (millis, incomplete) values (:millis, :incomplete)";
}

package net.runelite.client.plugins.tobstats;

import lombok.Data;

@Data
public class Death
{
	protected int id;
	protected Integer room_id;
	protected Integer timestamp;
	protected Integer member_id;

	static final String UPDATE_VALUES_QUERY = "insert into Death (room_id, timestamp, member_id) values (:room_id, :timestamp, :member_id)";
}

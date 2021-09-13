package net.runelite.client.plugins.tobstats;

import lombok.Data;

@Data
public class Drop
{
	protected int id;
	protected Integer raid_id = null;
	protected Integer receiver_id = null;
	protected Integer value = null;

	static final String UPDATE_VALUES_QUERY = "insert into Rare_Drop (raid_id, receiver_id, value) values (:raid_id, :receiver_id, :value)";
}

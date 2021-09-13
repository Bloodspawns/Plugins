package net.runelite.client.plugins.tobstats;

import lombok.Data;

@Data
class Raid
{
	private int id;

	// rooms
	private Integer maiden_id = null;
	private Integer bloat_id = null;
	private Integer nylocas_id = null;
	private Integer sotetseg_id = null;
	private Integer xarpus_id = null;
	private Integer verzik_id = null;

	// general info
	private Integer mvp = null;
	private Integer party_size = 0;
	private Integer death_count = 0;
	private Integer spectator = 1;

	static final String UPDATE_VALUES_QUERY = "insert into Raid (maiden_id, bloat_id, nylocas_id, sotetseg_id, " +
			"xarpus_id, verzik_id, mvp, party_size, death_count, spectator) values (:maiden_id, :bloat_id, :nylocas_id, " +
			":sotetseg_id, :xarpus_id, :verzik_id, :mvp, :party_size, :death_count, :spectator)";
}

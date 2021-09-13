package net.runelite.client.plugins.tobstats;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarClientStrChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.tobstats.rooms.Bloat.Bloat;
import net.runelite.client.plugins.tobstats.rooms.Maiden.Maiden;
import net.runelite.client.plugins.tobstats.rooms.Nylocas.Nylocas;
import net.runelite.client.plugins.tobstats.rooms.Sotetseg.Sotetseg;
import net.runelite.client.plugins.tobstats.rooms.Verzik.Verzik;
import net.runelite.client.plugins.tobstats.rooms.Xarpus.Xarpus;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Theatre stats",
	description = "Statistics tracker",
	tags = {"tob","stats","statistics"},
	enabledByDefault = false
)
@Slf4j
public class TobstatsPlugin extends Plugin
{

	private static final int ROOM_STATE_VARBIT = 6447;
	private static final int RAID_STATE_VARBIT = 6440; //0:not in raid, 1:in party not in raid, 2:in raid/raid started, 3:only used between 2 and 0 for leaving??
	// PLAYER HEALTH ORBS
	// 0:null, 1:empty, 27:full hp, 30:dead, 31:not here/left raid

	@Inject
	private OverlayManager overlayManager;
	@Inject
	private TobstatsConfig config;
	@Inject
	private Client client;
	@Inject
	private ConfigManager configManager;
	@Inject
	private EventBus eventBus;

	@Inject
	private RoomOverlay roomOverlay;
	@Inject
	private Maiden maiden;
	@Inject
	private Bloat bloat;
	@Inject
	private Nylocas nylocas;
	@Inject
	private Sotetseg sotetseg;
	@Inject
	private Xarpus xarpus;
	@Inject
	private Verzik verzik;

	@Provides
    TobstatsConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TobstatsConfig.class);
	}

	private Raid raid = null;
	private ArrayList<RaidMember> raidMembers = new ArrayList<>();

	private Room[] rooms = null;
	private Room currentRoom = null;
	private int lastVarp6440 = -1;
	private int lastVarp6447 = -1;
	private boolean newRaid = true;
	private long currentRaidMillis = 0;

	@Override
	protected void startUp()
	{

		if (rooms == null)
		{
			rooms = new Room[]{maiden, bloat, nylocas, sotetseg, xarpus, verzik};
		}

		for (Room room : rooms)
		{
			eventBus.register(room);
		}

		overlayManager.add(roomOverlay);

		reset();
	}

	@Override
	protected void shutDown()
	{
		for (Room room : rooms)
		{
			eventBus.unregister(room);
		}

		overlayManager.remove(roomOverlay);
	}

	@Subscribe
	protected void onVarClientStrChanged(VarClientStrChanged varClientStrChanged)
	{
		int index = varClientStrChanged.getIndex();
		if (index >= 330 && index < 335)
		{
			if (raid == null)
			{
				log.warn("Trying to assign raid member but no raid is active");
				return;
			}
			Map<Integer, Object> varcmap = client.getVarcMap();
			String name = unfuckName(Optional.ofNullable(varcmap.get(index).toString()).orElse(""));
			if (!name.equals(""))
			{
				raidMembers.add(new RaidMember(name, raid.getId()));
				if (client.getLocalPlayer() != null)
				{
					String localname = unfuckName(Optional.ofNullable(client.getLocalPlayer().getName()).orElse(""));
					for (RaidMember raidMember : raidMembers)
					{
						if (raidMember.getName().equals(localname))
						{
							raid.setSpectator(0);
						}
					}
				}
			}

			if (raidMembers.size() > 0)
			{
				StringBuilder raiders = new StringBuilder();
				raidMembers.forEach(s -> raiders.append(s.getName()).append(", "));
				log.debug("Raiders: " + raiders.toString());
				log.debug("Spectate: " + raid.getSpectator());
			}
		}
	}

	@Subscribe
	protected void onVarbitChanged(VarbitChanged varbitChanged)
	{
		int varp6440 = client.getVarbitValue(client.getVarps(), RAID_STATE_VARBIT);
		int varp6447 = client.getVarbitValue(client.getVarps(), ROOM_STATE_VARBIT);

		if (varp6440 != lastVarp6440)
		{
			if (varp6440 > 1)
			{
				if (newRaid)
				{
					newRaid = false;
					int raid_id = 0;
					raid = new Raid();
					raid.setId(raid_id);
					raidMembers.clear();

					if (currentRoom != null && varp6447 > 0)
					{
						int incomplete = isRoomIncomplete(varp6440, varp6447);
						currentRoom.setIncomplete(incomplete);
						log.debug("incomplete " + incomplete + " " + varp6440 + " " + varp6447 + " " + (System.currentTimeMillis() - currentRaidMillis));
					}
				}

				roomOverlay.setHidden(false);
			}
			else
			{
				roomOverlay.setHidden(true);
				save();

				reset();
			}
		}

		if (varp6447 != lastVarp6447 && lastVarp6447 <= 0)
		{
			if (currentRoom != null)
			{
				int incomplete = isRoomIncomplete(varp6440, varp6447);
				currentRoom.setIncomplete(incomplete);
				log.debug("incomplete " + incomplete + " " + varp6440 + " " + varp6447 + " " + (System.currentTimeMillis() - currentRaidMillis));
			}
		}

		lastVarp6447 = varp6447;
		lastVarp6440 = varp6440;
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		boolean insideRaid = false;
		for (Room room : rooms)
		{
			if (room.isInRegion())
			{
				insideRaid = true;
				if (!room.equals(currentRoom))
				{
					room.reset();
					room.init();

					currentRoom = room;
					roomOverlay.setRoom(room);

					int varp6440 = client.getVarbitValue(client.getVarps(), 6440);
					int varp6447 = client.getVarbitValue(client.getVarps(), 6447);
					int incomplete = isRoomIncomplete(varp6440, varp6447);
					currentRoom.setIncomplete(incomplete);
					log.debug("incomplete " + incomplete + " " + varp6440 + " " + varp6447 + " " + (System.currentTimeMillis() - currentRaidMillis));
				}
			}
		}

		if (!insideRaid)
		{
			reset();
		}
	}

	private int isRoomIncomplete(int varp6440, int varp6447)
	{
		if (currentRaidMillis <= 0)
		{
			currentRaidMillis = System.currentTimeMillis();
			return 0;
		}
		if (varp6440 > 0 && varp6447 > 0 && System.currentTimeMillis() - currentRaidMillis < 1000)
		{
			return 1;
		}
		return 0;
	}

	private void reset()
	{
		for (Room room : rooms)
		{
			room.reset();
		}

		lastVarp6447 = -1;
		lastVarp6440 = -1;
		raid = null;
		newRaid = true;
		raidMembers.clear();
		currentRoom = null;
		currentRaidMillis = 0;
	}

	private void save()
	{

	}

	private static String unfuckName(String name)
	{
		return Text.removeTags(name).replace('\u00A0', ' ');
	}
}

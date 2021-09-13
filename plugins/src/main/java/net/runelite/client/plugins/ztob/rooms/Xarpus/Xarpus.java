package net.runelite.client.plugins.ztob.rooms.Xarpus;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.AlternateSprites;
import net.runelite.client.plugins.poison.PoisonPlugin;
import net.runelite.client.plugins.ztob.Room;
import net.runelite.client.plugins.ztob.TheatreConfig;
import net.runelite.client.plugins.ztob.TheatrePlugin;
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;

public class Xarpus extends Room
{
	private static BufferedImage EXHUMED_COUNT_ICON;
	private static final int GROUNDOBJECT_ID_EXHUMED = 32743;
	protected static final Set<Integer> P0_IDS = ImmutableSet.of(
		NpcID.XARPUS, NpcID.XARPUS_10766, NpcID.XARPUS_10770
	);
	protected static final Set<Integer> P1_IDS = ImmutableSet.of(
		NpcID.XARPUS_8339, NpcID.XARPUS_10767, NpcID.XARPUS_10771
	);
	protected static final Set<Integer> P2_IDS = ImmutableSet.of(
		NpcID.XARPUS_8340, NpcID.XARPUS_10768, NpcID.XARPUS_10772
	);
	protected static final Set<Integer> P3_IDS = ImmutableSet.of(
		NpcID.XARPUS_8341, NpcID.XARPUS_10769, NpcID.XARPUS_10773
	);

	@Inject
	private XarpusOverlay xarpusOverlay;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private Client client;

	private Counter counter;
	@Getter
	private boolean xarpusActive;
	@Getter
	private boolean xarpusStare;
	@Getter
	private final Map<GroundObject, Integer> xarpusExhumeds = new HashMap<>();
	@Getter
	private int xarpusTicksUntilAttack;
	@Getter
	private NPC xarpusNPC;

	@Getter
	private int instanceTimer = 0;
	@Getter
	private boolean isInstanceTimerRunning = false;
	private boolean nextInstance = true;

	@Getter
	private boolean isHM = false;
	@Getter
	private boolean isP3Active = false;

	@Inject
	protected Xarpus(TheatrePlugin plugin, TheatreConfig config)
	{
		super(plugin, config);
	}

	@Override
	public void init()
	{
		EXHUMED_COUNT_ICON = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.POISON_HEART), 26, 26);
	}

	@Override
	public void load()
	{
		overlayManager.add(xarpusOverlay);
	}

	@Override
	public void unload()
	{
		overlayManager.remove(xarpusOverlay);

		infoBoxManager.removeInfoBox(counter);
		counter = null;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		isHM = false;
		isP3Active = false;
		NPC npc = npcSpawned.getNpc();
		switch (npc.getId())
		{
			case NpcID.XARPUS_10770:
			case NpcID.XARPUS_10771:
			case NpcID.XARPUS_10772:
			case NpcID.XARPUS_10773:
				isHM = true;
			case NpcID.XARPUS:
			case NpcID.XARPUS_8339:
			case NpcID.XARPUS_8340:
			case NpcID.XARPUS_8341:
			case NpcID.XARPUS_10766:
			case NpcID.XARPUS_10767:
			case NpcID.XARPUS_10768:
			case NpcID.XARPUS_10769:
				xarpusActive = true;
				xarpusNPC = npc;
				xarpusStare = false;
				xarpusTicksUntilAttack = 9;
				break;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned) {
		NPC npc = npcDespawned.getNpc();
		switch (npc.getId())
		{
			case NpcID.XARPUS:
			case NpcID.XARPUS_8339:
			case NpcID.XARPUS_8340:
			case NpcID.XARPUS_8341:
			case NpcID.XARPUS_10766:
			case NpcID.XARPUS_10767:
			case NpcID.XARPUS_10768:
			case NpcID.XARPUS_10769:
			case NpcID.XARPUS_10770:
			case NpcID.XARPUS_10771:
			case NpcID.XARPUS_10772:
			case NpcID.XARPUS_10773:
				xarpusActive = false;
				xarpusNPC = null;
				xarpusStare = false;
				xarpusTicksUntilAttack = 9;
				xarpusExhumeds.clear();
				infoBoxManager.removeInfoBox(counter);
				counter = null;
				isInstanceTimerRunning = false;
				break;
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		if (xarpusActive)
		{
			GroundObject o = event.getGroundObject();
			if (o.getId() == GROUNDOBJECT_ID_EXHUMED)
			{
				if (counter == null)
				{
					counter = new Counter(EXHUMED_COUNT_ICON, plugin, 1);
					infoBoxManager.addInfoBox(counter);
				}
				else
				{
					counter.setCount(counter.getCount() + 1);
					isInstanceTimerRunning = false;
				}

				xarpusExhumeds.put(o, 11);
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (xarpusActive)
		{
			for (Iterator<GroundObject> it = xarpusExhumeds.keySet().iterator(); it.hasNext();)
			{
				GroundObject key = it.next();
				xarpusExhumeds.replace(key, xarpusExhumeds.get(key) - 1);
				if (xarpusExhumeds.get(key) < 0)
				{
					it.remove();
				}
			}
			if (xarpusNPC.getOverheadText() != null && !xarpusStare)
			{
				xarpusStare = true;
				xarpusTicksUntilAttack = 9;
			}
			if (xarpusStare)
			{
				xarpusTicksUntilAttack--;
				if (xarpusTicksUntilAttack <= 0)
				{
					xarpusTicksUntilAttack = 8;
					isP3Active = true;
				}
			}
			else if (P2_IDS.contains(xarpusNPC.getId()))
			{
				xarpusTicksUntilAttack--;

				if (xarpusTicksUntilAttack <= 0)
				{
					xarpusTicksUntilAttack = 4;
				}
			}
		}

		instanceTimer = (instanceTimer + 1) % 4;
	}


	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		nextInstance = true;
	}

	@Subscribe
	protected void onClientTick(ClientTick event)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}
		List<Player> players = client.getPlayers();
		for (Player player : players)
		{
			if (player.getWorldLocation() != null)
			{
				WorldPoint wpPlayer = player.getWorldLocation();
				LocalPoint lpPlayer = LocalPoint.fromWorld(client, wpPlayer.getX(), wpPlayer.getY());

				if (lpPlayer == null)
				{
					continue;
				}
				WorldPoint wpChest = WorldPoint.fromRegion(player.getWorldLocation().getRegionID(),17,5, player.getWorldLocation().getPlane());
				LocalPoint lpChest = LocalPoint.fromWorld(client, wpChest.getX(), wpChest.getY());
				if (lpChest != null)
				{
					Point point = new Point(lpChest.getSceneX() - lpPlayer.getSceneX(), lpChest.getSceneY() - lpPlayer.getSceneY());

					if (isInSotetsegRegion() && point.getY() == 1 && (point.getX() == 1 || point.getX() == 2 || point.getX() == 3) && nextInstance)
					{
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Xarpus instance timer started", "", false);
						instanceTimer = 2;
						isInstanceTimerRunning = true;
						nextInstance = false;
					}
				}
			}
		}
	}

	protected boolean isInXarpusRegion()
	{
		return client.getMapRegions() != null && client.getMapRegions().length > 0 && Arrays.stream(client.getMapRegions()).anyMatch(s -> s == 12612);
	}

	protected boolean isInSotetsegRegion()
	{
		return client.getMapRegions() != null && client.getMapRegions().length > 0 && Arrays.stream(client.getMapRegions()).anyMatch(s -> s == 13123 || s == 13379);
	}
}

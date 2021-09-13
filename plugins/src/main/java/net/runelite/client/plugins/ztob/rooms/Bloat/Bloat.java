package net.runelite.client.plugins.ztob.rooms.Bloat;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.NpcID;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.ztob.Room;
import net.runelite.client.plugins.ztob.RoomOverlay;
import net.runelite.client.plugins.ztob.TheatreConfig;
import net.runelite.client.plugins.ztob.TheatrePlugin;
import net.runelite.client.plugins.ztob.rooms.Nylocas.NylocasWave;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class Bloat extends Room
{
	protected static final Set<Integer> BLOAT_IDS = ImmutableSet.of(NpcID.PESTILENT_BLOAT,
		NpcID.PESTILENT_BLOAT_10812, NpcID.PESTILENT_BLOAT_10813);
	// THEATRE OF BLOOD
	protected static final int ROOM_STATE_VARBIT = 6447;
	protected int lastVarp6447 = 0;
	@Getter
	private boolean bloatActive;
	@Getter
	private NPC bloatNPC;
	@Getter
	private int bloatDownCount = 0;
	@Getter
	private int bloatUpTimer = 0;
	@Getter
	private int bloatState = 0;
	private Color[] colors = new Color[]
	{
			Color.BLUE,
			Color.YELLOW,
			Color.CYAN,
			Color.GREEN,
			Color.MAGENTA,
			Color.ORANGE,
			Color.RED,
			Color.PINK
	};
	@Getter
	private Color handColor = colors[0];
	private final Random colorGenerator = new Random();
	@Getter
	private final HashMap<WorldPoint, Integer> bloathands = new HashMap<>();

	@Inject
	private BloatOverlay bloatOverlay;
	@Inject
	private Client client;

	@Inject
	protected Bloat(TheatrePlugin plugin, TheatreConfig config)
	{
		super(plugin, config);
	}

	@Override
	public void load()
	{
		overlayManager.add(bloatOverlay);
	}

	@Override
	public void unload()
	{
		overlayManager.remove(bloatOverlay);

		bloatDownCount = 0;
		bloatState = 0;
		bloatUpTimer = 0;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		if (BLOAT_IDS.contains(npc.getId()))
		{
			bloatActive = true;
			bloatNPC = npc;
			bloatUpTimer = 0;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		if (BLOAT_IDS.contains(npc.getId()))
		{
			bloatActive = false;
			bloatNPC = null;
			bloatUpTimer = 0;
		}
	}

	@Subscribe
	protected void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectC)
	{
		if (bloatActive)
		{
			GraphicsObject graphicsObject = graphicsObjectC.getGraphicsObject();
			if (graphicsObject.getId() >= 1560 && graphicsObject.getId() <= 1590)
			{
				WorldPoint point = WorldPoint.fromLocal(client, graphicsObject.getLocation());
				if (!bloathands.containsKey(point))
				{
					bloathands.put(point, 4);
				}
			}
		}
	}

	@Subscribe
	protected void onVarbitChanged(VarbitChanged event)
	{
		if (!isInRegion())
		{
			return;
		}
		int varp6447 = client.getVarbitValue(client.getVarps(), ROOM_STATE_VARBIT);
		if (varp6447 != lastVarp6447)
		{
			if (varp6447 > 0)
			{
				bloatUpTimer = 0;
			}
		}
		lastVarp6447 = varp6447;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (bloatActive)
		{
			handColor = colors[colorGenerator.nextInt(colors.length)];

			bloatDownCount++;

			bloathands.values().removeIf(v -> v <= 0);
			bloathands.replaceAll((k, v) -> v - 1);

			if (bloatNPC.getAnimation() == -1) //1 = up; 2 = down; 3 = warn;
			{
				bloatDownCount = 0;
				bloatUpTimer++;
				if (bloatNPC.getHealthScale() == 0)
				{
					bloatState = 2;
				}
				else
				{
					bloatState = 1;
				}
			}
			else
			{
				if (25 < bloatDownCount && bloatDownCount < 35)
				{
					bloatState = 3;
				}
				else if (bloatDownCount < 26)
				{
					bloatUpTimer = 0;
					bloatState = 2;
				}
				else if (bloatNPC.getModelHeight() == 568)
				{
					bloatUpTimer = 0;
					bloatState = 2;
				}
				else
				{
					bloatState = 1;
				}
			}
		}
	}

	Polygon getBloatTilePoly()
	{
		if (bloatNPC == null)
		{
			return null;
		}

		int size = 1;
		NPCComposition composition = bloatNPC.getTransformedComposition();
		if (composition != null)
		{
			size = composition.getSize();
		}

		LocalPoint lp = null;

		switch (bloatState)
		{
			case 1:
				lp = bloatNPC.getLocalLocation();

				if (lp == null)
				{
					return null;
				}

				return RoomOverlay.getCanvasTileAreaPoly(client, lp, size, true);
			case 2:
			case 3:
				lp = LocalPoint.fromWorld(client, bloatNPC.getWorldLocation());

				if (lp == null)
				{
					return null;
				}

				return RoomOverlay.getCanvasTileAreaPoly(client, lp, size, false);
		}

		return null;
	}

	Color getBloatStateColor()
	{
		Color col = Color.CYAN;
		switch (bloatState)
		{
			case 2:
				col = Color.MAGENTA;
				break;
			case 3:
				col = Color.RED;
				break;
		}
		return col;
	}

	private boolean isInRegion()
	{
		return client.getMapRegions() != null && client.getMapRegions().length > 0 && Arrays.stream(client.getMapRegions()).anyMatch(s -> s == 13125);
	}
}

package net.runelite.client.plugins.ztob.rooms.Sotetseg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.party.messages.TilePing;
import net.runelite.client.plugins.ztob.Room;
import net.runelite.client.plugins.ztob.TheatreConfig;
import net.runelite.client.plugins.ztob.TheatrePlugin;
import net.runelite.client.ui.overlay.infobox.AnimatedInfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.ws.PartyService;
import net.runelite.client.ws.WSClient;

@Slf4j
public class Sotetseg extends Room
{
	private static final Set<Integer> SOTETSEG_ACTIVE = ImmutableSet.of(
		NpcID.SOTETSEG_8388, NpcID.SOTETSEG_10865, NpcID.SOTETSEG_10868);
	static final int SOTETSEG_MAGE_ORB = 1606;
	static final int SOTETSEG_RANGE_ORB = 1607;
	static final int SOTETSEG_BIG_AOE_ORB = 1604;
	private static final Set<Integer> GROUNDOBJECT_ID_REDMAZE = ImmutableSet.of(33035, 41750, 41751, 41752, 41753);
	private static final int GROUNDOBJECT_ID_BLACKMAZE = 33034;
	private static final int GROUNDOBJECT_ID_GREYMAZE = 33033;
	private static final int OVERWORLD_REGION_ID = 13123;
	private static final int UNDERWORLD_REGION_ID = 13379;
	@Getter
	private static final Point swMazeSquareOverWorld = new Point(9, 22);
	@Getter
	private static final Point swMazeSquareUnderWorld = new Point(42, 31);

	private boolean bigOrbPresent = false;
	private AnimatedInfoBox animatedInfoBox = null;
	private static Clip clip;

	static BufferedImage TACTICAL_NUKE_OVERHEAD;
	private static BufferedImage TACTICAL_NUKE_SHEET;
	private static BufferedImage TACTICAL_NUKE_SHEET_BLANK;
	private String currentTopic = null;

	@Getter
	private boolean sotetActive;
	private NPC sotetsegNPC;
	private int overWorldRegionID = -1;
	@Getter
	private boolean wasInUnderWorld = false;
	@Getter
	private final LinkedHashSet<Point> redTiles = new LinkedHashSet<>();
	@Getter
	private HashSet<Point> greenTiles = new HashSet<>();
	@Getter
	private final Set<WorldPoint> mazePings = Collections.synchronizedSet(new HashSet<>());

	@Inject
	private Client client;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private SotetsegOverlay sotetsegOverlay;
	@Inject
	private WSClient wsClient;
	@Inject
	private PartyService party;

	@Inject
	protected Sotetseg(TheatrePlugin plugin, TheatreConfig config)
	{
		super(plugin, config);
	}

	@Override
	public void init()
	{
		TACTICAL_NUKE_SHEET = ImageUtil.loadImageResource(TheatrePlugin.class, "nuke_spritesheet.png");
		TACTICAL_NUKE_OVERHEAD = ImageUtil.loadImageResource(TheatrePlugin.class, "Tactical_Nuke_Care_Package_Icon_MW2.png");
		TACTICAL_NUKE_SHEET_BLANK = new BufferedImage(TACTICAL_NUKE_SHEET.getWidth(), TACTICAL_NUKE_SHEET.getHeight(), TACTICAL_NUKE_SHEET.getType());
		Graphics2D graphics = TACTICAL_NUKE_SHEET_BLANK.createGraphics();
		graphics.setColor(new Color(0, 0, 0, 0));
		graphics.fillRect(0, 0, TACTICAL_NUKE_SHEET.getWidth(), TACTICAL_NUKE_SHEET.getHeight());
		graphics.dispose();

		try {
			AudioInputStream stream;
			AudioFormat format;
			DataLine.Info info;

			stream = AudioSystem.getAudioInputStream(new BufferedInputStream(TheatrePlugin.class.getResourceAsStream("mw2_tactical_nuke.wav")));
			format = stream.getFormat();
			info = new DataLine.Info(Clip.class, format);
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(stream);
			FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
			if (control != null)
			{
				control.setValue(20f * (float) Math.log10(config.sotetsetAttacksSoundVolume() / 100.0f));
			}
		}
		catch (Exception e) {
			clip = null;
		}
	}

	@Override
	public void load()
	{
		overlayManager.add(sotetsegOverlay);
	}

	@Override
	public void unload()
	{
		overlayManager.remove(sotetsegOverlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged change)
	{
		if (change.getKey().equals("SotetsegAttacksSoundsVolume"))
		{
			if (clip != null)
			{
				FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
				if (control != null)
				{
					control.setValue(20f * (float) Math.log10(config.sotetsetAttacksSoundVolume() / 100.0f));
				}
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		switch (npc.getId())
		{
			case NpcID.SOTETSEG:
			case NpcID.SOTETSEG_8388:
			case NpcID.SOTETSEG_10864:
			case NpcID.SOTETSEG_10865:
			case NpcID.SOTETSEG_10867:
			case NpcID.SOTETSEG_10868:
				sotetActive = true;
				sotetsegNPC = npc;
				break;
		}
	}

	private ArrayList<String> getRaiders()
	{
		ArrayList<String> raiders = new ArrayList<>();
		Map<Integer, Object> varcmap = client.getVarcMap();
		for (int i = 330; i < 335; i++)
		{
			if (varcmap.containsKey(i))
			{
				String name = varcmap.get(i).toString();
				if (name != null && !name.equals(""))
				{
					raiders.add(MazeCommunication.unfuckName(name));
				}
			}
		}
		return raiders;
	}

	private void parsePayload(byte[] bytes)
	{
		HashSet<Point> set = MazeCommunication.pointSetFromSeed(bytes);
		log.debug(Arrays.toString(set.toArray()));
		greenTiles = set;
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned) {
		NPC npc = npcDespawned.getNpc();
		switch (npc.getId())
		{
			case NpcID.SOTETSEG:
			case NpcID.SOTETSEG_8388:
			case NpcID.SOTETSEG_10864:
			case NpcID.SOTETSEG_10865:
			case NpcID.SOTETSEG_10867:
			case NpcID.SOTETSEG_10868:
				if (client.getPlane() != 3)
				{
					sotetActive = false;
					sotetsegNPC = null;
				}
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (sotetActive)
		{
			if (sotetsegNPC != null && SOTETSEG_ACTIVE.contains(sotetsegNPC.getId()))
			{
				if (!redTiles.isEmpty())
				{
					redTiles.clear();
				}

				if (!greenTiles.isEmpty())
				{
					greenTiles.clear();
				}

				if (!mazePings.isEmpty())
				{
					mazePings.clear();
				}

				if (isInOverWorld())
				{
					wasInUnderWorld = false;
					if (client.getLocalPlayer() != null && client.getLocalPlayer().getWorldLocation() != null)
					{
						overWorldRegionID = client.getLocalPlayer().getWorldLocation().getRegionID();
					}
				}
			}

			if (!redTiles.isEmpty() && wasInUnderWorld && config.sotetsegMazeDiscord())
			{
				for (Point p : redTiles)
				{
					WorldPoint wp = worldPointFromMazePoint(p);
					TilePing tilePing = new TilePing(wp, true);
					if (party != null && party.getLocalMember() != null)
					{
						tilePing.setMemberId(party.getLocalMember().getMemberId());
						wsClient.send(tilePing);
					}
				}
			}
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		if (sotetActive)
		{
			GroundObject o = event.getGroundObject();

			if (GROUNDOBJECT_ID_REDMAZE.contains(o.getId()))
			{
				Tile t = event.getTile();
				WorldPoint p = WorldPoint.fromLocal(client, t.getLocalLocation());
				Point point = new Point(p.getRegionX(), p.getRegionY());
				if (isInOverWorld())
				{
					redTiles.add(new Point(point.getX() - swMazeSquareOverWorld.getX(), point.getY() - swMazeSquareOverWorld.getY()));
				}
				if (isInUnderWorld())
				{
					redTiles.add(new Point(point.getX() - swMazeSquareUnderWorld.getX(), point.getY() - swMazeSquareUnderWorld.getY()));
					wasInUnderWorld = true;

					//#COMMUNICATION SET MAZE
					if (MazeCommunication.isMazeComplete(redTiles))
					{
						if (currentTopic != null && !currentTopic.equals(""))
						{
							String hexseed = MazeCommunication.getMazeSeed(redTiles);
						}
						else
						{
							log.debug("Invalid topic  + " + currentTopic + ".");
						}
					}
				}
			}
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (sotetActive && config.sotetsetAttacks1())
		{
			boolean foundBigOrb = false;
			for (Projectile p : client.getProjectiles())
			{
				if (p.getId() == SOTETSEG_BIG_AOE_ORB)
				{
					foundBigOrb = true;
					if (!bigOrbPresent)
					{
						if (config.sotetsetAttacksAnimation())
						{
							animatedInfoBox = new AnimatedInfoBox(
									TACTICAL_NUKE_SHEET, plugin, p,
									new Rectangle(32, 32), 32, 5);
						}
						else
						{
							animatedInfoBox = new AnimatedInfoBox(
									TACTICAL_NUKE_SHEET_BLANK, plugin, p,
									new Rectangle(32, 32), 32, 5);
						}
						infoBoxManager.addInfoBox(animatedInfoBox);

						if (clip != null && config.sotetsetAttacksSound())
						{
							clip.setFramePosition(0);
							clip.start();
						}
					}
					break;
				}
			}
			bigOrbPresent = foundBigOrb;
		}
		if (!bigOrbPresent)
		{
			infoBoxManager.removeInfoBox(animatedInfoBox);
		}
	}

	@Subscribe
	private void onTilePing(TilePing ping)
	{
		if (ping.isRetain())
		{
			mazePings.add(ping.getPoint());
		}
	}

	WorldPoint worldPointFromMazePoint(Point mazePoint)
	{
		if (overWorldRegionID == -1 && client.getLocalPlayer() != null)
		{
			return WorldPoint.fromRegion(
				client.getLocalPlayer().getWorldLocation().getRegionID(), mazePoint.getX() + Sotetseg.getSwMazeSquareOverWorld().getX(),
				mazePoint.getY() + Sotetseg.getSwMazeSquareOverWorld().getY(), 0);
		}
		return WorldPoint.fromRegion(
			overWorldRegionID, mazePoint.getX() + Sotetseg.getSwMazeSquareOverWorld().getX(),
			mazePoint.getY() + Sotetseg.getSwMazeSquareOverWorld().getY(), 0);
	}

	private boolean isInOverWorld()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == OVERWORLD_REGION_ID;
	}

	private boolean isInUnderWorld()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == UNDERWORLD_REGION_ID;
	}
}

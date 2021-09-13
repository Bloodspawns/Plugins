package net.runelite.client.plugins.ztob.rooms.Verzik;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.ztob.Room;
import net.runelite.client.plugins.ztob.TheatreConfig;
import net.runelite.client.plugins.ztob.TheatrePlugin;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Verzik extends Room
{
	enum Phase
	{
		PHASE1,
		PHASE2,
		PHASE3
	}

	enum SpecialAttack
	{
		WEB_COOLDOWN,
		WEBS,
		YELLOWS,
		GREEN,
		NONE
	}

	private static final Set<Integer> VERZIK_HM_ID = ImmutableSet.of(NpcID.VERZIK_VITUR_10847, NpcID.VERZIK_VITUR_10848,
		NpcID.VERZIK_VITUR_10849, NpcID.VERZIK_VITUR_10850, NpcID.VERZIK_VITUR_10851, NpcID.VERZIK_VITUR_10852,
		NpcID.VERZIK_VITUR_10853);
	private static final Set<Integer> NPC_WEB = ImmutableSet.of(NpcID.WEB, NpcID.WEB_10837, NpcID.WEB_10854);
	private static final Set<Integer> NPC_ID_TORNADO = ImmutableSet.of(8386, 10845, 10863);
	private static final Set<Integer> AGGRO_NYLOS = ImmutableSet.of(
		NpcID.NYLOCAS_ISCHYROS_8381, NpcID.NYLOCAS_TOXOBOLOS_8382, NpcID.NYLOCAS_HAGIOS_8383,
		NpcID.NYLOCAS_ISCHYROS_10841, NpcID.NYLOCAS_TOXOBOLOS_10842, NpcID.NYLOCAS_HAGIOS_10843,
		NpcID.NYLOCAS_ISCHYROS_10858, NpcID.NYLOCAS_TOXOBOLOS_10859, NpcID.NYLOCAS_HAGIOS_10860);
	private static final Set<Integer> RED_NYLOS = ImmutableSet.of(
		NpcID.NYLOCAS_MATOMENOS_8385, NpcID.NYLOCAS_MATOMENOS_10845, NpcID.NYLOCAS_MATOMENOS_10862);

	private static final int VERZIK_P1_MAGIC = 8109;
	private static final int VERZIK_P2_REG = 8114;
	private static final int VERZIK_P2_BOUNCE = 8116;
	private static final int VERZIK_ORGASM = 8117;
	private static final int p3_crab_attack_count = 5;
	private static final int p3_web_attack_count = 10;
	private static final int p3_yellow_attack_count = 15;
	private static final int p3_green_attack_count = 20;

	@Inject
	private VerzikOverlay verzikOverlay;

	@Getter
	private NPC verzikNPC;
	@Getter
	private boolean verzikActive;

	private final List<NPC> verzikTornados = new ArrayList<>();
	@Getter
	private final List<WorldPoint> verzikTornadoLocations = new ArrayList<>();
	@Getter
	private final List<WorldPoint> verzikTornadoTrailingLocations = new ArrayList<>();
	// npc, (hpRatio, hp)

	@Getter
	private final Map<NPC, Pair<Integer, Integer>> verzikReds = new HashMap<>();
	@Getter
	private final HashSet<NPC> verzikAggros = new HashSet<>();

	@Getter
	private int verzikTicksUntilAttack = -1;
	@Getter
	private int verzikTotalTicksUntilAttack = 0;

	@Getter
	private boolean verzikEnraged = false;
	private boolean verzikFirstEnraged = false;
	@Getter
	private int verzikAttackCount;
	private Phase verzikPhase;

	private boolean verzikTickPaused = true;
	private boolean verzikRedPhase = false;
	@Getter
	private SpecialAttack verzikSpecial = SpecialAttack.NONE;
	private int verzikLastAnimation = -1;

	private boolean isHM = false;

	@Inject
	private Verzik(TheatrePlugin plugin, TheatreConfig config)
	{
		super(plugin, config);
	}

	@Override
	public void load()
	{
		overlayManager.add(verzikOverlay);
	}

	@Override
	public void unload()
	{
		overlayManager.remove(verzikOverlay);
		verzikCleanup();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		if (NPC_ID_TORNADO.contains(npc.getId()))
		{
			verzikTornados.add(npc);
			if (!verzikEnraged)
			{
				verzikEnraged = true;
				verzikFirstEnraged = true;
			}
		}
		else if (AGGRO_NYLOS.contains(npc.getId()))
		{
			verzikAggros.add(npc);
		}
		else if (RED_NYLOS.contains(npc.getId()))
		{
			verzikReds.putIfAbsent(npc, new MutablePair<>(npc.getHealthRatio(), npc.getHealthScale()));
		}
		else if (NPC_WEB.contains(npc.getId()))
		{
			if (verzikNPC != null && verzikNPC.getInteracting() == null)
			{
				verzikSpecial = SpecialAttack.WEBS;
			}
		}
		else
		{
			switch (npc.getId())
			{
				case NpcID.VERZIK_VITUR_8369:
				case NpcID.VERZIK_VITUR_10830:
				case NpcID.VERZIK_VITUR_10847:
						verzikSpawn(npc);
					break;
				case NpcID.VERZIK_VITUR_8370:
				case NpcID.VERZIK_VITUR_10831:
				case NpcID.VERZIK_VITUR_10848:
				case NpcID.VERZIK_VITUR_8371:
				case NpcID.VERZIK_VITUR_10832:
				case NpcID.VERZIK_VITUR_10849:
					verzikPhase = Phase.PHASE1;
					verzikSpawn(npc);
					break;
				case NpcID.VERZIK_VITUR_8372:
				case NpcID.VERZIK_VITUR_10833:
				case NpcID.VERZIK_VITUR_10850:
				case NpcID.VERZIK_VITUR_8373:
				case NpcID.VERZIK_VITUR_10834:
				case NpcID.VERZIK_VITUR_10851:
					verzikPhase = Phase.PHASE2;
					verzikSpawn(npc);
					break;
				case NpcID.VERZIK_VITUR_8374:
				case NpcID.VERZIK_VITUR_10835:
				case NpcID.VERZIK_VITUR_10852:
				case NpcID.VERZIK_VITUR_8375:
				case NpcID.VERZIK_VITUR_10836:
				case NpcID.VERZIK_VITUR_10853:
					verzikPhase = Phase.PHASE3;
					verzikSpawn(npc);
					break;
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		if (NPC_ID_TORNADO.contains(npc.getId()))
		{
			verzikTornados.remove(npc);
		}
		else if (AGGRO_NYLOS.contains(npc.getId()))
		{
			verzikAggros.remove(npc);
		}
		else if (RED_NYLOS.contains(npc.getId()))
		{
			verzikReds.remove(npc);
		}
		else
		{
			switch (npc.getId())
			{
				case NpcID.VERZIK_VITUR_8369:
				case NpcID.VERZIK_VITUR_10830:
				case NpcID.VERZIK_VITUR_10847:
				case NpcID.VERZIK_VITUR_8370:
				case NpcID.VERZIK_VITUR_10831:
				case NpcID.VERZIK_VITUR_10848:
				case NpcID.VERZIK_VITUR_8371:
				case NpcID.VERZIK_VITUR_10832:
				case NpcID.VERZIK_VITUR_10849:
				case NpcID.VERZIK_VITUR_8372:
				case NpcID.VERZIK_VITUR_10833:
				case NpcID.VERZIK_VITUR_10850:
				case NpcID.VERZIK_VITUR_8373:
				case NpcID.VERZIK_VITUR_10834:
				case NpcID.VERZIK_VITUR_10851:
				case NpcID.VERZIK_VITUR_8374:
				case NpcID.VERZIK_VITUR_10835:
				case NpcID.VERZIK_VITUR_10852:
				case NpcID.VERZIK_VITUR_8375:
				case NpcID.VERZIK_VITUR_10836:
				case NpcID.VERZIK_VITUR_10853:
					verzikCleanup();
					break;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick eventuld )
	{
		if (verzikActive)
		{
			verzikTornadoTrailingLocations.clear();
			verzikTornadoTrailingLocations.addAll(verzikTornadoLocations);
			verzikTornadoLocations.clear();
			for (NPC nado : verzikTornados)
			{
				verzikTornadoLocations.add(nado.getWorldLocation());
			}

			Function<Integer, Integer> adjust_for_enrage = i -> isVerzikEnraged() ? i - 2 : i;

			if (verzikTickPaused)
			{
				switch (verzikNPC.getId())
				{
					case NpcID.VERZIK_VITUR_8370:
					case NpcID.VERZIK_VITUR_10831:
					case NpcID.VERZIK_VITUR_10848:
						verzikPhase = Phase.PHASE1;
						verzikAttackCount = 0;
						verzikTicksUntilAttack = 18;
						verzikTickPaused = false;
						break;
					case NpcID.VERZIK_VITUR_8372:
					case NpcID.VERZIK_VITUR_10833:
					case NpcID.VERZIK_VITUR_10850:
						verzikPhase = Phase.PHASE2;
						verzikAttackCount = 0;
						verzikTicksUntilAttack = 3;
						verzikTickPaused = false;
						break;
					case NpcID.VERZIK_VITUR_8374:
					case NpcID.VERZIK_VITUR_10835:
					case NpcID.VERZIK_VITUR_10852:
						verzikPhase = Phase.PHASE3;
						verzikAttackCount = 0;
						verzikTicksUntilAttack = 6;
						verzikTickPaused = false;
						break;
				}
			}
			else if (verzikSpecial == SpecialAttack.WEBS)
			{
				verzikTotalTicksUntilAttack++;

				if (verzikNPC.getInteracting() != null)
				{
					verzikSpecial = SpecialAttack.WEB_COOLDOWN;
					verzikAttackCount = 10;
					verzikTicksUntilAttack = 10;
					verzikFirstEnraged = false;
				}
			}
			else
			{
				verzikTicksUntilAttack = Math.max(0, verzikTicksUntilAttack - 1);
				verzikTotalTicksUntilAttack++;

				int animationID = verzikNPC.getAnimation();

				if (animationID > -1 && verzikPhase == Phase.PHASE1 && verzikTicksUntilAttack < 5 && animationID != verzikLastAnimation)
				{
					if (animationID == VERZIK_P1_MAGIC)
					{
						verzikTicksUntilAttack = 14;
						verzikAttackCount++;
					}
				}

				if (animationID > -1 && verzikPhase == Phase.PHASE2 && verzikTicksUntilAttack < 3 && animationID != verzikLastAnimation)
				{
					switch (animationID)
					{
						case VERZIK_P2_REG:
						case VERZIK_P2_BOUNCE:
							verzikTicksUntilAttack = 4;
							verzikAttackCount++;
							if (verzikAttackCount == 7 && verzikRedPhase)
							{
								verzikTicksUntilAttack = 8;
							}
							break;
						case VERZIK_ORGASM:
							verzikRedPhase = true;
							verzikAttackCount = 0;
							verzikTicksUntilAttack = 12;
							break;
					}
				}

				verzikLastAnimation = animationID;

				if (verzikPhase == Phase.PHASE3)
				{
					verzikAttackCount = verzikAttackCount % p3_green_attack_count;

					if (verzikTicksUntilAttack <= 0)
					{
						verzikAttackCount++;

						// first 9 including crabs
						if (verzikAttackCount < p3_web_attack_count)
						{
							verzikSpecial = SpecialAttack.NONE;
							verzikTicksUntilAttack = adjust_for_enrage.apply(7);
						}
						// between webs and yellows
						else if (verzikAttackCount < p3_yellow_attack_count)
						{
							verzikSpecial = SpecialAttack.NONE;
							verzikTicksUntilAttack = adjust_for_enrage.apply(7);
						}
						// yellow cant attack
						else if (verzikAttackCount < p3_yellow_attack_count + 1)
						{
							verzikSpecial = SpecialAttack.YELLOWS;
							if (isHM)
							{
								verzikTicksUntilAttack = 14 + 7 + 6;
							}
							else
							{
								verzikTicksUntilAttack = 14 + 7;
							}
						}
						// between yellow and green
						else if (verzikAttackCount < p3_green_attack_count)
						{
							verzikSpecial = SpecialAttack.NONE;
							verzikTicksUntilAttack = adjust_for_enrage.apply(7);
						}
						// ready for green
						else if (verzikAttackCount < p3_green_attack_count + 1)
						{
							verzikSpecial = SpecialAttack.GREEN;
							// 12 during purps?
							verzikTicksUntilAttack = 12;
						}
						else
						{
							verzikSpecial = SpecialAttack.NONE;
							verzikTicksUntilAttack = adjust_for_enrage.apply(7);
						}
					}

					if (verzikFirstEnraged)
					{
						verzikFirstEnraged = false;
						if (verzikSpecial != SpecialAttack.YELLOWS || verzikTicksUntilAttack <= 7)
						{
							verzikTicksUntilAttack = 5;
						}
					}
				}
			}
		}
	}

	Color verzikSpecialWarningColor()
	{
		Color col = Color.WHITE;
		if (verzikPhase != Phase.PHASE3)
		{
			return col;
		}
		switch (verzikAttackCount)
		{
			case Verzik.p3_crab_attack_count - 1:
				col = Color.MAGENTA;
				break;
			case Verzik.p3_web_attack_count - 1:
				col = Color.ORANGE;
				break;
			case Verzik.p3_yellow_attack_count - 1:
				col = Color.YELLOW;
				break;
			case Verzik.p3_green_attack_count - 1:
				col = Color.GREEN;
				break;
		}
		return col;
	}

	private void verzikSpawn(NPC npc)
	{
		isHM = VERZIK_HM_ID.contains(npc.getId());
		verzikEnraged = false;
		verzikRedPhase = false;
		verzikFirstEnraged = false;
		verzikTicksUntilAttack = 0;
		verzikAttackCount = 0;
		verzikNPC = npc;
		verzikActive = true;
		verzikTickPaused = true;
		verzikSpecial = SpecialAttack.NONE;
		verzikTotalTicksUntilAttack = 0;
		verzikLastAnimation = -1;
	}

	private void verzikCleanup()
	{
		verzikAggros.clear();
		verzikReds.clear();
		verzikTornadoLocations.clear();
		verzikTornadoTrailingLocations.clear();
		verzikEnraged = false;
		verzikFirstEnraged = false;
		verzikRedPhase = false;
		verzikActive = false;
		isHM = false;
		verzikTornados.clear();
		verzikNPC = null;
		verzikPhase = null;
		verzikTickPaused = true;
		verzikSpecial = SpecialAttack.NONE;
		verzikTotalTicksUntilAttack = 0;
		verzikLastAnimation = -1;
	}
}

/*
 * Copyright (c) 2019, Lucas <https://github.com/lucwousin>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.alchemicalhydra;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.alchemicalhydra.Hydra.AttackStyle;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Alchemical Hydra",
	description = "Show what to pray against hydra",
	tags = {"Hydra", "Lazy", "4 headed asshole"},
	enabledByDefault = false
)
@Slf4j
@Singleton
public class HydraPlugin extends Plugin
{
	private static final int[] HYDRA_REGIONS = {
		5279, 5280,
		5535, 5536
	};
	private static final int STUN_LENGTH = 7;

	@Getter(AccessLevel.PACKAGE)
	private Map<LocalPoint, Projectile> poisonProjectiles = new HashMap<>();

	@Getter(AccessLevel.PACKAGE)
	private Hydra hydra;

	private boolean inHydraInstance = false;
	private int lastAttackTick;

	@Inject
	private Client client;

	@Inject
	private HydraConfig config;

	@Inject
	private HydraOverlay overlay;

	@Inject
	private HydraSceneOverlay sceneOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Provides
    HydraConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HydraConfig.class);
	}

	@Override
	protected void startUp()
	{
		initConfig();

		inHydraInstance = checkArea();
		lastAttackTick = -1;
		poisonProjectiles.clear();
	}

	@Override
	protected void shutDown()
	{
		inHydraInstance = false;
		hydra = null;
		poisonProjectiles.clear();
		removeOverlays();
		lastAttackTick = -1;
	}

	private void initConfig()
	{
		this.overlay.setSafeCol(config.safeCol());
		this.overlay.setMedCol(config.medCol());
		this.overlay.setBadCol(config.badCol());
		this.sceneOverlay.setPoisonBorder(config.poisonBorderCol());
		this.sceneOverlay.setPoisonFill(config.poisonCol());
		this.sceneOverlay.setBadFountain(config.fountainColA());
		this.sceneOverlay.setGoodFountain(config.fountainColB());
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("betterHydra"))
		{
			return;
		}

		switch (event.getKey())
		{
			case "safeCol":
				overlay.setSafeCol(config.safeCol());
				return;
			case "medCol":
				overlay.setMedCol(config.medCol());
				return;
			case "badCol":
				overlay.setBadCol(config.badCol());
				return;
			case "poisonBorderCol":
				sceneOverlay.setPoisonBorder(config.poisonBorderCol());
				break;
			case "poisonCol":
				sceneOverlay.setPoisonFill(config.poisonCol());
				break;
			case "fountainColA":
				sceneOverlay.setBadFountain(config.fountainColA());
				break;
			case "fountainColB":
				sceneOverlay.setGoodFountain(config.fountainColB());
				break;
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged state)
	{
		if (state.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		inHydraInstance = checkArea();

		if (!inHydraInstance)
		{

			if (hydra != null)
			{
				removeOverlays();
				hydra = null;
			}
			return;
		}

		for (NPC npc : client.getNpcs())
		{
			if (npc.getId() == NpcID.ALCHEMICAL_HYDRA)
			{
				hydra = new Hydra(npc);
				break;
			}
		}

		addOverlays();
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		if (!inHydraInstance)
		{
			return;
		}
		if (event.getNpc().getId() != NpcID.ALCHEMICAL_HYDRA)
		{
			return;
		}

		hydra = new Hydra(event.getNpc());
		addOverlays();
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged animationChanged)
	{
		Actor actor = animationChanged.getActor();

		if (!inHydraInstance || hydra == null || actor == client.getLocalPlayer())
		{
			return;
		}

		HydraPhase phase = hydra.getPhase();

		if (actor.getAnimation() == phase.getDeathAnim2() &&
			phase != HydraPhase.THREE  // Else log's gonna say "Tried some weird shit"
			|| actor.getAnimation() == phase.getDeathAnim1() &&
			phase == HydraPhase.THREE) // We want the pray to switch ye ok ty
		{
			switch (phase)
			{
				case ONE:
					hydra.changePhase(HydraPhase.TWO);
					return;
				case TWO:
					hydra.changePhase(HydraPhase.THREE);
					return;
				case THREE:
					hydra.changePhase(HydraPhase.FOUR);
					return;
				case FOUR:
					hydra = null;
					poisonProjectiles.clear();
					removeOverlays();
					return;
			}
		}

		else if (actor.getAnimation() == phase.getSpecAnimationId() && phase.getSpecAnimationId() != 0)
		{
			hydra.setNextSpecial(hydra.getNextSpecial() + 9);
		}

		if (poisonProjectiles.isEmpty())
		{
			return;
		}

		Set<LocalPoint> exPoisonProjectiles = new HashSet<>();
		for (Entry<LocalPoint, Projectile> entry : poisonProjectiles.entrySet())
		{
			if (entry.getValue().getEndCycle() < client.getGameCycle())
			{
				exPoisonProjectiles.add(entry.getKey());
			}
		}
		for (LocalPoint toRemove : exPoisonProjectiles)
		{
			poisonProjectiles.remove(toRemove);
		}
	}

	@Subscribe
	private void onProjectileMoved(ProjectileMoved event)
	{
		if (!inHydraInstance || hydra == null
			|| client.getGameCycle() >= event.getProjectile().getStartMovementCycle())
		{
			return;
		}

		Projectile projectile = event.getProjectile();
		int id = projectile.getId();

		if (hydra.getPhase().getSpecProjectileId() != 0 && hydra.getPhase().getSpecProjectileId() == id)
		{
			if (hydra.getAttackCount() == hydra.getNextSpecial())
			{
				// Only add 9 to next special on the first poison projectile (whoops)
				hydra.setNextSpecial(hydra.getNextSpecial() + 9);
			}

			poisonProjectiles.put(event.getPosition(), projectile);
		}
		else if (client.getTickCount() != lastAttackTick
			&& (id == AttackStyle.MAGIC.getProjectileID() || id == AttackStyle.RANGED.getProjectileID()))
		{
			hydra.handleAttack(id);
			lastAttackTick = client.getTickCount();
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (event.getMessage().equals("The chemicals neutralise the Alchemical Hydra's defences!"))
		{
			hydra.setWeakened(true);
		}
		else if (event.getMessage().equals("The Alchemical Hydra temporarily stuns you."))
		{
			if (config.stun())
			{
				overlay.setStunTicks(STUN_LENGTH);
			}
		}
	}

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (overlay.getStunTicks() > 0)
		{
			overlay.setStunTicks(overlay.getStunTicks() - 1);
		}
	}

	private boolean checkArea()
	{
		return Arrays.equals(client.getMapRegions(), HYDRA_REGIONS) && client.isInInstancedRegion();
	}

	private void addOverlays()
	{
		if (config.counting() || config.stun())
		{
			overlayManager.add(overlay);
		}

		if (config.counting() || config.fountain())
		{
			overlayManager.add(sceneOverlay);
		}
	}

	private void removeOverlays()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(sceneOverlay);
	}
}

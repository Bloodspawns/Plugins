package net.runelite.client.plugins.zgauntlet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.inject.Inject;
import com.google.inject.Provides;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.HeadIcon;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
		name = "Gauntlet",
		description = "Gauntlet plugin",
		tags = {"gauntlet", "Gauntlet"},
		enabledByDefault = false
)

@Slf4j
public class GauntletPlugin extends Plugin
{
	enum AttackStyle
	{
		MAGE,
		RANGE
	}

	@Provides
	GauntletConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletConfig.class);
	}

	@Inject
	private GauntletOverlay overlay;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Client client;
	@Inject
	private SkillIconManager skillIconManager;
	@Inject
	private GauntletConfig config;
	@Inject
	private BossPrayerOverlay bossPrayerOverlay;
	@Inject
	private ResourceManager resourceManager;
	@Inject
	private ResourceOverlay resourceOverlay;
	@Inject
	private GauntletTimerOverlay gauntletTimerOverlay;

	@Getter
	private BufferedImage WOODCUTTING_ICON;
	@Getter
	private BufferedImage MINING_ICON;
	@Getter
	private BufferedImage CRAFTING_ICON;
	@Getter
	private BufferedImage FISHING_ICON;
	@Getter
	private BufferedImage HERBLORE_ICON;

	private static final int NADO_TICKS = 20;
	private static final int BOSS_VARBIT = 9177;
	private static final int GAUNTLET_VARBIT = 9178;
	private static final int HUNLEFF_ATTACK_ANIMATION_ID = 8419;
	private static final int HUNLEFF_NADO_ANIMATION_ID = 8418;
	private static final int HUNLEFF_RANGE_STAND_ID = 8755;
	private static final int HUNLEFF_MAGE_STAND_ID = 8754;
	private static final int GAUNTLET_MAP_REGION_ID = 7512;
	private static final int CORRUPTED_GAUNTLET_MAP_REGION_ID = 7768;
	private static final int BOW_ANIMATION_ID = 426;
	private static final int STAFF_ANIMATION_ID = 1167;
	private static final int HALBERD_ANIMATION_ID_428 = 428;
	private static final int HALBERD_ANIMATION_ID_440 = 440;
	private static final int TORCH_ANIMATION_ID = 401;
	private static final int PUNCH_ANIMATION_ID = 422;
	private static final int AXE_ANIMATION_ID = 395;
	private static final int PICKAXE_ANIMATION_ID = 400;
	private static final int HARPOON_ANIMATION_ID = 386;
	private static final int HARPOON_ANIMATION_ID_390 = 390;
	private static final int KICK_ANIMATION_ID = 423;

	private static final int TORNADO_NPC_ID = 9025;
	private static final int CORRUPTED_TORNADO_NPC_ID = 9039;
	/*
	private static final int MAGIC_PROJECTILE = 1707;
	private static final int RANGE_PROJECTILE = 1711;
	private static final int CORRUPTED_MAGIC_PROJECTILE = 1708;
	private static final int CORRUPTED_RANGE_PROJECTILE = 1712;
	private static final int MAGE_DISABLE_PRAYER_PROJECTILE = 1714;
	*/

	@Getter
	private AttackStyle currentHunleffStyle = AttackStyle.RANGE;
	private int lastHunleffAnimationID = -1;
	private int varbit9177;
	private int varbit9178;

	private boolean triggerEntry = false;
	@Getter
	private boolean isInBossRoom = false;
	@Getter
	private NPC hunleff;
	@Getter
	private int hunleffTickCounter = 3;
	@Getter
	private int hunleffAttackCounter = 0;
	@Getter
	private int playerAttackCounter = 0;
	@Getter
	private HashMap<NPC, Integer> tornados = new HashMap<>();
	@Getter
	private HashMap<InstancePoint, ResourceObject> resourceObjects = new HashMap<>();
	private HashMultiset<Integer> lastInventory = HashMultiset.create();

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		resetHunleff();
		WOODCUTTING_ICON = skillIconManager.getSkillImage(Skill.WOODCUTTING);
		MINING_ICON = skillIconManager.getSkillImage(Skill.MINING);
		HERBLORE_ICON = skillIconManager.getSkillImage(Skill.HERBLORE);
		CRAFTING_ICON = skillIconManager.getSkillImage(Skill.CRAFTING);
		FISHING_ICON = skillIconManager.getSkillImage(Skill.FISHING);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	private void resetHunleff()
	{
		hunleff = null;
		hunleffAttackCounter = 0;
		hunleffTickCounter = 3;
		playerAttackCounter = 0;
		bossPrayerOverlay.resetFields();
		currentHunleffStyle = AttackStyle.RANGE;
		bossPrayerOverlay.setShouldPray(HeadIcon.RANGED);
		lastHunleffAnimationID = -1;
		tornados.clear();
	}

	private void resetResources()
	{
		isInBossRoom = false;
		resourceObjects.clear();
		lastInventory.clear();
	}

	private void resetTime()
	{
		gauntletTimerOverlay.setStartOfGauntlet(null);
		gauntletTimerOverlay.setStartOfBoss(null);
	}

	@Subscribe
	protected void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getItemContainer() != client.getItemContainer(InventoryID.INVENTORY) && varbit9177 != 1 && varbit9178 == 1)
		{
			return;
		}

		Multiset<Integer> currentInventory = HashMultiset.create();
		Arrays.stream(event.getItemContainer().getItems())
				.forEach(item -> currentInventory.add(item.getId(), item.getQuantity()));

		final Multiset<Integer> diff = Multisets.difference(currentInventory, lastInventory);

		for (Multiset.Entry<Integer> item : diff.entrySet())
		{
			ResourceItem.ResourceItemType resourceItemType = ResourceItem.ResourceItemType.getLookupMap().get(item.getElement());
			if (item.getCount() > 0 && resourceItemType != null && resourceManager.getResources().containsKey(resourceItemType))
			{
				resourceManager.getResources().put(resourceItemType, Math.max(resourceManager.getResources().get(resourceItemType) - item.getCount(), 0));
			}
		}

		final ItemContainer itemContainer = event.getItemContainer();
		if (itemContainer != null)
		{
			lastInventory = HashMultiset.create();
			Arrays.stream(itemContainer.getItems())
					.forEach(item -> lastInventory.add(item.getId(), item.getQuantity()));
		}
	}

	@Subscribe
	protected void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		switch (npc.getId())
		{
			case TORNADO_NPC_ID:
			case CORRUPTED_TORNADO_NPC_ID:
				tornados.put(npc, NADO_TICKS);
				break;
			case NpcID.CRYSTALLINE_HUNLLEF:
			case NpcID.CRYSTALLINE_HUNLLEF_9022:
			case NpcID.CRYSTALLINE_HUNLLEF_9023:
			case NpcID.CRYSTALLINE_HUNLLEF_9024:
			case NpcID.CORRUPTED_HUNLLEF:
			case NpcID.CORRUPTED_HUNLLEF_9036:
			case NpcID.CORRUPTED_HUNLLEF_9037:
			case NpcID.CORRUPTED_HUNLLEF_9038:
				hunleff = npc;
				if (hunleff.getComposition() != null && hunleff.getComposition().getOverheadIcon() != null)
				{
					bossPrayerOverlay.setFirstPrayer(true);
				}
				break;
		}
	}

	@Subscribe
	protected void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		switch (npc.getId())
		{
			case TORNADO_NPC_ID:
			case CORRUPTED_TORNADO_NPC_ID:
				tornados.remove(npc);
				break;
		}
	}

	@Subscribe
	protected void onGameTick(GameTick event)
	{
		if (triggerEntry && isInGauntletRegion())
		{
			triggerEntry = false;
			if (client.getMapRegions() != null && client.getMapRegions().length > 0)
			{
				switch (client.getMapRegions()[0])
				{
					case GAUNTLET_MAP_REGION_ID:
						resourceManager.fillResourceMap(config, false);
						break;
					case CORRUPTED_GAUNTLET_MAP_REGION_ID:
						resourceManager.fillResourceMap(config, true);
						break;
				}
				overlayManager.add(resourceOverlay);
			}
		}

		if (hunleff != null && hunleff.getInteracting() != null
				&& hunleff.getInteracting().equals(client.getLocalPlayer()))
		{
			hunleffTickCounter--;
			if (hunleff.getAnimation() != -1 &&
					lastHunleffAnimationID != HUNLEFF_ATTACK_ANIMATION_ID &&
					lastHunleffAnimationID != HUNLEFF_NADO_ANIMATION_ID &&
					lastHunleffAnimationID != hunleff.getAnimation())
			{
				switch (hunleff.getAnimation())
				{
					case HUNLEFF_ATTACK_ANIMATION_ID:
					case HUNLEFF_NADO_ANIMATION_ID:
						hunleffTickCounter = 5;
						hunleffAttackCounter++;
						if (hunleffAttackCounter % 4 == 0)
						{
							hunleffAttackCounter = 0;
							switch (currentHunleffStyle)
							{
								case RANGE:
									currentHunleffStyle = AttackStyle.MAGE;
									bossPrayerOverlay.setShouldPray(HeadIcon.MAGIC);
									break;
								case MAGE:
									currentHunleffStyle = AttackStyle.RANGE;
									bossPrayerOverlay.setShouldPray(HeadIcon.RANGED);
									break;
							}
						}
						bossPrayerOverlay.setAttackCounter(hunleffAttackCounter);
						break;
				}
			}
			lastHunleffAnimationID = hunleff.getAnimation();
		}
		if (hunleff != null && hunleff.getComposition() != null && hunleff.getComposition().getOverheadIcon() != null
				&& hunleff.getComposition().getOverheadIcon() != bossPrayerOverlay.getHunleffOverhead())
		{
			bossPrayerOverlay.setHunleffOverhead(hunleff.getComposition().getOverheadIcon());
		}

		if (!tornados.isEmpty())
		{
			tornados.values().removeIf(v -> v <= 0);
			tornados.replaceAll((k, v) -> v - 1);
		}
	}

	@Subscribe
	protected void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() == hunleff && isInBossRoom)
		{
			switch (event.getActor().getAnimation())
			{
				case HUNLEFF_RANGE_STAND_ID:
					hunleffAttackCounter = 0;
					currentHunleffStyle = AttackStyle.RANGE;
					bossPrayerOverlay.setShouldPray(HeadIcon.RANGED);
					break;
				case HUNLEFF_MAGE_STAND_ID:
					hunleffAttackCounter = 0;
					currentHunleffStyle = AttackStyle.MAGE;
					bossPrayerOverlay.setShouldPray(HeadIcon.MAGIC);
					break;
			}
		}
		if (event.getActor() == client.getLocalPlayer() && isInBossRoom)
		{
			switch (event.getActor().getAnimation())
			{
				case BOW_ANIMATION_ID:
					updatePlayer(HeadIcon.RANGED);
					break;
				case STAFF_ANIMATION_ID:
					updatePlayer(HeadIcon.MAGIC);
					break;
				case HALBERD_ANIMATION_ID_428:
				case HALBERD_ANIMATION_ID_440:
				case PUNCH_ANIMATION_ID:
				case TORCH_ANIMATION_ID:
				case KICK_ANIMATION_ID:
				case HARPOON_ANIMATION_ID:
				case HARPOON_ANIMATION_ID_390:
				case AXE_ANIMATION_ID:
				case PICKAXE_ANIMATION_ID:
					updatePlayer(HeadIcon.MELEE);
					break;
			}
		}
	}

	private void updatePlayer(HeadIcon filter)
	{
		if (hunleff != null && hunleff.getComposition() != null
				&& hunleff.getComposition().getOverheadIcon() == filter)
		{
			return;
		}
		playerAttackCounter++;
		if (playerAttackCounter % 6 == 0)
		{
			playerAttackCounter = 0;
		}
	}

	@Subscribe
	protected void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		GameObject gameObject = gameObjectSpawned.getGameObject();
		ResourceObject.ResourceType type = ResourceObject.ResourceType.getLookupMap().get(gameObject.getId());
		if (type != null)
		{
			boolean depleted = !ResourceObject.ResourceType.getIdLookupMap().containsKey(gameObject.getId());
			WorldPoint wp = WorldPoint.fromLocal(client, gameObject.getLocalLocation());
			Point point = new Point(wp.getRegionX(), wp.getRegionY());
			InstancePoint instancePoint = new InstancePoint(point.getX(), point.getY(), wp.getRegionID(), wp.getPlane());
			ResourceObject resourceObject = new ResourceObject(instancePoint, type, depleted);
			resourceObjects.put(instancePoint, resourceObject);
		}
	}

	@Subscribe
	protected void onVarbitChanged(VarbitChanged event)
	{
		int newVarbit9177 = client.getVarbitValue(client.getVarps(), BOSS_VARBIT);
		int newVarbit9178 = client.getVarbitValue(client.getVarps(), GAUNTLET_VARBIT);
		if (isInGauntletRegion() && newVarbit9177 != 0 && newVarbit9177 != varbit9177)
		{
			isInBossRoom = true;
			bossPrayerOverlay.setFirstPrayer(false);
			overlayManager.remove(resourceOverlay);
			gauntletTimerOverlay.setStartOfBoss(Instant.now());
		}
		if (newVarbit9178 != varbit9178)
		{
			switch (newVarbit9178)
			{
				case 0:
					overlayManager.remove(bossPrayerOverlay);
					overlayManager.remove(resourceOverlay);
					overlayManager.remove(gauntletTimerOverlay);
					break;
				case 1:
					triggerEntry = true;
					overlayManager.add(bossPrayerOverlay);
					overlayManager.add(gauntletTimerOverlay);
					gauntletTimerOverlay.setStartOfGauntlet(Instant.now());
					break;
			}
		}
		varbit9178 = newVarbit9178;
		varbit9177 = newVarbit9177;

	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (shouldReset() && !isInGauntletRegion())
		{
			log.debug("Resetting gauntlet plugin collections.");
			resetHunleff();
			resetResources();
			resetTime();
		}
	}

	private boolean shouldReset()
	{
		return hunleff != null
				|| playerAttackCounter != 0
				|| hunleffAttackCounter != 0
				|| hunleffTickCounter != 3
				|| resourceObjects.size() > 0
				|| lastInventory.size() > 0
				|| gauntletTimerOverlay.getStartOfGauntlet() != null;
	}

	Color getHunleffAttackColor()
	{
		switch (currentHunleffStyle)
		{
			case MAGE:
				return Color.CYAN;
			case RANGE:
				return Color.GREEN;
			default:
				return Color.WHITE;
		}
	}

	Color getPlayerAttackColorOverwrite()
	{
		if (config.playerAttackCounter() && playerAttackCounter % 6 == 5)
		{
			return new Color(255, 150, 0);
		}
		return null;
	}

	BufferedImage getIcon(ResourceObject.ResourceType type)
	{
		switch (type)
		{
			case POND:
				return getFISHING_ICON();
			case ROCK:
				return getMINING_ICON();
			case TREE:
				return getWOODCUTTING_ICON();
			case PLANT:
				return getHERBLORE_ICON();
			case LINUM:
				// yes Linum whatever
				return getCRAFTING_ICON();
		}
		return null;
	}

	private boolean isInGauntletRegion()
	{
		return client.isInInstancedRegion() && client.getMapRegions().length > 0 &&
				(client.getMapRegions()[0] == GAUNTLET_MAP_REGION_ID || client.getMapRegions()[0] == CORRUPTED_GAUNTLET_MAP_REGION_ID);
	}
}

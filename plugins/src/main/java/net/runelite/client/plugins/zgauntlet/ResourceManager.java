package net.runelite.client.plugins.zgauntlet;

import java.util.HashMap;
import java.util.function.Consumer;
import javax.inject.Singleton;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

@Singleton
class ResourceManager
{
	@Getter
	private HashMap<ResourceItem.ResourceItemType, Integer> resources = new HashMap<>();

	void fillResourceMap(GauntletConfig config, boolean corrupted)
	{
		if (!config.resourceTracker())
		{
			return;
		}

		resources.clear();

		Consumer<Pair<Integer, GauntletItem>> handleOther = item ->
		{
			for (int i = 0; i < item.getLeft(); i++)
			{
				sumMap(resources, item.getRight().getResourceCounts());
			}
		};

		Consumer<GauntletConfig.GauntletTier> handleArmor = tier ->
		{
			GauntletItem item = null;
			switch (tier)
			{
				case TIER1:
					item = GauntletItem.GEAR_TIER1;
					break;
				case TIER2:
					item = GauntletItem.GEAR_TIER2;
					break;
				case TIER3:
					item = GauntletItem.GEAR_TIER3;
					break;
			}
			if (item != null)
			{
				sumMap(resources, item.getResourceCounts());
			}
		};

		Consumer<GauntletConfig.GauntletTier> handleArmorBody = tier ->
		{
			GauntletItem item = null;
			switch (tier)
			{
				case TIER1:
					item = GauntletItem.GEAR_TIER1;
					break;
				case TIER2:
					item = GauntletItem.GEAR_TIER2_BODY;
					break;
				case TIER3:
					item = GauntletItem.GEAR_TIER3_BODY;
					break;
			}
			if (item != null)
			{
				sumMap(resources, item.getResourceCounts());
			}
		};

		Consumer<GauntletConfig.GauntletTier> handleWeapon = tier ->
		{
			GauntletItem item = null;
			switch (tier)
			{
				case TIER1:
					item = GauntletItem.WEAPON_TIER1;
					break;
				case TIER2:
				case TIER3:
					item = GauntletItem.WEAPON_TIER2;
					break;
			}
			if (item != null)
			{
				sumMap(resources, item.getResourceCounts());
			}
		};

		if (corrupted)
		{
			handleOther.accept(Pair.of(config.corruptedTeleportCrystalCount(), GauntletItem.TELEPORT_CRYSTAL));
			handleOther.accept(Pair.of(config.corruptedPotionCount(), GauntletItem.POTION));
			handleOther.accept(Pair.of(config.corruptedFishCount(), GauntletItem.FISH));

			handleArmorBody.accept(config.corruptedBodyTier());
			handleArmor.accept(config.corruptedHelmetTier());
			handleArmor.accept(config.corruptedLegsTier());
			handleWeapon.accept(config.corruptedHalberdTier());
			handleWeapon.accept(config.corruptedStaffTier());
			handleWeapon.accept(config.corruptedBowTier());
			sumMap(resources, ResourceItem.ResourceItemType.SHARD, config.corruptedExtraShards());
		}
		else
		{
			handleOther.accept(Pair.of(config.teleportCrystalCount(), GauntletItem.TELEPORT_CRYSTAL));
			handleOther.accept(Pair.of(config.potionCount(), GauntletItem.POTION));
			handleOther.accept(Pair.of(config.fishCount(), GauntletItem.FISH));

			handleArmorBody.accept(config.bodyTier());
			handleArmor.accept(config.helmetTier());
			handleArmor.accept(config.legsTier());
			handleWeapon.accept(config.halberdTier());
			handleWeapon.accept(config.staffTier());
			handleWeapon.accept(config.bowTier());
			sumMap(resources, ResourceItem.ResourceItemType.SHARD, config.extraShards());
		}
	}

	private void sumMap(HashMap<ResourceItem.ResourceItemType, Integer> resources, final ResourceItem.ResourceItemType type, final int amount)
	{
		if (resources.containsKey(type))
		{
			resources.put(type, resources.get(type) + amount);
		}
		else
		{
			resources.put(type, amount);
		}
	}

	private void sumMap(HashMap<ResourceItem.ResourceItemType, Integer> resources, final HashMap<ResourceItem.ResourceItemType, Integer> hashMap)
	{
		for (ResourceItem.ResourceItemType key : hashMap.keySet())
		{
			sumMap(resources, key, hashMap.get(key));
		}
	}
}

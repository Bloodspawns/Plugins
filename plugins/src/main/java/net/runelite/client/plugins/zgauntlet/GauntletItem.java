package net.runelite.client.plugins.zgauntlet;

import java.util.HashMap;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;


import static net.runelite.client.plugins.zgauntlet.ResourceItem.ResourceItemType;

public enum GauntletItem
{
	TELEPORT_CRYSTAL(new ResourceItem(40, ResourceItemType.SHARD)),
	VIAL(new ResourceItem(10, ResourceItemType.SHARD)),
	WEAPON_TIER1(new ResourceItem(20, ResourceItemType.SHARD), new ResourceItem(1, ResourceItemType.WEAPON_FRAME)),
	WEAPON_TIER2(WEAPON_TIER1.getItems(), new ResourceItem(60, ResourceItemType.SHARD)),
	GEAR_TIER1(new ResourceItem(40, ResourceItemType.SHARD),
			new ResourceItem(1, ResourceItemType.ORE),
			new ResourceItem(1, ResourceItemType.BARK),
			new ResourceItem(1, ResourceItemType.COTTON)),
	GEAR_TIER2(GEAR_TIER1.getItems(), new ResourceItem(60, ResourceItemType.SHARD),
			new ResourceItem(1, ResourceItemType.ORE),
			new ResourceItem(1, ResourceItemType.BARK),
			new ResourceItem(1, ResourceItemType.COTTON)),
	GEAR_TIER2_BODY(GEAR_TIER1.getItems(), new ResourceItem(60, ResourceItemType.SHARD),
			new ResourceItem(2, ResourceItemType.ORE),
			new ResourceItem(2, ResourceItemType.BARK),
			new ResourceItem(2, ResourceItemType.COTTON)),
	GEAR_TIER3(GEAR_TIER2.getItems(), new ResourceItem(80, ResourceItemType.SHARD),
			new ResourceItem(2, ResourceItemType.ORE),
			new ResourceItem(2, ResourceItemType.BARK),
			new ResourceItem(2, ResourceItemType.COTTON)),
	GEAR_TIER3_BODY(GEAR_TIER2_BODY.getItems(), new ResourceItem(80, ResourceItemType.SHARD),
		new ResourceItem(2, ResourceItemType.ORE),
		new ResourceItem(2, ResourceItemType.BARK),
		new ResourceItem(2, ResourceItemType.COTTON)),
	POTION(VIAL.getItems(), new ResourceItem(10, ResourceItemType.SHARD), new ResourceItem(1, ResourceItemType.HERB)),
	FISH(new ResourceItem(1, ResourceItemType.FISH));

	@Getter
	private final ResourceItem[] items;

	GauntletItem(ResourceItem... items)
	{
		this.items = items;
	}

	GauntletItem(ResourceItem[] items, ResourceItem... items1)
	{
		this.items = ArrayUtils.addAll(items, items1);
	}

	HashMap<ResourceItemType, Integer> getResourceCounts()
	{
		HashMap<ResourceItemType, Integer> map = new HashMap<>();
		for (ResourceItem item : items)
		{
			if (map.containsKey(item.getResourceItemType()))
			{
				int old = map.get(item.getResourceItemType());
				map.put(item.getResourceItemType(), old + item.getAmount());
			} else
			{
				map.put(item.getResourceItemType(), item.getAmount());
			}
		}
		return map;
	}
}

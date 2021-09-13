package net.runelite.client.plugins.zgauntlet;

import java.util.HashMap;
import lombok.Getter;
import net.runelite.api.ItemID;

class ResourceItem
{
	enum ResourceItemType
	{
		SPIKE(ItemID.CRYSTAL_SPIKE, ItemID.CORRUPTED_SPIKE),
		ORB(ItemID.CRYSTAL_ORB, ItemID.CORRUPTED_ORB),
		STRING(ItemID.CRYSTALLINE_BOWSTRING, ItemID.CORRUPTED_BOWSTRING),
		SHARD(ItemID.CRYSTAL_SHARDS, ItemID.CORRUPTED_SHARDS),
		WEAPON_FRAME(ItemID.WEAPON_FRAME_23871, ItemID.WEAPON_FRAME),
		HERB(ItemID.GRYM_LEAF_23875, ItemID.GRYM_LEAF),
		ORE(ItemID.CRYSTAL_ORE, ItemID.CORRUPTED_ORE),
		COTTON(ItemID.LINUM_TIRINUM_23876, ItemID.LINUM_TIRINUM),
		FISH(ItemID.RAW_PADDLEFISH, ItemID.RAW_PADDLEFISH),
		BARK(ItemID.PHREN_BARK_23878, ItemID.PHREN_BARK);

		@Getter
		private final int itemID;
		@Getter
		private final int corruptedItemID;
		@Getter
		private static final HashMap<Integer, ResourceItemType> lookupMap;
		static
		{
			lookupMap = new HashMap<>();
			for (ResourceItemType item : ResourceItemType.values())
			{
				lookupMap.put(item.itemID, item);
				lookupMap.put(item.corruptedItemID, item);
			}
		}

		ResourceItemType(int itemID, int corruptedItemID)
		{
			this.itemID = itemID;
			this.corruptedItemID = corruptedItemID;
		}
	}

	@Getter
	private final int amount;
	@Getter
	private final ResourceItemType resourceItemType;

	ResourceItem(int amount, ResourceItemType resourceItemType)
	{
		this.resourceItemType = resourceItemType;
		this.amount = amount;
	}
}

package net.runelite.client.plugins.zgauntlet;

import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ObjectID;

@AllArgsConstructor
class ResourceObject
{
	enum ResourceType
	{
		TREE(new int[]{ ObjectID.PHREN_ROOTS, ObjectID.PHREN_ROOTS_36066 },
				new int[] { ObjectID.PHREN_ROOTS_DEPLETED, ObjectID.PHREN_ROOTS_DEPLETED_36067 },
				GauntletConfig::treeResourceColor,
				1),
		ROCK(new int[]{ ObjectID.CRYSTAL_DEPOSIT, ObjectID.CORRUPT_DEPOSIT },
				new int[]{ ObjectID.CRYSTAL_DEPOSIT_DEPLETED, ObjectID.CORRUPT_DEPOSIT_DEPLETED },
				GauntletConfig::rockResourceColor,
				1),
		PLANT(new int[]{ ObjectID.GRYM_ROOT, ObjectID.GRYM_ROOT_36070 },
				new int[]{ ObjectID.GRYM_ROOT_DEPLETED, ObjectID.GRYM_ROOT_DEPLETED_36071 },
				GauntletConfig::plantResourceColor,
				1),
		LINUM(new int[]{ ObjectID.LINUM_TIRINUM, ObjectID.LINUM_TIRINUM_36072 },
				new int[]{ ObjectID.LINUM_TIRINUM_DEPLETED, ObjectID.LINUM_TIRINUM_DEPLETED_36073 },
				GauntletConfig::linumResourceColor,
				1),
		POND(new int[]{ ObjectID.FISHING_SPOT_36068, ObjectID.FISHING_SPOT_35971 },
				new int[]{ ObjectID.FISHING_SPOT_DEPLETED, ObjectID.FISHING_SPOT_DEPLETED_36069 },
				GauntletConfig::fishResourceColor,
				2);

		@Getter
		private int[] ids;
		@Getter
		private int[] depletedIDs;
		@Getter
		private Function<GauntletConfig, Color> color;
		@Getter
		private int size;
		@Getter
		private static final HashMap<Integer, ResourceType> lookupMap;
		@Getter
		private static final HashMap<Integer, ResourceType> idLookupMap;

		static
		{
			lookupMap = new HashMap<>();
			idLookupMap = new HashMap<>();
			for (ResourceType v : ResourceType.values())
			{
				for (int id : v.ids)
				{
					lookupMap.put(id, v);
					idLookupMap.put(id, v);
				}
				for (int dID : v.depletedIDs)
				{
					lookupMap.put(dID, v);
				}
			}
		}

		ResourceType(int[] ids, int[] depletedIDs, Function<GauntletConfig, Color> color, int size)
		{
			this.ids = ids;
			this.depletedIDs = depletedIDs;
			this.color = color;
			this.size = size;
		}
	}

	@Getter
	private InstancePoint instancePoint;
	@Getter
	private ResourceType type;
	@Getter
	private boolean depleted;
}

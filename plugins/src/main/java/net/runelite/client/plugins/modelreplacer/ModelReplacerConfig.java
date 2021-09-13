package net.runelite.client.plugins.modelreplacer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("modelreplacer")
public interface ModelReplacerConfig extends Config
{
	@ConfigItem(
			position = 15,
			keyName = "modelsToReplace",
			name = "Model to replace",
			description = "The ids separated by commas of the models to replace"
	)
	default String modelsToReplace()
	{
		return "";
	}
}

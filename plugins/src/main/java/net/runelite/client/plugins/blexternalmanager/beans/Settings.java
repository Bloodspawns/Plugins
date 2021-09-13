package net.runelite.client.plugins.blexternalmanager.beans;

import com.google.common.collect.ArrayListMultimap;
import lombok.Value;

import java.util.Collection;
import java.util.Map;

@Value
public class Settings
{
	// manifest link, jar names
	Map<String, Collection<String>> installedPlugins;
}

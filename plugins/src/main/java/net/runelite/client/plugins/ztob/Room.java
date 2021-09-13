package net.runelite.client.plugins.ztob;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayManager;

@Singleton
public abstract class Room
{
	protected final TheatrePlugin plugin;
	protected final TheatreConfig config;

	@Inject
	protected OverlayManager overlayManager;
	// not adding overlay in this class in load because not every room should always have an overlay

	@Inject
	protected Room(TheatrePlugin plugin, TheatreConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}

	public void init()
	{
	}

	public void load()
	{
	}

	public void unload()
	{
	}
}

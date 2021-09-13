package net.runelite.client.plugins.ztob.rooms.Maiden;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.ztob.RoomOverlay;
import net.runelite.client.plugins.ztob.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayUtil;

public class MaidenOverlay extends RoomOverlay
{
	@Inject
	private Maiden maiden;
	@Inject
	private Client client;

	@Inject
	protected MaidenOverlay(TheatreConfig config)
	{
		super(config);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (maiden.isMaidenActive())
		{
			if (config.maidenBlood())
			{
				for (WorldPoint point : maiden.getMaidenBloodSplatters())
				{
					drawTile(graphics, point, new Color(0, 150, 200), 2, 150, 10);
				}
			}

			if (config.maidenSpawns())
			{
				for (WorldPoint point : maiden.getMaidenBloodSpawnLocations())
				{
					drawTile(graphics, point, new Color(0, 150, 200), 2, 180, 20);
				}
				for (WorldPoint point : maiden.getMaidenBloodSpawnTrailingLocations())
				{
					drawTile(graphics, point, new Color(0, 150, 200), 1, 120, 10);
				}
			}

			if (config.maidenTickCounter() && maiden.getMaidenNPC() != null && !maiden.getMaidenNPC().isDead())
			{
				String text = String.valueOf(maiden.getTicksUntilAttack());
				Point canvasPoint = maiden.getMaidenNPC().getCanvasTextLocation(graphics, text, 30);

				if (canvasPoint != null)
				{
					Color col = maiden.maidenSpecialWarningColor();
					OverlayUtil.renderTextLocation(graphics, canvasPoint, text, col);
				}
			}
		}
		return null;
	}
}

package net.runelite.client.plugins.ztob.rooms.Xarpus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.plugins.ztob.RoomOverlay;
import net.runelite.client.plugins.ztob.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayUtil;

public class XarpusOverlay extends RoomOverlay
{
	@Inject
	private Xarpus xarpus;

	@Inject
	protected XarpusOverlay(TheatreConfig config)
	{
		super(config);
	}

	public Dimension render(Graphics2D graphics)
	{
		if (xarpus.isInstanceTimerRunning() && xarpus.isInXarpusRegion() && config.xarpusInstanceTimer())
		{
			Player player = client.getLocalPlayer();
			if (player != null)
			{
				Point point = player.getCanvasTextLocation(graphics, "#", player.getLogicalHeight() + 60);
				if (point != null)
				{
					OverlayUtil.renderTextLocation(graphics, point, String.valueOf(xarpus.getInstanceTimer()), Color.CYAN);
				}
			}
		}

		if (xarpus.isXarpusActive())
        {
            NPC boss = xarpus.getXarpusNPC();

            boolean showp2 = config.xarpusTick1() && Xarpus.P2_IDS.contains(boss.getId());
            boolean p3exception = xarpus.isHM() && xarpus.isXarpusStare() && xarpus.isP3Active();
			boolean showp3 = config.xarpusTick2() && Xarpus.P3_IDS.contains(boss.getId()) && !p3exception;
            if (showp2 || showp3)
            {
                int tick = xarpus.getXarpusTicksUntilAttack();
                final String ticksLeftStr = String.valueOf(tick);
                Point canvasPoint = boss.getCanvasTextLocation(graphics, ticksLeftStr, 130);
                renderTextLocation(graphics, ticksLeftStr, Color.WHITE, canvasPoint);
            }

            if (Xarpus.P1_IDS.contains(boss.getId()))
            {
                for (GroundObject o : xarpus.getXarpusExhumeds().keySet())
                {
                	if (config.xarpusExhumed())
					{
						Polygon poly = o.getCanvasTilePoly();
						if (poly != null)
						{
							graphics.setColor(new Color(0, 255, 0, 130));
							graphics.setStroke(new BasicStroke(1));
							graphics.draw(poly);
						}
					}

                	if (config.xarpusExhumedTimers())
					{
						int tick = xarpus.getXarpusExhumeds().get(o);
						String text = String.valueOf(tick);
						Point textLocation = o.getCanvasTextLocation(graphics, text, 0);
						if (textLocation != null)
						{
							OverlayUtil.renderTextLocation(graphics, textLocation, text, Color.WHITE);
						}
					}
                }
            }
        }
		return null;
	}
}

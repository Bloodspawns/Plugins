package net.runelite.client.plugins.ztob.rooms.Sotetseg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import javax.inject.Inject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.ztob.RoomOverlay;
import net.runelite.client.plugins.ztob.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayUtil;

public class SotetsegOverlay extends RoomOverlay
{
	@Inject
	private Sotetseg sotetseg;
	@Inject
	private SkillIconManager iconManager;

	@Inject
	protected SotetsegOverlay(TheatreConfig config)
	{
		super(config);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (sotetseg.isSotetActive())
        {
            if (config.sotetsegMaze())
            {
            	int counter = 1;
                for (Point p : sotetseg.getRedTiles())
				{
					WorldPoint wp = sotetseg.worldPointFromMazePoint(p);
					drawTile(graphics, wp, Color.WHITE, 1, 255, 0);
					LocalPoint lp = LocalPoint.fromWorld(client, wp);
					if (lp != null && !sotetseg.isWasInUnderWorld())
					{
						Point textPoint = Perspective.getCanvasTextLocation(client, graphics, lp, String.valueOf(counter), 0);
						if (textPoint != null)
						{
							OverlayUtil.renderTextLocation(graphics, textPoint, String.valueOf(counter), Color.WHITE);
						}
					}
					counter++;
				}

				for (Point p : sotetseg.getGreenTiles())
				{
					WorldPoint wp = sotetseg.worldPointFromMazePoint(p);
					drawTile(graphics, wp, Color.GREEN, 1, 255, 0);
				}

				synchronized (sotetseg.getMazePings())
				{
					for (WorldPoint next : sotetseg.getMazePings())
					{
						final LocalPoint localPoint = LocalPoint.fromWorld(client, next);

						if (localPoint != null)
						{
							final Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);

							if (poly != null)
							{

								renderPolygon(graphics, poly, config.sotetsegDiscordMazeColor(), new Color(0, 0, 0, 20), 1);
							}
						}
					}
				}
            }

			if (config.sotetsetAttacks() || config.sotetsetAttacks1())
			{
				for (Projectile p : client.getProjectiles())
				{
					int id = p.getId();
					int x = (int)p.getX();
					int y = (int)p.getY();
					int z = (int)p.getZ();
					Point point = Perspective.localToCanvas(
						client, new LocalPoint(x, y), 0,
						Perspective.getTileHeight(client, new LocalPoint(x, y), p.getFloor()) - z);
					if (point == null)
					{
						continue;
					}
					if (id == Sotetseg.SOTETSEG_MAGE_ORB && config.sotetsetAttacks())
					{
						BufferedImage icon = iconManager.getSkillImage(Skill.MAGIC);
						icon = fadeImage(icon, (config.sotetsetAttacksBrightness() / 10.0f), 50);
						point = new Point(point.getX() - icon.getWidth() / 2, point.getY() - 30);
						OverlayUtil.renderImageLocation(graphics, point, icon);
					}
					if (id == Sotetseg.SOTETSEG_RANGE_ORB && config.sotetsetAttacks())
					{
						BufferedImage icon = iconManager.getSkillImage(Skill.RANGED);
						icon = fadeImage(icon, (config.sotetsetAttacksBrightness() / 10.0f), 50);
						point = new Point(point.getX() - icon.getWidth() / 2, point.getY() - 30);
						OverlayUtil.renderImageLocation(graphics, point, icon);
					}
					if (id == Sotetseg.SOTETSEG_BIG_AOE_ORB && config.sotetsetAttacks1())
					{
						Point txtPoint = new Point(point.getX(), point.getY());
						point = new Point(point.getX() - Sotetseg.TACTICAL_NUKE_OVERHEAD.getWidth() / 2, point.getY() - 60);
						OverlayUtil.renderImageLocation(graphics, point, Sotetseg.TACTICAL_NUKE_OVERHEAD);
						OverlayUtil.renderTextLocation(graphics, txtPoint, String.valueOf(p.getRemainingCycles() / 30), Color.WHITE);
					}
				}
			}
        }
		return null;
	}

	public static BufferedImage fadeImage(Image img, float fade, float target) {
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.drawImage(img, 0, 0, null);

		float offset = target * (1.0f - fade);
		float[] scales = { fade, fade, fade, 1.0f };
		float[] offsets = { offset, offset, offset, 0.0f };
		RescaleOp rop = new RescaleOp(scales, offsets, null);

		g.drawImage(bi, rop, 0, 0);

		g.dispose();
		return bi;
	}

	public static void renderPolygon(Graphics2D graphics, Shape poly, Color color, Color color2, int width)
	{
		graphics.setColor(color);
		final Stroke originalStroke = graphics.getStroke();
		graphics.setStroke(new BasicStroke(width));
		graphics.draw(poly);
		graphics.setColor(color2);
		graphics.fill(poly);
		graphics.setStroke(originalStroke);
	}
}

package net.runelite.client.plugins.zgauntlet;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import javax.annotation.Nonnull;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;


import static net.runelite.api.Constants.TILE_FLAG_BRIDGE;

public class GauntletOverlay extends Overlay
{
	private final GauntletPlugin plugin;
	private final GauntletConfig config;

	@Inject
	private Client client;

	@Inject
	private GauntletOverlay(GauntletPlugin plugin, GauntletConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

    @Override
    public Dimension render(Graphics2D graphics)
    {
    	if (plugin.getHunleff() != null)
		{
			int size = 1;
			NPCComposition composition = plugin.getHunleff().getTransformedComposition();
			if (composition != null)
			{
				size = composition.getSize();
			}
			LocalPoint lp = LocalPoint.fromWorld(client, plugin.getHunleff().getWorldLocation());
			if (lp != null)
			{
				Polygon tilePoly = getCanvasTileAreaPoly(client, lp, size, false);
				if (tilePoly != null)
				{
					Color color = plugin.getPlayerAttackColorOverwrite();
					if (color == null)
					{
						color = Color.MAGENTA;
					}
					graphics.setColor(color);
					graphics.setStroke(new BasicStroke(1));
					graphics.drawPolygon(tilePoly);
				}
			}

			for (NPC npc : plugin.getTornados().keySet())
			{
				lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
				if (config.nadoLocation() && lp != null)
				{
					Polygon tilePoly = getCanvasTileAreaPoly(client, lp, 1, false);
					if (tilePoly != null)
					{
						Color color = Color.WHITE;
						graphics.setColor(color);
						graphics.setStroke(new BasicStroke(1));
						graphics.drawPolygon(tilePoly);
					}
				}
				if (config.nadoTickCounter())
				{
					int tick = plugin.getTornados().get(npc);
					String text = String.valueOf(tick);
					Point textLocation = npc.getCanvasTextLocation(graphics, text, 100);
					if (textLocation != null)
					{
						OverlayUtil.renderTextLocation(graphics, textLocation, text, Color.WHITE);
					}
				}
			}
		}

    	if (config.hunleffAttackCounter() || config.hunleffTickCounter() || config.playerAttackCounter())
		{
			if (plugin.getHunleff() != null)
			{
				String line1 = "";
				String line2 = "";
				if (config.hunleffAttackCounter())
				{
					line1 += "Boss: " + plugin.getHunleffAttackCounter();
					if (config.hunleffTickCounter())
					{
						line1 += " : ";
					}
				}
				if (config.hunleffTickCounter())
				{
				    if (!config.hunleffAttackCounter())
                    {
                        line1 += "Tc: ";
                    }
					line1 += plugin.getHunleffTickCounter();
				}
				if (config.playerAttackCounter())
				{
					line2 += "You: " + plugin.getPlayerAttackCounter();
				}
				Point canvasPointCenter = plugin.getHunleff().getCanvasTextLocation(graphics, "", 120);
				if (canvasPointCenter != null)
				{
					Font lastFont = graphics.getFont();
					switch (config.fontTypeOverwrite())
					{
						case SMALL:
							break;
						case REGULAR:
							graphics.setFont(FontManager.getRunescapeFont());
							break;
						case BOLD:
							graphics.setFont(FontManager.getRunescapeBoldFont());
							break;
					}
					FontMetrics metrics = graphics.getFontMetrics();
					int width1 = metrics.stringWidth(line1);
					int width2 = metrics.stringWidth(line2);
					Point line1Point = new Point(canvasPointCenter.getX() - width1 / 2, canvasPointCenter.getY());
					Point line2Point = new Point(canvasPointCenter.getX() - width2 / 2,
							config.hunleffTickCounter() || config.hunleffAttackCounter() ? canvasPointCenter.getY() + metrics.getHeight() : canvasPointCenter.getY());
					OverlayUtil.renderTextLocation(graphics, line1Point, line1, plugin.getHunleffAttackColor());
					OverlayUtil.renderTextLocation(graphics, line2Point, line2, plugin.getPlayerAttackColorOverwrite() == null ? plugin.getHunleffAttackColor() : plugin.getPlayerAttackColorOverwrite());
					graphics.setFont(lastFont);
				}
			}
		}
		if (!plugin.isInBossRoom() && config.resourceTracker())
		{
			if (client.getLocalPlayer().getLocalLocation() != null)
			{
				WorldPoint wp = WorldPoint.fromLocal(client, client.getLocalPlayer().getLocalLocation());

				for (ResourceObject object : plugin.getResourceObjects().values())
				{
					if (client.getLocalPlayer().getWorldLocation() != null && (config.resourceDrawDistance() == 0 || object.getInstancePoint().distanceToWorldPoint(client, wp) <= config.resourceDrawDistance()))
					{
						WorldPoint objectWp = WorldPoint.fromRegion(object.getInstancePoint().getRegionID(), object.getInstancePoint().getX(), object.getInstancePoint().getY(), object.getInstancePoint().getPlane());
						LocalPoint lp = LocalPoint.fromWorld(client, objectWp);
						if (lp != null)
						{
							// i have a feeling region coords are always the most sw tile even though all order coords are?
							int size = object.getType().getSize();
							lp = new LocalPoint(lp.getX() - size / 2 * Perspective.LOCAL_TILE_SIZE, lp.getY() - size / 2 * Perspective.LOCAL_TILE_SIZE);

							Polygon poly = getCanvasTileAreaPoly(client, lp, size, false);
							if (poly != null)
							{
								OverlayUtil.renderPolygon(graphics, poly, object.isDepleted() ? Color.GRAY : object.getType().getColor().apply(config));
							}

							if (!object.isDepleted() && object.getInstancePoint().distanceToWorldPoint(client, wp) <= 25)
							{
								BufferedImage icon = plugin.getIcon(object.getType());
								if (icon != null)
								{
									Point point = Perspective.getCanvasImageLocation(client, lp, icon, 60);
									if (point != null)
									{
										OverlayUtil.renderImageLocation(graphics, point, icon);
									}
								}
							}
						}
					}
				}
			}
		}

        return null;
    }

	public static Polygon getCanvasTileAreaPoly(@Nonnull Client client, @Nonnull LocalPoint localLocation, int size, boolean centered)
	{
		return getCanvasTileAreaPoly(client, localLocation, size,0,  centered);
	}

	public static Polygon getCanvasTileAreaPoly(@Nonnull Client client, @Nonnull LocalPoint localLocation, int size, int borderOffset, boolean centered)
	{
		final int plane = client.getPlane();

		final int swX;
		final int swY;

		final int neX;
		final int neY;

		if (centered)
		{
			swX = localLocation.getX() - size * (Perspective.LOCAL_TILE_SIZE + borderOffset) / 2;
			swY = localLocation.getY() - size * (Perspective.LOCAL_TILE_SIZE + borderOffset) / 2;

			neX = localLocation.getX() + size * (Perspective.LOCAL_TILE_SIZE + borderOffset) / 2;
			neY = localLocation.getY() + size * (Perspective.LOCAL_TILE_SIZE + borderOffset) / 2;
		}
		else
		{
			swX = localLocation.getX() - (Perspective.LOCAL_TILE_SIZE + borderOffset) / 2;
			swY = localLocation.getY() - (Perspective.LOCAL_TILE_SIZE + borderOffset) / 2;

			neX = localLocation.getX() - (Perspective.LOCAL_TILE_SIZE + borderOffset) / 2 + size * (Perspective.LOCAL_TILE_SIZE + borderOffset);
			neY = localLocation.getY() - (Perspective.LOCAL_TILE_SIZE + borderOffset) / 2 + size * (Perspective.LOCAL_TILE_SIZE + borderOffset);
		}

		final int seX = swX;
		final int seY = neY;

		final int nwX = neX;
		final int nwY = swY;

		final byte[][][] tileSettings = client.getTileSettings();

		final int sceneX = localLocation.getSceneX();
		final int sceneY = localLocation.getSceneY();

		if (sceneX < 0 || sceneY < 0 || sceneX >= Perspective.SCENE_SIZE || sceneY >= Perspective.SCENE_SIZE)
		{
			return null;
		}

		int tilePlane = plane;
		if (plane < Constants.MAX_Z - 1 && (tileSettings[1][sceneX][sceneY] & TILE_FLAG_BRIDGE) == TILE_FLAG_BRIDGE)
		{
			tilePlane = plane + 1;
		}

		final int swHeight = getHeight(client, swX, swY, tilePlane);
		final int nwHeight = getHeight(client, nwX, nwY, tilePlane);
		final int neHeight = getHeight(client, neX, neY, tilePlane);
		final int seHeight = getHeight(client, seX, seY, tilePlane);

		Point p1 = Perspective.localToCanvas(client, swX, swY, swHeight);
		Point p2 = Perspective.localToCanvas(client, nwX, nwY, nwHeight);
		Point p3 = Perspective.localToCanvas(client, neX, neY, neHeight);
		Point p4 = Perspective.localToCanvas(client, seX, seY, seHeight);

		if (p1 == null || p2 == null || p3 == null || p4 == null)
		{
			return null;
		}

		Polygon poly = new Polygon();
		poly.addPoint(p1.getX(), p1.getY());
		poly.addPoint(p2.getX(), p2.getY());
		poly.addPoint(p3.getX(), p3.getY());
		poly.addPoint(p4.getX(), p4.getY());

		return poly;
	}

	private static int getHeight(@Nonnull Client client, int localX, int localY, int plane)
	{
		int sceneX = localX >> Perspective.LOCAL_COORD_BITS;
		int sceneY = localY >> Perspective.LOCAL_COORD_BITS;
		if (sceneX >= 0 && sceneY >= 0 && sceneX < Perspective.SCENE_SIZE && sceneY < Perspective.SCENE_SIZE)
		{
			int[][][] tileHeights = client.getTileHeights();

			int x = localX & (Perspective.LOCAL_TILE_SIZE - 1);
			int y = localY & (Perspective.LOCAL_TILE_SIZE - 1);
			int var8 = x * tileHeights[plane][sceneX + 1][sceneY] + (Perspective.LOCAL_TILE_SIZE - x) * tileHeights[plane][sceneX][sceneY] >> Perspective.LOCAL_COORD_BITS;
			int var9 = tileHeights[plane][sceneX][sceneY + 1] * (Perspective.LOCAL_TILE_SIZE - x) + x * tileHeights[plane][sceneX + 1][sceneY + 1] >> Perspective.LOCAL_COORD_BITS;
			return (Perspective.LOCAL_TILE_SIZE - y) * var8 + y * var9 >> Perspective.LOCAL_COORD_BITS;
		}

		return 0;
	}
}

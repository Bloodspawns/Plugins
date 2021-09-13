package net.runelite.client.plugins.raidsb.raidshelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class RaidsHelperOverlay extends Overlay
{
    private final Client client;
    private final RaidsHelperPlugin plugin;
    private final RaidsHelperConfig config;

    @Inject
    private RaidsHelperOverlay(final Client client, final RaidsHelperPlugin plugin, final RaidsHelperConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.olmHealingPools())
        {
            for (WorldPoint point : plugin.getOlmHeals())
            {
                drawTile(graphics, point, Color.CYAN);
            }
        }

        if (config.olmTpPortals())
        {
            for (WorldPoint point : plugin.getOlmTeleports())
            {
                client.setHintArrow(point);
                drawTile(graphics, point, Color.ORANGE);
            }
        }

        if (config.olmFallingCrystals())
        {
            for (WorldPoint point : plugin.getOlmCrystals().keySet())
            {
                drawTile(graphics, point, Color.RED);
            }
        }
        return null;
    }

    private void drawTile(Graphics2D graphics, WorldPoint point, Color color)
    {
        LocalPoint lp = LocalPoint.fromWorld(client, point);
        if (lp != null)
        {
            Polygon poly = Perspective.getCanvasTilePoly(client, lp);
            if (poly != null)
            {
                graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
                graphics.setStroke(new BasicStroke(1));
                graphics.draw(poly);
                graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 10));
                graphics.fill(poly);
            }
        }
    }
}

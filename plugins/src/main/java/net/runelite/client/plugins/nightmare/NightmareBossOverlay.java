package net.runelite.client.plugins.nightmare;

import com.google.inject.Inject;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;

import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Perspective;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class NightmareBossOverlay extends Overlay
{
    private final NightmareBossPlugin plugin;
    private final NightmareBossConfig config;

    @Inject
    private Client client;

    @Inject
    private NightmareBossOverlay(NightmareBossPlugin plugin, NightmareBossConfig config)
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
        if (config.nightmareHands())
        {
            for (GraphicsObject graphicsObject : client.getGraphicsObjects())
            {
                if (graphicsObject.getId() != 1767)
                {
                    continue;
                }
                Polygon poly = Perspective.getCanvasTilePoly(client, graphicsObject.getLocation());
                if (poly != null)
                {
                    renderPolygon(graphics, poly, config.handsColorOutline(), config.handsColorFill());
                }
            }
        }
        return null;
    }

    public static void renderPolygon(Graphics2D graphics, Shape poly, Color colorOutline, Color colorFill)
    {
        renderPolygon(graphics, poly, colorOutline, colorFill, new BasicStroke(1));
    }

    public static void renderPolygon(Graphics2D graphics, Shape poly, Color colorOutline, Color colorFill, Stroke borderStroke)
    {
        graphics.setColor(colorOutline);
        final Stroke originalStroke = graphics.getStroke();
        graphics.setStroke(borderStroke);
        graphics.draw(poly);
        graphics.setColor(colorFill);
        graphics.fill(poly);
        graphics.setStroke(originalStroke);
    }
}

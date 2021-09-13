package net.runelite.client.plugins.aoewarnings;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.client.plugins.ProjectileID;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class AoeWarningsOverlay extends Overlay
{
    private static final int BOMB_AOE = 7;
    private static final int BOMB_DETONATE_TIME = 8;
    private static final int FILL_START_ALPHA = 25;
    private static final int FALLING_ROCK_GRAPHIC_ID = 1436;

    private final Client client;
    private final AoeWarningsPlugin plugin;
    private final AoeWarningsConfig config;

    @Inject
    public AoeWarningsOverlay(final Client client, final AoeWarningsPlugin plugin, final AoeWarningsConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.isVerzikRocksEnabled())
        {
            client.getGraphicsObjects().forEach(g -> {
                if (g.getId() == FALLING_ROCK_GRAPHIC_ID)
                {
                    renderTile(graphics, new Color(0, 150, 200), WorldPoint.fromLocal(client, g.getLocation()));
                }
            });
        }

        if (config.LightningTrail())
        {
            plugin.getLightningTrail().forEach(o ->
                    renderTile(graphics, new Color(0, 150, 200), o));
        }

        if (config.isVerzikPoisonEnabled())
        {
            plugin.getVerzikAcidTrail().forEach(o ->
                renderTile(graphics, new Color(69, 241, 44), o.getWorldLocation()));
        }

        if (config.isOlmEnabled())
        {
            plugin.getOlmAcidTrail().forEach(o ->
                    renderTile(graphics, new Color(69, 241, 44), o.getWorldLocation()));
            plugin.getCrystalSpike().forEach(o ->
                    renderTile(graphics, new Color(255, 0, 84), o.getWorldLocation()));
        }

        if (config.isWintertodtEnabled())
        {
            plugin.getWintertodtSnowFall().forEach(o ->
                    renderTile(graphics, config.overlayColor(), o.getWorldLocation()));
        }

        if (config.bombDisplay())
        {
            plugin.getBombs().forEach((go, bomb) -> drawBomb(graphics, bomb));
        }

        if (config.isNightmareSporesEnabled())
        {
            plugin.getGameObjects().forEach((key, value) ->
            {
                final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, key.getLocalLocation(),value);
                if (tilePoly != null)
                {
                    Color c = config.overlayColor();
                    renderPolygon(graphics, tilePoly, config.overlayOutlineColor(), new Color(c.getRed(), c.getGreen(), c.getBlue(), 10), 1);
                }
            });
        }

        Instant now = Instant.now();
        HashMap<Projectile, ProjectileContainer> projectiles = plugin.getProjectiles();
        projectiles.forEach((v, proj) ->
        {
            if (proj.getTargetPoint() == null)
            {
                return;
            }

            if (!plugin.isConfigEnabledForProjectileId(v.getId()))
            {
                return;
            }

            Color color;

            if (now.isAfter(proj.getStartTime().plus(Duration.ofMillis(proj.getLifetime()))))
            {
                return;
            }

            if (proj.getProjectile().getId() == ProjectileID.ICE_DEMON_ICE_BARRAGE_AOE || proj.getProjectile().getId() == ProjectileID.TEKTON_METEOR_AOE)
            {
                if (client.getVar(Varbits.IN_RAID) == 0)
                {
                    return;
                }
            }

            final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, proj.getTargetPoint(), proj.getAoeProjectileInfo().getAoeSize());

            if (tilePoly == null)
            {
                return;
            }

            final double progress = (System.currentTimeMillis() - proj.getStartTime().toEpochMilli()) / (double) proj.getLifetime();

            final int tickProgress = proj.getFinalTick() - client.getTickCount();

            int fillAlpha;
            if (config.isFadeEnabled())
            {
                fillAlpha = (int) ((1 - progress) * FILL_START_ALPHA);
            }
            else
            {
                fillAlpha = FILL_START_ALPHA;
            }
            if (tickProgress == 0)
            {
                color = Color.RED;
            }
            else
            {
                color = Color.WHITE;
            }

            if (fillAlpha < 0)
            {
                fillAlpha = 0;
            }

            if (fillAlpha > 255)
            {
                fillAlpha = 255;
            }

            if (config.tickTimers() && tickProgress >= 0)
            {
                Point p = Perspective.localToCanvas(client, proj.getTargetPoint(), client.getPlane());
                if (p != null)
                {

                    OverlayUtil.renderTextLocation(graphics, p, Integer.toString(tickProgress), color);
                }
            }

            Color c = config.overlayColor();
            renderPolygon(graphics, tilePoly, config.overlayOutlineColor(), new Color(c.getRed(), c.getGreen(), c.getBlue(), fillAlpha), 1);

        });
        projectiles.entrySet().removeIf(p -> now.isAfter(p.getValue().getStartTime().plus(Duration.ofMillis(p.getValue().getLifetime()))));
        return null;
    }

    private void renderTile(Graphics2D graphics2D, Color color, WorldPoint worldPoint)
    {
        Shape poly = getTilePoly(worldPoint);
        if (worldPoint != null && poly != null)
        {
            renderPolygon(graphics2D, poly, color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 10), 1);
        }
    }

    private Shape getTilePoly(WorldPoint wp)
    {
        LocalPoint lp = LocalPoint.fromWorld(client, wp);
        if (lp == null)
        {
            return null;
        }

        return Perspective.getCanvasTilePoly(client, lp);
    }

    private void drawBomb(Graphics2D graphics, CrystalBomb bomb)
    {
        final LocalPoint localLoc = LocalPoint.fromWorld(client, bomb.getWorldLocation());
        final WorldPoint worldLoc = bomb.getWorldLocation();

        if (localLoc == null)
        {
            return;
        }
        final Polygon poly = Perspective.getCanvasTileAreaPoly(client, localLoc, BOMB_AOE);

        Color color = config.overlayColor();
        renderPolygon(graphics, poly, config.overlayOutlineColor(), new Color(color.getRed(), color.getGreen(), color.getBlue(), 10), 1);

        String time = String.valueOf(client.getTickCount() - bomb.getTickStarted());
        final Point canvasPoint = Perspective.localToCanvas(client, localLoc, worldLoc.getPlane());

        if (canvasPoint != null)
        {
            OverlayUtil.renderTextLocation(graphics, canvasPoint, time, Color.WHITE);
        }
    }

    private static void renderPolygon(Graphics2D graphics, Shape poly, Color color, Color color2, int width)
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

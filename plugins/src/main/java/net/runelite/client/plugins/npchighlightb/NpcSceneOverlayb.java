/*
 * Copyright (c) 2018, James Swindle <wilingua@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.npchighlightb;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.BLPerspective;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.util.Text;

public class NpcSceneOverlayb extends Overlay
{
	// Anything but white text is quite hard to see since it is drawn on
	// a dark background
	private static final Color TEXT_COLOR = Color.WHITE;

	private static final NumberFormat TIME_LEFT_FORMATTER = DecimalFormat.getInstance(Locale.US);

	static
	{
		((DecimalFormat)TIME_LEFT_FORMATTER).applyPattern("#0.0");
	}

	private final Client client;
	private final NpcIndicatorsConfigb config;
	private final NpcIndicatorsPluginb plugin;
	private ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	NpcSceneOverlayb(Client client, NpcIndicatorsConfigb config, NpcIndicatorsPluginb plugin, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showRespawnTimer())
		{
			plugin.getDeadNpcsToDisplay().forEach((id, npc) -> renderNpcRespawn(npc, graphics));
		}

		for (NPC npc : plugin.getHighlightedNpcs())
		{
			renderNpcOverlay(graphics, npc, config.getHighlightColor(), config.fillColor());
		}

		return null;
	}

	private void renderNpcRespawn(final MemorizedNpcb npc, final Graphics2D graphics)
	{
		if (npc.getPossibleRespawnLocations().isEmpty())
		{
			return;
		}

		final WorldPoint respawnLocation = npc.getPossibleRespawnLocations().get(0);
		final LocalPoint lp = LocalPoint.fromWorld(client, respawnLocation.getX(), respawnLocation.getY());

		if (lp == null)
		{
			return;
		}

		final Color color = config.getHighlightColor();

		final LocalPoint centerLp = new LocalPoint(
			lp.getX() + Perspective.LOCAL_TILE_SIZE * (npc.getNpcSize() - 1) / 2,
			lp.getY() + Perspective.LOCAL_TILE_SIZE * (npc.getNpcSize() - 1) / 2);

		final Polygon poly = Perspective.getCanvasTileAreaPoly(client, centerLp, npc.getNpcSize());

		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color);
		}

		final Instant now = Instant.now();
		final double baseTick = ((npc.getDiedOnTick() + npc.getRespawnTime()) - client.getTickCount()) * (Constants.GAME_TICK_LENGTH / 1000.0);
		final double sinceLast = (now.toEpochMilli() - plugin.getLastTickUpdate().toEpochMilli()) / 1000.0;
		final double timeLeft = Math.max(0.0, baseTick - sinceLast);
		final String timeLeftStr = TIME_LEFT_FORMATTER.format(timeLeft);

		final int textWidth = graphics.getFontMetrics().stringWidth(timeLeftStr);
		final int textHeight = graphics.getFontMetrics().getAscent();

		final Point canvasPoint = Perspective
			.localToCanvas(client, centerLp, respawnLocation.getPlane());

		if (canvasPoint != null)
		{
			final Point canvasCenterPoint = new Point(
				canvasPoint.getX() - textWidth / 2,
				canvasPoint.getY() + textHeight / 2);

			OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, timeLeftStr, TEXT_COLOR);
		}
	}

	private void renderNpcOverlay(Graphics2D graphics, NPC actor, Color color, Color fillColor)
	{
		NPCComposition npcComposition = actor.getTransformedComposition();
		if (npcComposition == null || !npcComposition.isInteractible()
			|| (actor.isDead() && config.ignoreDeadNpcs()))
		{
			return;
		}

		OverlayOptions options = plugin.getOverlayOptions(actor.getName());
		boolean anyOption = options.hull || options.worldTile || options.swTile || options.localTile || options.outline;
		boolean worldTile = (config.highlightWorldPositionTile() && !anyOption) || options.worldTile;
		boolean outline = (config.outlineNpc() && !anyOption) || options.outline;
		boolean hull = (config.highlightHull() && !anyOption) || options.hull;
		boolean localTile = (config.highlightTile() && !anyOption) || options.localTile;
		boolean swTile = (config.highlightSouthWestTile() && !anyOption) || options.swTile;

		if (worldTile)
		{
			int size = npcComposition.getSize();
			LocalPoint lp = LocalPoint.fromWorld(client, actor.getWorldLocation());
			if (lp != null)
			{
				Polygon tilePoly = BLPerspective.getCanvasTileAreaPoly(client, lp, size, false);
				if (tilePoly != null)
				{
					renderPoly(graphics, color, fillColor, tilePoly, config.tileBorderWidth());
				}
			}
		}

		if (outline)
		{
			modelOutlineRenderer.drawOutline(actor, config.outlineWidth3d(), color, config.outlineFeather3d());
		}

		if (hull)
		{
			Shape objectClickbox = actor.getConvexHull();
			renderPoly(graphics, color, fillColor, objectClickbox, config.tileBorderWidth());
		}

		if (localTile)
		{
			int size = npcComposition.getSize();
			LocalPoint lp = actor.getLocalLocation();
			Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);

			renderPoly(graphics, color, fillColor, tilePoly, config.tileBorderWidth());
		}

		if (swTile)
		{
			int size = npcComposition.getSize();
			LocalPoint lp = actor.getLocalLocation();

			int x = lp.getX() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);
			int y = lp.getY() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);

			Polygon southWestTilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));

			renderPoly(graphics, color, fillColor, southWestTilePoly, config.tileBorderWidth());
		}

		if (config.drawNames() && actor.getName() != null)
		{
			String npcName = Text.removeTags(actor.getName());
			Point textLocation = actor.getCanvasTextLocation(graphics, npcName, actor.getLogicalHeight() + 40);

			if (textLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, textLocation, npcName, color);
			}
		}
	}

	protected void renderPoly(Graphics2D graphics, Color borderColor, Color fillColor, Shape polygon, int width)
	{
		if (polygon != null)
		{
			graphics.setColor(borderColor);
			graphics.setStroke(new BasicStroke(width));
			graphics.draw(polygon);
			graphics.setColor(fillColor);
			graphics.fill(polygon);
		}
	}
}

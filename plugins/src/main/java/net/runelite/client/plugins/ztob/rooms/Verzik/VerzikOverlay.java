package net.runelite.client.plugins.ztob.rooms.Verzik;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.text.DecimalFormat;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.ztob.RoomOverlay;
import net.runelite.client.plugins.ztob.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class VerzikOverlay extends RoomOverlay
{
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

	@Inject
	private Verzik verzik;

	@Inject
	protected VerzikOverlay(TheatreConfig config)
	{
		super(config);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (verzik.isVerzikActive() && config.verzikMelee())
		{
			for (WorldPoint point : verzik.getVerzikTornadoLocations())
			{
				drawTile(graphics, point, new Color(0, 200, 255), 1, 120, 10);
			}
			for (WorldPoint point : verzik.getVerzikTornadoTrailingLocations())
			{
				drawTile(graphics, point, new Color(0, 200, 255), 2, 180, 20);
			}

			int size = 1;
			NPCComposition composition = verzik.getVerzikNPC().getTransformedComposition();
			if (composition != null)
			{
				size = composition.getSize();
			}
			LocalPoint lp = LocalPoint.fromWorld(client, verzik.getVerzikNPC().getWorldLocation());
			if (lp != null)
			{
				Polygon tilePoly = getCanvasTileAreaPoly(client, lp, size, false);
				if (tilePoly != null)
				{
					if (verzik.isVerzikEnraged())
					{
						renderPoly(graphics, new Color(255, 50, 25), tilePoly);
					}
					else
					{
						renderPoly(graphics, new Color(255, 0, 255), tilePoly);
					}
				}
			}
		}

		if (verzik.isVerzikActive())
		{
			verzik.getVerzikAggros().forEach(k ->
			{
				if (config.verzikNyloAggroWarning())
				{
					if (k.getInteracting() != null && !k.isDead())
					{
						Point textLocation = k.getCanvasTextLocation(graphics, k.getInteracting().getName(), 80);

						if (textLocation != null)
						{
							Color color = Color.GREEN;
							if (k.getInteracting().equals(client.getLocalPlayer()))
							{
								color = Color.YELLOW;
							}
							OverlayUtil.renderTextLocation(graphics, textLocation, k.getInteracting().getName(), color);
						}
					}
				}

				if (config.verzikNyloExplodeRange() == TheatreConfig.VerzikNyloSetting.EVERY_CRAB ||
						(config.verzikNyloExplodeRange() == TheatreConfig.VerzikNyloSetting.AGGRO_CRAB &&
								(client.getLocalPlayer() != null && client.getLocalPlayer().equals(k.getInteracting()))))
				{
					int size = 1;
					int thick_size = 1;
					NPCComposition composition = k.getTransformedComposition();
					if (composition != null)
					{
						size = composition.getSize() + 2 * thick_size;
					}
					LocalPoint lp = LocalPoint.fromWorld(client, k.getWorldLocation());
					if (lp != null)
					{
						// translate to middle
						lp = new LocalPoint(lp.getX() - thick_size * Perspective.LOCAL_TILE_SIZE, lp.getY() - thick_size * Perspective.LOCAL_TILE_SIZE);

						Polygon tilePoly = getCanvasTileAreaPoly(client, lp, size, false);
						if (tilePoly != null)
						{
							renderPoly(graphics, new Color(255, 0, 255), tilePoly);
						}
					}
				}
			});
		}

		if (verzik.isVerzikActive() && config.verzikReds())
		{
			verzik.getVerzikReds().forEach((k, v) ->
			{
				int v_health = v.getValue();
				int v_healthRation = v.getKey();
				if (k.getName() != null && k.getHealthScale() > 0)
				{
					v_health = k.getHealthScale();
					v_healthRation = Math.min(v_healthRation, k.getHealthRatio());
				}
				float percentage = ((float) v_healthRation / (float) v_health) * 100f;
				Point textLocation = k.getCanvasTextLocation(graphics, String.valueOf(DECIMAL_FORMAT.format(percentage)), 80);

				if (textLocation != null)
				{
					OverlayUtil.renderTextLocation(graphics, textLocation, String.valueOf(DECIMAL_FORMAT.format(percentage)), Color.WHITE);
				}
			});

			NPC[] reds = verzik.getVerzikReds().keySet().toArray(new NPC[0]);
			for (NPC npc : reds)
			{
				if (npc.getName() != null && npc.getHealthScale() > 0 && npc.getHealthRatio() < 100)
				{
					Pair<Integer, Integer> newVal = new MutablePair<>(npc.getHealthRatio(), npc.getHealthScale());
					if (verzik.getVerzikReds().containsKey(npc))
					{
						verzik.getVerzikReds().put(npc, newVal);
					}
				}
			}
		}

		if (verzik.isVerzikActive() && (config.verzikWheelchairMode1() || config.verzikWheelchairMode2() || config.verzikWheelchairMode3()))
		{
			String text = "";
			if (config.verzikWheelchairMode2() && verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS)
			{
				text += "Att " + verzik.getVerzikAttackCount();
				if (config.verzikWheelchairMode1() || config.verzikWheelchairMode3())
				{
					text += " : ";
				}
			}
			if (config.verzikWheelchairMode1() && verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS)
			{
				text += verzik.getVerzikTicksUntilAttack();
				if (config.verzikWheelchairMode3())
				{
					text += " : ";
				}
			}
			if (config.verzikWheelchairMode3())
			{
				text += "(" + verzik.getVerzikTotalTicksUntilAttack() + ")";
			}
			Point canvasPoint = verzik.getVerzikNPC().getCanvasTextLocation(graphics, text, 60);

			if (canvasPoint != null)
			{
				Color col = verzik.verzikSpecialWarningColor();
				OverlayUtil.renderTextLocation(graphics, canvasPoint, text, col);
			}
		}
		return null;
	}
}

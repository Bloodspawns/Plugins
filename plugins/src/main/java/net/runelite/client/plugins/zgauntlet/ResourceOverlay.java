package net.runelite.client.plugins.zgauntlet;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class ResourceOverlay extends Overlay
{
	private final ResourceManager resourceManager;
	private final GauntletConfig config;

	@Inject
	private ItemManager itemManager;

	private static final int SEPERATOR = 1;
	private static final int[] resourceIndices = new int[]{0, 0, 0, 1, 2, 3, 5, 6, 4, 7};

	@Inject
	private ResourceOverlay(ResourceManager resourceManager, GauntletConfig config)
	{
		this.config = config;
		this.resourceManager = resourceManager;
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.resourceTracker())
		{
			return null;
		}

		Rectangle bounds = new Rectangle();

		ArrayList<ResourceItem.ResourceItemType> sortedTypes = resourceManager.getResources().keySet().stream().
			sorted(Comparator.comparingInt(t -> resourceIndices[t.ordinal()]))
			.collect(Collectors.toCollection(ArrayList::new));

		// Determine bounds.
		int maxImageWidth = 0;
		for (ResourceItem.ResourceItemType resourceItemType : sortedTypes)
		{
			BufferedImage image = getIcon(resourceItemType);
			if (image == null || resourceManager.getResources().get(resourceItemType) <= 0)
			{
				continue;
			}

			maxImageWidth = Math.max(maxImageWidth, image.getWidth());

			FontMetrics fontMetrics = graphics.getFontMetrics();
			String text = String.valueOf(resourceManager.getResources().get(resourceItemType));

			if (config.verticalResourceOverlay())
			{
				bounds.height += SEPERATOR;
				bounds.height += image.getHeight();

				bounds.width = Math.max(bounds.width, SEPERATOR * 2 + image.getWidth() + fontMetrics.stringWidth(text));
			}
			else
			{
				bounds.width += SEPERATOR;
				bounds.width += image.getWidth();

				bounds.height = Math.max(bounds.height, image.getHeight() + fontMetrics.getHeight() + SEPERATOR * 2);
			}
		}

		if (config.verticalResourceOverlay())
		{
			bounds.height += SEPERATOR;
		}
		else
		{
			bounds.width += SEPERATOR;
		}

		// Draw the icons and text.
		int x = 0; int y = 0;
		for (ResourceItem.ResourceItemType resourceItemType : sortedTypes)
		{
			BufferedImage image = getIcon(resourceItemType);
			if (image == null || resourceManager.getResources().get(resourceItemType) <= 0)
			{
				continue;
			}

			FontMetrics fontMetrics = graphics.getFontMetrics();
			String text = String.valueOf(resourceManager.getResources().get(resourceItemType));

			if (config.verticalResourceOverlay())
			{
				Point textPoint = new Point(bounds.width - 2 * SEPERATOR - maxImageWidth - fontMetrics.stringWidth(text), y + SEPERATOR + image.getHeight() / 2);
				Point imagePoint = new Point(bounds.width - SEPERATOR - image.getWidth(), y + SEPERATOR);

				y += SEPERATOR + image.getHeight();

				OverlayUtil.renderTextLocation(graphics, textPoint, text, Color.WHITE);
				OverlayUtil.renderImageLocation(graphics, imagePoint, image);
			}
			else
			{
				Point textPoint = new Point(x + SEPERATOR + image.getWidth() / 2 - fontMetrics.stringWidth(text) / 2, bounds.height - SEPERATOR);
				Point imagePoint = new Point(x + SEPERATOR, y + SEPERATOR);

				x += SEPERATOR + image.getWidth();

				OverlayUtil.renderTextLocation(graphics, textPoint, text, Color.WHITE);
				OverlayUtil.renderImageLocation(graphics, imagePoint, image);
			}
		}

		return bounds.getSize();
	}

	private BufferedImage getIcon(ResourceItem.ResourceItemType resourceItemType)
	{
		//100 and false are to force the sprite of a stack of crystal shards.
		return itemManager.getImage(resourceItemType.getItemID(),100,false);
	}
}

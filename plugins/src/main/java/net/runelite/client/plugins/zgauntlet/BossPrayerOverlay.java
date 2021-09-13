package net.runelite.client.plugins.zgauntlet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.HeadIcon;
import net.runelite.api.Point;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.BackgroundComponent;

public class BossPrayerOverlay extends Overlay
{
	private static final int SEPERATOR = 2;
	private final GauntletConfig config;
	@Setter
	@Getter
	private HeadIcon shouldPray = null;
	@Setter
	@Getter
	private HeadIcon hunleffOverhead = null;
	@Setter
	@Getter
	private boolean firstPrayer = false;
	@Setter
	@Getter
	private int attackCounter = 0;

	@Inject
	private SpriteManager spriteManager;

	private BufferedImage meleePray;
	private BufferedImage magePray;
	private BufferedImage rangePray;

    @Inject
	private BossPrayerOverlay(GauntletConfig config)
	{
		this.config = config;
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

    @Override
    public Dimension render(Graphics2D graphics)
    {
    	if (!config.showPrayerPanel())
		{
			return null;
		}

    	Rectangle bounds = new Rectangle();

		FontMetrics metrics = graphics.getFontMetrics();

		String text = "Attack: " + attackCounter;
		if (firstPrayer)
		{
			text = "Overhead";
		}

		HeadIcon displayedStyle = firstPrayer ? hunleffOverhead : shouldPray;

		BufferedImage image;
		if (displayedStyle != null)
		{
			image = getOverheadSprite(displayedStyle);
			BufferedImage backgroundImage = getOverheadBackground();

			if (image == null || backgroundImage == null)
			{
				return null;
			}

			bounds.height += Math.max(image.getHeight(), backgroundImage.getHeight());
			bounds.width += Math.max(image.getWidth(), backgroundImage.getWidth());

			bounds.height += metrics.getHeight();
			bounds.width = Math.max(bounds.width, metrics.stringWidth(text));

			bounds.width += 2 * SEPERATOR;
			bounds.height += 4 * SEPERATOR;

			BackgroundComponent backgroundComponent = new BackgroundComponent();
			backgroundComponent.setRectangle(bounds);
			backgroundComponent.render(graphics);

			Point textLocation = new Point(bounds.x + SEPERATOR, bounds.y + bounds.height - 2 - metrics.getDescent());
			OverlayUtil.renderTextLocation(graphics, textLocation, text, Color.WHITE);

			graphics.drawImage(backgroundImage, bounds.x + bounds.width / 2 - backgroundImage.getWidth() / 2, bounds.y + 3,  null);
			graphics.drawImage(image, bounds.x + bounds.width / 2 - image.getWidth() / 2, bounds.y + 3 + backgroundImage.getHeight() / 2 - image.getHeight() / 2, null);
		}

        return bounds.getSize();
    }

    private BufferedImage getOverheadBackground()
	{
		return spriteManager.getSprite(SpriteID.ACTIVATED_PRAYER_BACKGROUND, 0 );
	}

    private BufferedImage getOverheadSprite(HeadIcon icon)
	{
		switch (icon)
		{
			case RANGED:
				if (rangePray == null)
				{
					rangePray = spriteManager.getSprite(SpriteID.PRAYER_PROTECT_FROM_MISSILES, 0);
				}
				return rangePray;
			case MELEE:
				if (meleePray == null)
				{
					meleePray = spriteManager.getSprite(SpriteID.PRAYER_PROTECT_FROM_MELEE, 0);
				}
				return meleePray;
			case MAGIC:
				if (magePray == null)
				{
					magePray = spriteManager.getSprite(SpriteID.PRAYER_PROTECT_FROM_MAGIC, 0);
				}
				return magePray;
		}
		return null;
	}

    void resetFields()
	{
		attackCounter = 0;
		hunleffOverhead = null;
		shouldPray = null;
		firstPrayer = false;
	}
}

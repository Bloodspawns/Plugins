package net.runelite.client.plugins.nightmare;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.BackgroundComponent;

public class BossPrayerOverlay extends Overlay
{
	private static final int SEPERATOR = 2;
	private final NightmareBossConfig config;
	private final NightmareBossPlugin plugin;

	@Inject
	private Client client;

	@Inject
	private SpriteManager spriteManager;

	private BufferedImage meleePray;
	private BufferedImage magePray;
	private BufferedImage rangePray;

    @Inject
	private BossPrayerOverlay(NightmareBossPlugin plugin, NightmareBossConfig config)
	{
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

    @Override
    public Dimension render(Graphics2D graphics)
    {
    	if (!config.nightmarePrayerOverlay())
		{
			return null;
		}

		if (Arrays.stream(client.getMapRegions()).noneMatch(i -> i == 15258))
		{
			return null;
		}

		BufferedImage image;
		Rectangle bounds = new Rectangle();
		if (plugin.getAttackStyle() != null)
		{
			image = getOverheadSprite(plugin.getAttackStyle());
			BufferedImage backgroundImage = getOverheadBackground();

			if (image == null || backgroundImage == null)
			{
				return null;
			}

			bounds.height += Math.max(image.getHeight(), backgroundImage.getHeight());
			bounds.width += Math.max(image.getWidth(), backgroundImage.getWidth());

			bounds.width += 2 * SEPERATOR;
			bounds.height += 4 * SEPERATOR;

			BackgroundComponent backgroundComponent = new BackgroundComponent();
			backgroundComponent.setRectangle(bounds);
			backgroundComponent.render(graphics);

			graphics.drawImage(backgroundImage, bounds.x + bounds.width / 2 - backgroundImage.getWidth() / 2, bounds.y + 3,  null);
			graphics.drawImage(image, bounds.x + bounds.width / 2 - image.getWidth() / 2, bounds.y + 3 + backgroundImage.getHeight() / 2 - image.getHeight() / 2, null);
		}

        return bounds.getSize();
    }

    private BufferedImage getOverheadBackground()
	{
		return spriteManager.getSprite(SpriteID.ACTIVATED_PRAYER_BACKGROUND, 0 );
	}

    private BufferedImage getOverheadSprite(ProtectPrayer protectPrayer)
	{
		switch (protectPrayer)
		{
			case RANGE:
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
			case MAGE:
				if (magePray == null)
				{
					magePray = spriteManager.getSprite(SpriteID.PRAYER_PROTECT_FROM_MAGIC, 0);
				}
				return magePray;
		}
		return null;
	}
}

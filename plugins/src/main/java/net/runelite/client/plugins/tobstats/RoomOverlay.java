package net.runelite.client.plugins.tobstats;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class RoomOverlay extends Overlay
{
    @Getter
    @Setter
    private Room room;
    protected TobstatsConfig config;
    @Getter
    protected final PanelComponent panelComponent = new PanelComponent();
    @Getter
    @Setter
    private boolean hidden = true;
    private final Font font;

    @Inject
    public RoomOverlay(TobstatsConfig config)
    {
        this.config = config;

        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.HIGH);

        font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (hidden || !config.timerOverlay())
        {
            return null;
        }

        Font font = graphics.getFont();
        if (font.equals(FontManager.getRunescapeFont()) || font.equals(FontManager.getRunescapeBoldFont()) || font.equals(FontManager.getRunescapeSmallFont()))
        {
            graphics.setFont(this.font);
        }

        if (room != null)
        {
            room.preRender(this);
            return panelComponent.render(graphics);
        }

        return null;
    }
}

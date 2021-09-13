package net.runelite.client.plugins.zgauntlet;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class GauntletTimerOverlay extends Overlay
{
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("mm:ss");
    private final PanelComponent panelComponent = new PanelComponent();
    private final GauntletConfig config;
    @Getter
    @Setter
    private Instant startOfGauntlet;
    @Getter
    @Setter
    private Instant startOfBoss;

    @Inject
    private GauntletTimerOverlay(GauntletPlugin plugin, GauntletConfig config)
    {
        super(plugin);
        this.config = config;
        setPosition(OverlayPosition.CANVAS_TOP_RIGHT);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.gauntletTimerOverlay() || startOfGauntlet == null)
        {
            return null;
        }

        panelComponent.getChildren().clear();

        Duration duration = Duration.between(startOfGauntlet, Instant.now());
        LocalTime localTime = LocalTime.ofSecondOfDay(duration.getSeconds());

        Instant end;
        if (startOfBoss == null)
        {
            end = Instant.now();
        }
        else
        {
            end = startOfBoss;
        }
        duration = Duration.between(startOfGauntlet, end);
        LocalTime localTimePrep = LocalTime.ofSecondOfDay(duration.getSeconds());

        if (startOfBoss == null)
        {
            duration = Duration.ZERO;
        }
        else
        {
            duration = Duration.between(startOfBoss, Instant.now());
        }
        LocalTime localTimeBoss = LocalTime.ofSecondOfDay(duration.getSeconds());

        panelComponent.getChildren().add(TitleComponent.builder().text("Gauntlet Timer").build());
        panelComponent.getChildren().add(LineComponent.builder().left("Total").right(localTime.format(dateTimeFormatter)).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Prep").right(localTimePrep.format(dateTimeFormatter)).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Boss").right(localTimeBoss.format(dateTimeFormatter)).build());

        return panelComponent.render(graphics);
    }
}

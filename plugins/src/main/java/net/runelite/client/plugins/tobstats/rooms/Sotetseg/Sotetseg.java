package net.runelite.client.plugins.tobstats.rooms.Sotetseg;

import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.AnimationChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.tobstats.Room;
import net.runelite.client.plugins.tobstats.RoomData;
import net.runelite.client.plugins.tobstats.RoomOverlay;
import net.runelite.client.plugins.tobstats.TobstatsConfig;
import net.runelite.client.plugins.tobstats.TobstatsPlugin;
import net.runelite.client.ui.overlay.components.LineComponent;

@Slf4j
public class Sotetseg extends Room
{
    private SotetsegData data;

    @Inject
    protected Sotetseg(TobstatsPlugin plugin, TobstatsConfig config)
    {
        super(plugin, config);
    }

    @Override
    protected boolean isInRegion()
    {
        return client.getMapRegions() != null && client.getMapRegions().length > 0 && Arrays.stream(client.getMapRegions()).anyMatch(s -> s == 13123 || s == 13379);
    }

    @Override
    protected void setIncomplete(Integer incomplete)
    {
        log.debug("Setting " + this.getClass().getSimpleName() + " incomplete to " + incomplete);
        data.setIncomplete(incomplete);
    }

    @Override
    protected RoomData getData()
    {
        return data;
    }

    @Override
    protected void init()
    {
        super.init();
        int id = 0;
        log.debug("Initializing " + this.getClass().getSimpleName() + " id " + id);
        data = new SotetsegData();
        data.setId(id);
    }

    @Override
    protected void reset()
    {
        super.reset();

        isActive = false;
        log.debug("resetting " + this.getClass().getSimpleName());
    }

    @Subscribe
    protected void onAnimationChanged(AnimationChanged animationChanged)
    {
        if (!isInRegion())
        {
            return;
        }
        int id = animationChanged.getActor().getAnimation();
        if (id == 1816)
        {
            int millis = getCurrentMillis();
            if (data.getP1() == null)
            {
                data.setP1(millis);
                printTimeToChat(millis, null, "Sotetseg phase 1", true, config.precisionTimers());
            }
            else if (data.getP2() == null)
            {
                if (millis > data.getP1() + 6000)
                {
                    data.setP2(millis);
                    printTimeToChat(millis, data.getP1(), "Sotetseg phase 2", true, config.precisionTimers());
                }
            }
        }
    }

    @Override
    protected void preRender(RoomOverlay roomOverlay)
    {
        int millis = getCurrentMillis();
        String ttime = millisToSplit(millis,  !isActive && data.getP2() != null ? data.getP2() : millis, false) +
                millisToTime(millis, true, config.precisionTimers());
        LineComponent total = LineComponent.builder().left("Sotetseg").right(ttime).build();

        int p1Time = data.getP1() != null ? data.getP1() : 0;
        LineComponent p1 = LineComponent.builder().left("Phase 1").right(millisToTime(p1Time, false, false)).build();

        int p2time = data.getP2() != null ? data.getP2() : 0;
        LineComponent p2 = LineComponent.builder().left("Phase 2").right(millisToSplit(p2time, p1Time, false) +
                millisToTime(p2time, false, false)).build();

        if (config.simpleTimerOverlay())
        {
            super.preRender(roomOverlay, total);
        }
        else
        {
            super.preRender(roomOverlay, total, p1, p2);
        }
    }
}

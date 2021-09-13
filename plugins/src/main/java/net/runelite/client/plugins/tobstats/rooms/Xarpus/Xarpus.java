package net.runelite.client.plugins.tobstats.rooms.Xarpus;

import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.tobstats.Room;
import net.runelite.client.plugins.tobstats.RoomData;
import net.runelite.client.plugins.tobstats.RoomOverlay;
import net.runelite.client.plugins.tobstats.TobstatsConfig;
import net.runelite.client.plugins.tobstats.TobstatsPlugin;
import net.runelite.client.ui.overlay.components.LineComponent;

@Slf4j
public class Xarpus extends Room
{
    private XarpusData data;

    @Inject
    protected Xarpus(TobstatsPlugin plugin, TobstatsConfig config)
    {
        super(plugin, config);
    }

    @Override
    protected boolean isInRegion()
    {
        return client.getMapRegions() != null && client.getMapRegions().length > 0 && Arrays.stream(client.getMapRegions()).anyMatch(s -> s == 12612);
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
        data = new XarpusData();
        data.setId(id);
    }

    @Override
    protected void reset()
    {
        super.reset();
        isActive = false;
        log.debug("resetting " + this.getClass().getSimpleName());
    }

    private void setPhaseMillis(int millis, String prefix)
    {
        setPhaseMillis(millis, prefix, null);
    }

    private void setPhaseMillis(int millis, String prefix, Integer previous)
    {
        log.debug(prefix + " spawn millis " + millis);
        String subject = "Xarpus " + prefix;
        printTimeToChat(millis, previous, subject, true, config.precisionTimers());
    }

    @Subscribe
    protected void onNpcChanged(NpcChanged npcChanged)
    {
        if (!isInRegion())
        {
            return;
        }
        int id = npcChanged.getNpc().getId();
        switch (id)
        {
            case NpcID.XARPUS_8339:
            case NpcID.XARPUS_10767:
            case NpcID.XARPUS_10771:

            case NpcID.XARPUS_8340:
            case NpcID.XARPUS_10768:
            case NpcID.XARPUS_10772:

            case NpcID.XARPUS_8341:
            case NpcID.XARPUS_10769:
            case NpcID.XARPUS_10773:
                startTimer();
                break;
        }
        handleXarpusNPC(id);
    }


    @Subscribe
    protected void onNpcSpawned(NpcSpawned npcSpawned)
    {
        if (!isInRegion())
        {
            return;
        }
        int id = npcSpawned.getNpc().getId();
        handleXarpusNPC(id);
    }

    @Subscribe
    protected void onOverheadTextChanged(OverheadTextChanged overheadTextChanged)
    {
        if (!isInRegion())
        {
            return;
        }
        if (overheadTextChanged.getActor() instanceof NPC)
        {
            NPC xarpus = (NPC)overheadTextChanged.getActor();
            switch (xarpus.getId())
            {
                case NpcID.XARPUS:
                case NpcID.XARPUS_10766:
                case NpcID.XARPUS_10770:

                case NpcID.XARPUS_8339:
                case NpcID.XARPUS_10767:
                case NpcID.XARPUS_10771:

                case NpcID.XARPUS_8340:
                case NpcID.XARPUS_10768:
                case NpcID.XARPUS_10772:

                case NpcID.XARPUS_8341:
                case NpcID.XARPUS_10769:
                case NpcID.XARPUS_10773:
                    int millis = getCurrentMillis();
                    setPhaseMillis(millis, "phase 2", data.getP1());
                    data.setP2(millis);
                    break;
            }
        }
    }

    private void handleXarpusNPC(int id)
    {
        switch (id)
        {
            case NpcID.XARPUS_8340:
            case NpcID.XARPUS_10768:
            case NpcID.XARPUS_10772:

            case NpcID.XARPUS_8341:
            case NpcID.XARPUS_10769:
            case NpcID.XARPUS_10773:
                if (data.getP1() != null)
                {
                    return;
                }
                int millis = getCurrentMillis();
                data.setP1(millis);
                setPhaseMillis(millis, "phase 1");
                break;
        }
    }

    @Override
    protected void preRender(RoomOverlay roomOverlay)
    {
        int millis = getCurrentMillis();
        String ttime = millisToSplit(millis, !isActive && data.getP2() != null ? data.getP2() : millis, false) +
                millisToTime(millis, true, config.precisionTimers());
        LineComponent total = LineComponent.builder().left("Xarpus").right(ttime).build();

        int p1Time = data.getP1() != null ? data.getP1() : 0;
        LineComponent p1 = LineComponent.builder().left("Vents").right(millisToTime(p1Time, false, false)).build();

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

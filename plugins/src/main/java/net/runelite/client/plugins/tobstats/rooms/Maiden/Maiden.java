package net.runelite.client.plugins.tobstats.rooms.Maiden;

import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NpcID;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.tobstats.Room;
import net.runelite.client.plugins.tobstats.RoomData;
import net.runelite.client.plugins.tobstats.RoomOverlay;
import net.runelite.client.plugins.tobstats.TobstatsConfig;
import net.runelite.client.plugins.tobstats.TobstatsPlugin;
import net.runelite.client.ui.overlay.components.LineComponent;

@Slf4j
public class Maiden extends Room
{
    private MaidenData data = new MaidenData();

    @Inject
    protected Maiden(TobstatsPlugin plugin, TobstatsConfig config)
    {
        super(plugin, config);
    }

    @Subscribe
    protected void onNpcChanged(NpcChanged npcChanged)
    {
        if (!isInRegion())
        {
            return;
        }
        int npcId = npcChanged.getNpc().getId();

        handleMaidenNPC(npcId);
    }

    @Subscribe
    protected void onNpcSpawned(NpcSpawned npcSpawned)
    {
        int npcId = npcSpawned.getNpc().getId();

        switch (npcId)
        {
            // regular
            case NpcID.THE_MAIDEN_OF_SUGADINTI:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_8361:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_8362:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_8363:
            // story
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10814:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10815:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10816:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10817:
            // hard
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10822:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10823:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10824:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10825:
            startTimer();
            break;
        }

        handleMaidenNPC(npcId);
    }

    private void handleMaidenNPC(int id)
    {
        int millis = getCurrentMillis();
        if (millis < 1)
        {
            return;
        }
        switch (id)
        {
            case NpcID.THE_MAIDEN_OF_SUGADINTI_8361:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10815:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10823:
                if (data.getP1() != null)
                {
                    log.warn("Overwriting millis for p1, previous " + data.getP1());
                }
                data.setP1(millis);
                setPhaseMillis(millis, "phase 1");
                break;
            case NpcID.THE_MAIDEN_OF_SUGADINTI_8362:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10816:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10824:
                if (data.getP2() != null)
                {
                    log.warn("Overwriting millis for p2, previous " + data.getP1());
                }
                data.setP2(millis);
                setPhaseMillis(millis, "phase 2", data.getP1());
                break;
            case NpcID.THE_MAIDEN_OF_SUGADINTI_8363:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10817:
            case NpcID.THE_MAIDEN_OF_SUGADINTI_10825:
                if (data.getP3() != null)
                {
                    log.warn("Overwriting millis for p3, previous " + data.getP2());
                }
                data.setP3(millis);
                setPhaseMillis(millis, "phase 3", data.getP2());
                break;
        }
    }

    private void setPhaseMillis(int millis, String prefix)
    {
        setPhaseMillis(millis, prefix, null);
    }

    private void setPhaseMillis(int millis, String prefix, Integer previous)
    {
        log.debug(prefix + " spawn millis " + millis);
        String subject = "Maiden " + prefix;
        printTimeToChat(millis, previous, subject, true, config.precisionTimers());
    }

    @Override
    protected boolean isInRegion()
    {
        return client.getMapRegions() != null && client.getMapRegions().length > 0 && Arrays.stream(client.getMapRegions()).anyMatch(r -> r == 12613 || r == 12869);
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
        data = new MaidenData();
        data.setId(id);
    }

    @Override
    protected void reset()
    {
        super.reset();
        log.debug("resetting " + this.getClass().getSimpleName());
    }

    @Override
    protected void preRender(RoomOverlay roomOverlay)
    {
        int millis = getCurrentMillis();
        String ttime = millisToSplit(millis,  !isActive && data.getP3() != null ? data.getP3() : millis, false) +
                millisToTime(millis, true, config.precisionTimers());
        LineComponent total = LineComponent.builder().left("Maiden").right(ttime).build();

        int p1Time = data.getP1() != null ? data.getP1() : 0;
        LineComponent p1 = LineComponent.builder().left("Phase 1").right(millisToTime(p1Time, false, false)).build();

        int p2time = data.getP2() != null ? data.getP2() : 0;
        LineComponent p2 = LineComponent.builder().left("Phase 2").right(millisToSplit(p2time, p1Time, false) + millisToTime(p2time, false, false)).build();

        int p3time = data.getP3() != null ? data.getP3() : 0;
        LineComponent p3 = LineComponent.builder().left("Phase 3").right(millisToSplit(p3time, p2time, false) + millisToTime(p3time, false, false)).build();
        if (config.simpleTimerOverlay())
        {
            super.preRender(roomOverlay, total);
        }
        else
        {
            super.preRender(roomOverlay, total, p1, p2, p3);
        }
    }
}

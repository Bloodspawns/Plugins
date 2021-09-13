package net.runelite.client.plugins.tobstats.rooms.Nylocas;

import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NpcID;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.tobstats.Room;
import net.runelite.client.plugins.tobstats.RoomData;
import net.runelite.client.plugins.tobstats.RoomOverlay;
import net.runelite.client.plugins.tobstats.TobstatsConfig;
import net.runelite.client.plugins.tobstats.TobstatsPlugin;
import net.runelite.client.ui.overlay.components.LineComponent;

@Slf4j
public class Nylocas extends Room
{
    private NylocasData data;

    @Inject
    protected Nylocas(TobstatsPlugin plugin, TobstatsConfig config)
    {
        super(plugin, config);
    }

    @Override
    protected boolean isInRegion()
    {
        return client.getMapRegions() != null && client.getMapRegions().length > 0 && Arrays.stream(client.getMapRegions()).anyMatch(s -> s == 13122);
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
    protected void startTimer()
    {
        boolean active = isActive;
        super.startTimer();

        if (!active)
        {
            net.runelite.client.plugins.ztob.rooms.Nylocas.Nylocas.setWave31Callback(() ->
            {
                int millis = getCurrentMillis();
                log.debug("Executing wave 31 callback " + millis);
                data.setWave31(millis);
                printTimeToChatNoCheck(millis,null, "Nylocas last wave", true, config.precisionTimers());
            });

            net.runelite.client.plugins.ztob.rooms.Nylocas.Nylocas.setEndOfWavesCallback(() ->
            {
                int millis = getCurrentMillis();
                log.debug("Executing end of wave callback " + millis);
                data.setEnd_waves(millis);
                printTimeToChatNoCheck(millis, data.getWave31(), "Nylocas waves and cleanup", true, config.precisionTimers());
            });
        }
    }

    @Override
    protected void init()
    {
        super.init();
        int id = 0;
        log.debug("Initializing " + this.getClass().getSimpleName() + " id " + id);
        data = new NylocasData();
        data.setId(id);
    }

    @Override
    protected void reset()
    {
        super.reset();

        net.runelite.client.plugins.ztob.rooms.Nylocas.Nylocas.setEndOfWavesCallback(null);
        net.runelite.client.plugins.ztob.rooms.Nylocas.Nylocas.setWave31Callback(null);

        isActive = false;
        log.debug("resetting " + this.getClass().getSimpleName());
    }

    @Subscribe
    protected void onNpcSpawned(NpcSpawned npcSpawned)
    {
        if (!isInRegion())
        {
            return;
        }
        int id = npcSpawned.getNpc().getId();
        switch (id)
        {
            case NpcID.NYLOCAS_VASILIAS:
            case NpcID.NYLOCAS_VASILIAS_10786:
            case NpcID.NYLOCAS_VASILIAS_10807:
                int millis = getCurrentMillis();
                data.setBoss_spawn(millis);
                printTimeToChatNoCheck(millis, data.getEnd_waves(), "Nylocas boss spawn", true, config.precisionTimers());
                break;
        }
    }

    @Override
    protected void onVarbitChanged(VarbitChanged varbitChanged)
    {
        if (!isInRegion())
        {
            return;
        }
        int varp6447 = client.getVarbitValue(client.getVarps(), ROOM_STATE_VARBIT);

        if (data.getBoss_spawn() != null && lastVarp6447 != varp6447 && varp6447 == 0)
        {
            int millis = getCurrentMillis() - data.getBoss_spawn();
            printTimeToChatNoCheck(millis, null, "Nylocas Boss", true, config.precisionTimers());
        }

        super.onVarbitChanged(varbitChanged);
    }

    @Override
    protected void preRender(RoomOverlay roomOverlay)
    {
        int millis = getCurrentMillis();
        String ttime = millisToSplit(millis, !isActive && data.getBoss_spawn() != null ? data.getBoss_spawn() : millis, false) +
                millisToTime(millis, true, config.precisionTimers());
        LineComponent total = LineComponent.builder().left("Nylocas").right(ttime).build();

        int p1Time = data.getWave31() != null ? data.getWave31() : 0;
        LineComponent p1 = LineComponent.builder().left("Wave 31").right(millisToTime(p1Time, false, false)).build();

        int p2time = data.getEnd_waves() != null ? data.getEnd_waves() : 0;
        LineComponent p2 = LineComponent.builder().left("Cleanup").right(millisToSplit(p2time, p1Time, false) +
                millisToTime(p2time, false, false)).build();

        int p3time = data.getBoss_spawn() != null ? data.getBoss_spawn() : 0;
        LineComponent p3 = LineComponent.builder().left("Boss spawn").right(millisToSplit(p3time, p2time, false) +
                millisToTime(p3time, false, false)).build();

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

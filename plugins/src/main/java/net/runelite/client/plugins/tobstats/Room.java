package net.runelite.client.plugins.tobstats;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.components.LineComponent;

@Slf4j
public abstract class Room
{
    // THEATRE OF BLOOD
    protected static final int ROOM_STATE_VARBIT = 6447;

    protected final TobstatsPlugin plugin;
    protected final TobstatsConfig config;
    protected boolean isActive = false;
    protected int lastVarp6447 = 0;
    private long startMillis = 0;
    private long endMillis = -1;
    @Inject
    protected Client client;
    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    protected Room(TobstatsPlugin plugin, TobstatsConfig config)
    {
        this.plugin = plugin;
        this.config = config;
    }

    protected void init()
    {

    }

    protected void startTimer()
    {
        if (!isActive)
        {
            startMillis = System.currentTimeMillis();
            endMillis = -1;
            log.debug("Timer started");
        }
        isActive = true;
    }

    private void stopTimer()
    {
        if (isActive)
        {
            endMillis = System.currentTimeMillis();
            log.debug("Timer stopped");
        }
        isActive = false;
    }

    protected void reset()
    {
        isActive = false;
        lastVarp6447 = 0;
        endMillis = -1;
        startMillis = 0;
    }

    protected abstract boolean isInRegion();

    protected abstract void setIncomplete(Integer incomplete);

    protected abstract RoomData getData();

    @Subscribe
    protected void onVarbitChanged(VarbitChanged event)
    {
        if (!isInRegion())
        {
            return;
        }
        int varp6447 = client.getVarbitValue(client.getVarps(), ROOM_STATE_VARBIT);
        if (varp6447 != lastVarp6447)
        {
            log.debug("Varbit6447 changed " + varp6447 + " " + startMillis);
            if (varp6447 > 0)
            {
                startTimer();
            }
            else if (varp6447 == 0)
            {
                stopTimer();
            }
        }
        lastVarp6447 = varp6447;
    }

    protected void preRender(RoomOverlay roomOverlay)
    {

    }

    protected void preRender(RoomOverlay roomOverlay, LineComponent... lines)
    {
        roomOverlay.getPanelComponent().getChildren().clear();
        for (LineComponent lineComponent : lines)
        {
            roomOverlay.getPanelComponent().getChildren().add(lineComponent);
        }
    }

    private static int[] millisToMinuteSeconds(int millis)
    {
        if (millis <= 0)
        {
            return new int[] {0, 0};
        }
        int[] result = new int[2];
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / 1000) / 60;
        result[0] = minutes;
        result[1] = seconds;
        return result;
    }

    protected String millisToSplit(int millis, Integer previous, boolean highPrecision)
    {
        StringBuilder stringBuilder = new StringBuilder();
        if (config.chatTimerSplits() && previous != null)
        {
            int[] splitTime = millisToMinuteSeconds(millis - previous);
            String splitTimeStr;
            if (highPrecision)
            {
                splitTimeStr = String.format("%d:%02d.%s", splitTime[0], splitTime[1], hunderthSecond(millis - previous));
            }
            else
            {
                splitTimeStr = String.format("%d:%02d", splitTime[0], splitTime[1]);
            }
            stringBuilder = stringBuilder.append("(").append(splitTimeStr);
            stringBuilder = stringBuilder.append(") ");
        }

        return stringBuilder.toString();
    }

    protected String millisToTime(int millis, boolean markIncomplete, boolean highPrecision)
    {
        int[] time = Room.millisToMinuteSeconds(millis);

        String timeStr;
        if (highPrecision)
        {
            timeStr = String.format("%d:%02d.%s", time[0], time[1], hunderthSecond(millis));
        } else
        {
            timeStr = String.format("%d:%02d", time[0], time[1]);
        }
        StringBuilder stringBuilder = new StringBuilder(timeStr);

        if (getData().getIncomplete() == 1)
        {
            stringBuilder = stringBuilder.append(markIncomplete ? "*" : "");
        }
        return stringBuilder.toString();
    }

    private String hunderthSecond(long millis)
    {
        String num = String.valueOf(millis);
        if (num.length() < 3)
        {
            return "0";
        }
        return num.substring(num.length() - 3, num.length() - 2);
    }

    protected void printTimeToChatNoCheck(int millis, Integer previous, String subject, boolean markIncomplete, boolean highPrecision)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder = stringBuilder.append("Wave '").append(subject).append("' complete! Duration: <col=EF1020>").
                append(millisToTime(millis, markIncomplete, highPrecision)).append(" ").append(millisToSplit(millis, previous, highPrecision));
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", stringBuilder.toString(), "", false);
    }

    protected void printTimeToChat(int millis, Integer previous, String subject, boolean markIncomplete, boolean highPrecision)
    {
        if (config.chatMessagePhase())
        {
            printTimeToChatNoCheck(millis, previous, subject, markIncomplete, highPrecision);
        }
    }

    protected int getCurrentMillis()
    {
        if (endMillis > 0)
        {
            return (int)(endMillis - startMillis);
        }
        if (!isActive)
        {
            return 0;
        }
        return (int)(System.currentTimeMillis() - startMillis);
    }
}

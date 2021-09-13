package net.runelite.client.plugins.raidsb.raidshelper;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
        name = "Chambers Of Xeric helper",
        description = "CoX cheats",
        tags = {"combat", "raid", "overlay", "pve", "pvm", "bosses"}
)
@Slf4j
@Singleton
public class RaidsHelperPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private RaidsHelperConfig raidsHelperConfig;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private RaidsHelperOverlay raidsHelperOverlay;

    @Provides
    RaidsHelperConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(RaidsHelperConfig.class);
    }

    private static final int OLM_TELEPORT = 1359;
    private static final int OLM_HEAL = 1363;
    private static final int OLM_CRYSTAL = 1447;
    private boolean runOlm;
    private int teleportTicks = 10;
    @Getter
    private List<WorldPoint> olmHeals = new ArrayList<>();
    @Getter
    private List<WorldPoint> olmTeleports = new ArrayList<>();
    @Getter
    private HashMap<WorldPoint, Integer> olmCrystals = new HashMap<>();

    @Override
    protected void startUp()
    {
        olmTeleports.clear();
        olmHeals.clear();
        teleportTicks = 10;
        overlayManager.add(raidsHelperOverlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(raidsHelperOverlay);
    }

    @Subscribe
    protected void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectC)
    {
        if (runOlm)
        {
            GraphicsObject graphicsObject = graphicsObjectC.getGraphicsObject();
            if (graphicsObject.getId() == OLM_CRYSTAL)
            {
                WorldPoint point = WorldPoint.fromLocal(client, graphicsObject.getLocation());
                olmCrystals.put(point, 4);
            }
        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event)
    {
        if (!inRaid())
        {
            return;
        }

        if (event.getType() == ChatMessageType.GAMEMESSAGE)
        {
            switch (Text.standardize(event.getMessageNode().getValue()))
            {
                case "the great olm rises with the power of acid.":
                case "the great olm rises with the power of crystal.":
                case "the great olm rises with the power of flame.":
                case "the great olm is giving its all. this is its final stand.":
                    runOlm = true;
                    break;
            }
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (client.getGameState() == GameState.LOGGED_IN && !inRaid())
        {
            runOlm = false;
        }
    }

    @Subscribe
    private void onGameTick(GameTick event)
    {

        olmCrystals.values().removeIf(v -> v <= 0);
        olmCrystals.replaceAll((k, v) -> v - 1);
        if (runOlm)
        {
            olmTeleports.clear();
            olmHeals.clear();

            client.clearHintArrow();

            for (GraphicsObject o : client.getGraphicsObjects())
            {
                if (o.getId() == OLM_TELEPORT)
                {
                    olmTeleports.add(WorldPoint.fromLocal(client, o.getLocation()));
                }
                if (o.getId() == OLM_HEAL)
                {
                    olmHeals.add(WorldPoint.fromLocal(client, o.getLocation()));
                }
                if (!olmTeleports.isEmpty())
                {
                    teleportTicks--;
                    if (teleportTicks <= 0)
                    {
                        client.clearHintArrow();
                        teleportTicks = 10;
                    }
                }
            }
        }
    }

    boolean inRaid()
    {
        return client.getGameState() == GameState.LOGGED_IN && client.getVar(Varbits.IN_RAID) == 1;
    }
}

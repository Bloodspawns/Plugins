/*
 * THIS SOFTWARE WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI
 * No rights reserved. Use, redistribute, and modify at your own discretion,
 * and in accordance with Yagex and RuneLite guidelines.
 * However, aforementioned monkey would prefer if you don't sell this plugin for profit.
 * Credit to lyzrds for input on creating this plugin.
 */

package net.runelite.client.plugins.raidsb.reconnect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.raids.RaidsPlugin;

@PluginDescriptor(
        name = "Reconnect Plugin",
        description = "Reconnect plugin",
        tags = {"reconnect", "cox", "dc scout", "dc", "scout"},
        enabledByDefault = false
)

@Slf4j
public class ReconnectPlugin extends Plugin
{
    private boolean hotkey1;
    private boolean hotkey2;
    private boolean reloaded = false;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ReconnectInput inputListener;

    @Inject
    private ScheduledExecutorService executorService;

    @Inject
    private KeyManager keyManager;

    @Inject
    private EventBus eventBus;

    @Inject
    private PluginManager pluginManager;

    private RaidsPlugin raidsPlugin = null;

    private Method checkRaidPresence;

    @Override
    protected void startUp()
    {
        hotkey1 = false;
        hotkey2 = false;
        keyManager.registerKeyListener(inputListener);
        eventBus.register(this);

        try
        {
            checkRaidPresence = RaidsPlugin.class.getDeclaredMethod("checkRaidPresence");
        }
        catch (NoSuchMethodException e)
        {
            log.warn("Couldnt find method to force refresh raid scout overlay");
        }
    }

    @Override
    protected void shutDown()
    {
        keyManager.unregisterKeyListener(inputListener);
        eventBus.unregister(this);
    }

    void updateHotkey1(boolean pressed)
    {
        hotkey1 = pressed;
    }

    void updateHotkey2(boolean pressed)
    {
        hotkey2 = pressed;
    }

    void updateHotkey3(boolean pressed)
    {
        if (hotkey1 && hotkey2 && pressed)
        {
           if ((client.getGameState() == GameState.LOGGED_IN))
           {
               clientThread.invoke(() -> client.setGameState(GameState.CONNECTION_LOST));
               reloaded = true;
           }
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN && reloaded)
        {
            executorService.schedule(() ->
                clientThread.invoke(() ->
                {
                    try
                    {
                        if (checkRaidPresence == null)
                        {
                            return;
                        }
                        checkRaidPresence.setAccessible(true);
                        checkRaidPresence.invoke(getRaidsPlugin());
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        log.warn("Failed to auto refresh scout overlay");
                    }
                }), 250, TimeUnit.MILLISECONDS);
            reloaded = false;
        }
    }

    private RaidsPlugin getRaidsPlugin()
    {
        if (raidsPlugin == null)
        {
            for (Plugin plugin : pluginManager.getPlugins())
            {
                if (plugin instanceof RaidsPlugin)
                {
                    raidsPlugin = (RaidsPlugin) plugin;
                }
            }
        }
        return raidsPlugin;
    }
}

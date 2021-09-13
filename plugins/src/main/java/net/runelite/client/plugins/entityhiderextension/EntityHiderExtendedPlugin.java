package net.runelite.client.plugins.entityhiderextension;

import com.google.inject.Provides;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
        name = "EntityHiderExtendedPlugin",
        description = "shhh",
        tags = {"dead","hide","npc"}
)
@Slf4j
public class EntityHiderExtendedPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private EntityHiderExtendedConfig config;

    @Inject
    private PluginManager pluginManager;

    private HashSet<String> getNpcsToHide = null;
    private HashSet<String> getNpcsToHideOnDeath = null;
    private HashSet<Integer> getNpcsByAnimationToHideOnDeath = null;
    private HashSet<Integer> getNpcsByIdToHideOnDeath = null;

    private Method setHidden = null;

    @Provides
    EntityHiderExtendedConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(EntityHiderExtendedConfig.class);
    }

    private void setDeadNPCsHidden(boolean val)
    {
        if (setHidden != null)
        {
            try
            {
                setHidden.invoke(client, val);
            } catch (IllegalAccessException | InvocationTargetException e)
            {
                log.warn("Couldn't invoke setDeadNPCsHidden", e);
            }
        }
    }

    @Override
    protected void startUp()
    {
        try
        {
            setHidden = Arrays.stream(client.getClass().getDeclaredMethods()).filter(s -> s.getName().equals("setDeadNPCsHidden")).findFirst().orElse(null);
            if (setHidden == null)
            {
                throw new AbstractMethodError("No such method setDeadNPCsHidden");
            }

                Arrays.stream(client.getClass().getDeclaredMethods()).forEach(m ->
                {
                    try
                    {
                        switch (m.getName())
                        {
                            case "getNpcsToHide":
                                getNpcsToHide = (HashSet<String>) m.invoke(client);
                                break;
                            case "getNpcsToHideOnDeath":
                                getNpcsToHideOnDeath = (HashSet<String>) m.invoke(client);
                                break;
                            case "getNpcsByAnimationToHideOnDeath":
                                getNpcsByAnimationToHideOnDeath = (HashSet<Integer>) m.invoke(client);
                                break;
                            case "getNpcsByIdToHideOnDeath":
                                getNpcsByIdToHideOnDeath = (HashSet<Integer>) m.invoke(client);
                                break;
                        }
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        throw new AbstractMethodError("No such method ");
                    }
                });

            updateConfig();

            getNpcsToHide.addAll(Text.fromCSV(config.hideNPCsNames().toLowerCase()));
            getNpcsToHideOnDeath.addAll(Text.fromCSV(config.hideNPCsOnDeath().toLowerCase()));
            parseAndAddSave(Text.fromCSV(config.hideNPCsByID()), getNpcsByIdToHideOnDeath);
            parseAndAddSave(Text.fromCSV(config.hideNPCsByAnimationId()), getNpcsByAnimationToHideOnDeath);
        }
        catch (AbstractMethodError ignored)
        {
            SwingUtilities.invokeLater(() ->
            {
                try
                {
                    pluginManager.stopPlugin(this);
                }
                catch (PluginInstantiationException ex)
                {
                    log.error("error stopping plugin", ex);
                }
            });
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (event.getGroup().equals("ehextended"))
        {
            updateConfig();

            if (event.getOldValue() == null || event.getNewValue() == null)
            {
                return;
            }

            if (event.getKey().equals("hideNPCsNames"))
            {
                getNpcsToHide.clear();
                getNpcsToHide.addAll(Text.fromCSV(config.hideNPCsNames().toLowerCase()));
            }

            if (event.getKey().equals("hideNPCsOnDeath"))
            {
                getNpcsByIdToHideOnDeath.clear();
                getNpcsToHideOnDeath.addAll(Text.fromCSV(config.hideNPCsOnDeath().toLowerCase()));
            }

            if (event.getKey().equals("hideNPCsByID"))
            {
                getNpcsByIdToHideOnDeath.clear();
                parseAndAddSave(Text.fromCSV(config.hideNPCsByID()), getNpcsByIdToHideOnDeath);
            }

            if (event.getKey().equals("hideNPCsByAnimationId"))
            {
                getNpcsByAnimationToHideOnDeath.clear();
                parseAndAddSave(Text.fromCSV(config.hideNPCsByAnimationId()), getNpcsByAnimationToHideOnDeath);
            }
        }
    }

    private static void parseAndAddSave(Collection<String> source, Collection<Integer> collection)
    {
        for (String s : source)
        {
            try
            {
                int val = Integer.parseInt(s);
                collection.add(val);
            }
            catch (NumberFormatException ex)
            {
                log.warn("Config entry could not be parsed, entry: {}", s);
            }
        }
    }

    private void updateConfig()
    {
        setDeadNPCsHidden(config.hideDeadNPCs());
    }

    @Override
    protected void shutDown()
    {
        try
        {
            setDeadNPCsHidden(false);

            Text.fromCSV(config.hideNPCsNames().toLowerCase()).forEach(s -> getNpcsToHide.remove(s));
            Text.fromCSV(config.hideNPCsOnDeath().toLowerCase()).forEach(s -> getNpcsToHideOnDeath.remove(s));
            Text.fromCSV(config.hideNPCsByID()).forEach(id -> getNpcsByIdToHideOnDeath.remove(Integer.parseInt(id)));
            Text.fromCSV(config.hideNPCsByAnimationId()).forEach(id -> getNpcsByAnimationToHideOnDeath.remove(Integer.parseInt(id)));
        }
        catch (AbstractMethodError | NullPointerException ignored)
        {

        }
    }

}

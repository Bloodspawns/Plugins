package net.runelite.client.plugins.aoewarnings;

import com.google.inject.Provides;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NullObjectID;
import net.runelite.api.ObjectID;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "AoeWarnings",
        description = "shhh",
        tags = {"aoe","projectile","warning"},
        enabledByDefault = false
)
public class AoeWarningsPlugin extends Plugin
{
    @Getter(AccessLevel.PACKAGE)
    private final HashMap<GameObject, CrystalBomb> bombs = new HashMap<>();

    @Getter(AccessLevel.PACKAGE)
    private final HashMap<GameObject, Integer> gameObjects = new HashMap<>();

    @Getter(AccessLevel.PACKAGE)
    private final HashMap<Projectile, ProjectileContainer> projectiles = new HashMap<>();

    @Inject
    public AoeWarningsConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AoeWarningsOverlay coreOverlay;

    @Inject
    private Client client;

    @Getter(AccessLevel.PACKAGE)
    private final List<WorldPoint> lightningTrail = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    private final List<GameObject> olmAcidTrail = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    private final List<GameObject> verzikAcidTrail = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    private final List<GameObject> crystalSpike = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    private final List<GameObject> wintertodtSnowFall = new ArrayList<>();

    @Provides
    AoeWarningsConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(AoeWarningsConfig.class);
    }

    @Override
    protected void startUp()
    {
        overlayManager.add(coreOverlay);
        reset();
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(coreOverlay);
        reset();
    }

    private void reset()
    {
        lightningTrail.clear();
        olmAcidTrail.clear();
        verzikAcidTrail.clear();
        crystalSpike.clear();
        wintertodtSnowFall.clear();
        bombs.clear();
        projectiles.clear();
        gameObjects.clear();
    }

    private void onProjectileSpawned(Projectile projectile)
    {
        if (AoeProjectileInfo.getById(projectile.getId()) == null)
        {
            return;
        }

        final int id = projectile.getId();
        final int lifetime = config.delay() + (projectile.getRemainingCycles() * 20);
        int ticksRemaining = projectile.getRemainingCycles() / 30;
        if (!isTickTimersEnabledForProjectileID(id))
        {
            ticksRemaining = 0;
        }
        final int tickCycle = client.getTickCount() + ticksRemaining;
        projectiles.put(projectile, new ProjectileContainer(projectile, Instant.now(), lifetime, tickCycle));
    }

    @Subscribe
    private void onProjectileMoved(ProjectileMoved event)
    {
        if (!projectiles.containsKey(event.getProjectile()))
        {
            onProjectileSpawned(event.getProjectile());
        }

        if (projectiles.isEmpty())
        {
            return;
        }

        final Projectile projectile = event.getProjectile();

        if (projectiles.containsKey(projectile))
        {
            projectiles.get(projectile).setTargetPoint(event.getPosition());
        }
    }

    @Subscribe
    private void onGameObjectSpawned(GameObjectSpawned event)
    {
        final GameObject gameObject = event.getGameObject();

        switch (gameObject.getId())
        {
            case 37738:
            case 37739:
                gameObjects.put(gameObject, 3);
                break;
            case ObjectID.CRYSTAL_BOMB:
                bombs.put(gameObject, new CrystalBomb(gameObject, client.getTickCount()));
                break;
            case ObjectID.ACID_POOL:
                olmAcidTrail.add(gameObject);
                break;
            case ObjectID.ACID_POOL_41747:
                verzikAcidTrail.add(gameObject);
                break;
            case ObjectID.SMALL_CRYSTALS:
                crystalSpike.add(gameObject);
                break;
            case NullObjectID.NULL_26690:
                if (config.isWintertodtEnabled())
                {
                    wintertodtSnowFall.add(gameObject);
                }
                break;
        }
    }

    @Subscribe
    private void onGameObjectDespawned(GameObjectDespawned event)
    {
        final GameObject gameObject = event.getGameObject();

        switch (gameObject.getId())
        {
            case 37738:
            case 37739:
                gameObjects.remove(gameObject);
                break;
            case ObjectID.CRYSTAL_BOMB:
                bombs.remove(event.getGameObject());
                break;
            case ObjectID.ACID_POOL:
                olmAcidTrail.remove(gameObject);
                break;
            case ObjectID.ACID_POOL_41747:
                verzikAcidTrail.remove(gameObject);
                break;
            case ObjectID.SMALL_CRYSTALS:
                crystalSpike.remove(gameObject);
                break;
            case NullObjectID.NULL_26690:
                wintertodtSnowFall.remove(gameObject);
                break;
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            return;
        }
        reset();
    }

    @Subscribe
    private void onGameTick(GameTick event)
    {
        lightningTrail.clear();

        if (config.LightningTrail())
        {
            client.getGraphicsObjects().forEach(o ->
            {
                if (o.getId() == 1356)
                {
                    lightningTrail.add(WorldPoint.fromLocal(client, o.getLocation()));
                }
            });
        }
    }

    private boolean isTickTimersEnabledForProjectileID(int projectileId)
    {
        AoeProjectileInfo projectileInfo = AoeProjectileInfo.getById(projectileId);

        if (projectileInfo == null)
        {
            return false;
        }

        switch (projectileInfo)
        {
            case VASA_RANGED_AOE:
            case VORKATH_POISON_POOL:
            case VORKATH_SPAWN:
            case VORKATH_TICK_FIRE:
            case OLM_BURNING:
            case OLM_FALLING_CRYSTAL_TRAIL:
            case OLM_ACID_TRAIL:
            case OLM_FIRE_LINE:
                return false;
        }

        return true;
    }


    boolean isConfigEnabledForProjectileId(int projectileId)
    {
        AoeProjectileInfo projectileInfo = AoeProjectileInfo.getById(projectileId);
        if (projectileInfo == null)
        {
            return false;
        }

        switch (projectileInfo)
        {
            case VERZIK_CUPCAKE:
                return config.isVerzikCakesEnabled();
            case LIZARDMAN_SHAMAN_AOE:
                return config.isShamansEnabled();
            case CRAZY_ARCHAEOLOGIST_AOE:
                return config.isArchaeologistEnabled();
            case ICE_DEMON_RANGED_AOE:
            case ICE_DEMON_ICE_BARRAGE_AOE:
                return config.isIceDemonEnabled();
            case VASA_AWAKEN_AOE:
            case VASA_RANGED_AOE:
                return config.isVasaEnabled();
            case TEKTON_METEOR_AOE:
                return config.isTektonEnabled();
            case VORKATH_BOMB:
            case VORKATH_POISON_POOL:
            case VORKATH_SPAWN:
            case VORKATH_TICK_FIRE:
                return config.isVorkathEnabled();
            case VETION_LIGHTNING:
                return config.isVetionEnabled();
            case CHAOS_FANATIC:
                return config.isChaosFanaticEnabled();
            case GALVEK_BOMB:
            case GALVEK_MINE:
                return config.isGalvekEnabled();
            case DAWN_FREEZE:
            case DUSK_CEILING:
                return config.isGargBossEnabled();
            case OLM_FALLING_CRYSTAL:
            case OLM_BURNING:
            case OLM_FALLING_CRYSTAL_TRAIL:
            case OLM_ACID_TRAIL:
            case OLM_FIRE_LINE:
                return config.isOlmEnabled();
            case CORPOREAL_BEAST:
            case CORPOREAL_BEAST_DARK_CORE:
                return config.isCorpEnabled();
            case XARPUS_POISON_AOE:
                return config.isXarpusEnabled();
            case ADDY_DRAG_POISON:
                return config.addyDrags();
            case DRAKE_BREATH:
                return config.isDrakeEnabled();
            case CERB_FIRE:
                return config.isCerbFireEnabled();
            case DEMONIC_GORILLA_BOULDER:
                return config.isDemonicGorillaEnabled();
        }

        return false;
    }
}

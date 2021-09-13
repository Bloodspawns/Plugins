package net.runelite.client.plugins.nightmare;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.SpriteID;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.infobox.Timer;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.time.temporal.ChronoUnit;

@PluginDescriptor(
	name = "Nightmare",
	description = "shhh",
	tags = {"nm", "shit boss", "prayer swap"},
	enabledByDefault = false
)
@Slf4j
public class NightmareBossPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private NightmareBossConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NightmareBossOverlay nightmareBossOverlay;

	@Inject
	private BossPrayerOverlay bossPrayerOverlay;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	private static BufferedImage vespula;

	@Provides
	NightmareBossConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NightmareBossConfig.class);
	}

	@Getter
	private ProtectPrayer attackStyle = ProtectPrayer.MELEE;
	private static final String P2_CURSE = "the nightmare has cursed you, shuffling your prayers!";
	private static final String P2CURSE_END = "you feel the effects of the nightmare's curse wear off.";
	Point originalMagePosition = null;
	Point originalMeleePosition = null;
	Point originalRangePosition = null;

	boolean reorderActive = false;

	@Override
	protected void startUp() throws Exception
	{
		setOriginalPositions();
		deActivateShuffle();
		reorderActive = false;
		overlayManager.add(nightmareBossOverlay);
		overlayManager.add(bossPrayerOverlay);
		vespula = itemManager.getImage(ItemID.VESPINA);
	}

	@Override
	protected void shutDown() throws Exception
	{
		deActivateShuffle();
		reorderActive = false;
		overlayManager.remove(bossPrayerOverlay);
		overlayManager.remove(nightmareBossOverlay);
	}

//	@Subscribe
//	public void onConfigChanged(ConfigChanged configChanged)
//	{
//		if (!"NightmareBoss".equals(configChanged.getGroup()))
//		{
//			return;
//		}
//
//		if ("test".equals(configChanged.getKey()))
//		{
//			if (config.test())
//			{
//				activateShuffle();
//			}
//			else
//			{
//				deActivateShuffle();
//			}
//		}
//	}

	@Subscribe(priority = -1)
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == WidgetID.PRAYER_GROUP_ID)
		{
			setOriginalPositions();
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		Actor actor = actorDeath.getActor();
		if (actor instanceof Player)
		{
			Player player = (Player) actor;
			if (player == client.getLocalPlayer())
			{
				deActivateShuffle();
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		Actor actor = animationChanged.getActor();
		if (actor instanceof NPC)
		{
			switch (((NPC) actor).getId())
			{
				case NpcID.PHOSANIS_NIGHTMARE:
				case NpcID.PHOSANIS_NIGHTMARE_9416:
				case NpcID.PHOSANIS_NIGHTMARE_9417:
				case NpcID.PHOSANIS_NIGHTMARE_9418:
				case NpcID.PHOSANIS_NIGHTMARE_9419:
				case NpcID.PHOSANIS_NIGHTMARE_9420:
				case NpcID.PHOSANIS_NIGHTMARE_9421:
				case NpcID.PHOSANIS_NIGHTMARE_9422:
				case NpcID.PHOSANIS_NIGHTMARE_9423:
				case NpcID.PHOSANIS_NIGHTMARE_9424:
				case NpcID.PHOSANIS_NIGHTMARE_11153:
				case NpcID.PHOSANIS_NIGHTMARE_11154:
				case NpcID.PHOSANIS_NIGHTMARE_11155:
				case NpcID.THE_NIGHTMARE:
				case NpcID.THE_NIGHTMARE_9425:
				case NpcID.THE_NIGHTMARE_9426:
				case NpcID.THE_NIGHTMARE_9427:
				case NpcID.THE_NIGHTMARE_9428:
				case NpcID.THE_NIGHTMARE_9429:
				case NpcID.THE_NIGHTMARE_9430:
				case NpcID.THE_NIGHTMARE_9431:
				case NpcID.THE_NIGHTMARE_9432:
				case NpcID.THE_NIGHTMARE_9433:
				case NpcID.THE_NIGHTMARE_9460:
				case NpcID.THE_NIGHTMARE_9461:
				case NpcID.THE_NIGHTMARE_9462:
				case NpcID.THE_NIGHTMARE_9463:
				case NpcID.THE_NIGHTMARE_9464:
					int animation = animationChanged.getActor().getAnimation();
					switch (animation)
					{
						case 8594:
							attackStyle = ProtectPrayer.MELEE;
							break;
						case 8595:
							attackStyle = ProtectPrayer.MAGE;
							break;
						case 8596:
							attackStyle = ProtectPrayer.RANGE;
							break;
						case 8606:
							if (config.nightmareParasites())
							{
								infoBoxManager.addInfoBox(new Timer(26 * 600, ChronoUnit.MILLIS, vespula, this));
							}
							break;
					}
					break;
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE)
		{
			if (Text.standardize(chatMessage.getMessage()).startsWith(P2_CURSE))
			{
				activateShuffle();
			}
			else if (Text.standardize(chatMessage.getMessage()).startsWith(P2CURSE_END))
			{
				deActivateShuffle();
			}
		}
	}

	private void activateShuffle()
	{
		if (!config.nightmarePrayers() || reorderActive)
		{
			return;
		}
		reorderActive = setPrayerPositions();
		if (reorderActive)
		{
			setPrayerIcons();
		}
	}

	private void deActivateShuffle()
	{
		if (!reorderActive)
		{
			return;
		}
		reorderActive = !resetPrayer();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		if (!reorderActive)
		{
			return;
		}
		if (scriptPostFired.getScriptId() == 461 || scriptPostFired.getScriptId() == 462)
		{
			boolean result = setPrayerPositions();
			if (result)
			{
				setPrayerIcons();
			}
		}
	}

	protected void setOriginalPositions()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (reorderActive)
		{
			if (originalRangePosition != null && originalMeleePosition != null && originalMagePosition != null)
			{
				return;
			}
		}

		Widget widgetMage = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
		Widget widgetRange = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
		Widget widgetMelee = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MELEE);

		if (widgetMage == null || widgetRange == null || widgetMelee == null)
		{
			return;
		}

		originalMagePosition = new Point(widgetMage.getOriginalX(), widgetMage.getOriginalY());
		originalRangePosition = new Point(widgetRange.getOriginalX(), widgetRange.getOriginalY());
		originalMeleePosition = new Point(widgetMelee.getOriginalX(), widgetMelee.getOriginalY());
	}

	protected boolean setPrayerPositions()
	{
		Widget widgetMage = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
		Widget widgetRange = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
		Widget widgetMelee = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MELEE);

		if (widgetMage == null || widgetRange == null || widgetMelee == null ||
			originalMagePosition == null || originalRangePosition == null || originalMeleePosition == null)
		{
			return false;
		}

		// mage -> range
		setWidgetPosition(widgetMage, originalRangePosition.getX(), originalRangePosition.getY());
		// range -> melee
		setWidgetPosition(widgetRange, originalMeleePosition.getX(), originalMeleePosition.getY());
		// melee -> mage
		setWidgetPosition(widgetMelee, originalMagePosition.getX(), originalMagePosition.getY());

		return true;
	}

	protected boolean setPrayerIcons()
	{
		Widget widgetMage = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
		Widget widgetRange = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
		Widget widgetMelee = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MELEE);

		if (widgetMage == null || widgetRange == null || widgetMelee == null)
		{
			return false;
		}

		Widget widgetMageChild = getPrayerIconWidgetChild(widgetMage);
		Widget widgetRangeChild = getPrayerIconWidgetChild(widgetRange);
		Widget widgetMeleeChild = getPrayerIconWidgetChild(widgetMelee);

		if (widgetMageChild == null || widgetRangeChild == null || widgetMeleeChild == null)
		{
			return false;
		}

		setWidgetIcon(widgetMageChild, SpriteID.PRAYER_PROTECT_FROM_MISSILES);
		setWidgetIcon(widgetRangeChild, SpriteID.PRAYER_PROTECT_FROM_MELEE);
		setWidgetIcon(widgetMeleeChild, SpriteID.PRAYER_PROTECT_FROM_MAGIC);

		return true;
	}

	protected boolean resetPrayer()
	{
		Widget widgetMage = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
		Widget widgetRange = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
		Widget widgetMelee = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MELEE);

		if (widgetMage == null || widgetRange == null || widgetMelee == null)
		{
			return false;
		}

		Widget widgetMageChild = getPrayerIconWidgetChild(widgetMage);
		Widget widgetRangeChild = getPrayerIconWidgetChild(widgetRange);
		Widget widgetMeleeChild = getPrayerIconWidgetChild(widgetMelee);

		if (widgetMageChild == null || widgetRangeChild == null || widgetMeleeChild == null)
		{
			return false;
		}

		// range -> mage
		setWidgetPosition(widgetMage, originalMagePosition.getX(), originalMagePosition.getY());
		// melee - > range
		setWidgetPosition(widgetRange, originalRangePosition.getX(), originalRangePosition.getY());
		// mage - > melee
		setWidgetPosition(widgetMelee, originalMeleePosition.getX(), originalMeleePosition.getY());

		setWidgetIcon(widgetMageChild, SpriteID.PRAYER_PROTECT_FROM_MAGIC);
		setWidgetIcon(widgetRangeChild, SpriteID.PRAYER_PROTECT_FROM_MISSILES);
		setWidgetIcon(widgetMeleeChild, SpriteID.PRAYER_PROTECT_FROM_MELEE);

		return true;
	}


	private void setWidgetPosition(final Widget widget, int x, int y)
	{
		final Runnable r = () ->
		{
			widget.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
			widget.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
			widget.setOriginalX(x);
			widget.setOriginalY(y);
			widget.revalidate();
		};
		if (client.isClientThread())
		{
			r.run();
		}
		else
		{
			clientThread.invoke(r);
		}
	}

	private void setWidgetIcon(final Widget widget, int iconId)
	{
		final Runnable r = () ->
		{
			widget.setSpriteId(iconId);
			widget.revalidate();
		};
		if (client.isClientThread())
		{
			r.run();
		}
		else
		{
			clientThread.invoke(r);
		}
	}

	private static Widget getPrayerIconWidgetChild(Widget widget)
	{
		Widget[] children = widget.getDynamicChildren();
		if (children != null && children.length > 1)
		{
			return children[1];
		}
		return null;
	}
}

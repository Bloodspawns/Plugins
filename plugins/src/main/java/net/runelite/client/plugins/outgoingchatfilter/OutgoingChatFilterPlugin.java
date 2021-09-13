package net.runelite.client.plugins.outgoingchatfilter;

import com.google.common.base.CharMatcher;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.VarClientInt;
import net.runelite.api.VarClientStr;
import net.runelite.api.vars.InputType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
		name = "Outgoing Chat filter",
		description = "Filters words for your message before it is send to the server",
		enabledByDefault = false
)
@Singleton
@Slf4j
public class OutgoingChatFilterPlugin extends Plugin implements KeyListener
{
	@Inject
	private Client client;

	@Inject
	private OutgoingChatFilterConfig config;

	@Inject
	private KeyManager keyManager;

	private final CharMatcher jagexPrintableCharMatcher = Text.JAGEX_PRINTABLE_CHAR_MATCHER;
	private static final ArrayList<Pattern> FILTERED_WORDS = new ArrayList<>();
	static final String CONFIG_GROUP = "OutgoingChatFilterConfig";

	@Provides
	OutgoingChatFilterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OutgoingChatFilterConfig.class);
	}

	@Override
	protected void startUp()
	{
		keyManager.registerKeyListener(this);

		parseConfig(config.getWordsToFilter());
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			cleanupText();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(CONFIG_GROUP))
		{
			parseConfig(event.getNewValue());
		}
	}

	private static void parseConfig(String text)
	{
		// only match whole words or words that are prefixed with a forward slash for example that go into cc
		FILTERED_WORDS.clear();
		Text.fromCSV(text).forEach(word ->
				FILTERED_WORDS.add(Pattern.compile("(?<=(^| )|/)" + Pattern.quote(word.trim()) + "(?=$| )", Pattern.CASE_INSENSITIVE)));
		FILTERED_WORDS.sort((m1, m2) -> Integer.compare(m2.pattern().length(), m1.pattern().length()));
	}

	private void cleanupText()
	{
		int inputType = client.getVar(VarClientInt.INPUT_TYPE);
		if (inputType == InputType.PRIVATE_MESSAGE.getType() || inputType == InputType.NONE.getType())
		{
			String text;
			VarClientStr var;
			if (inputType == InputType.PRIVATE_MESSAGE.getType())
			{
				var = VarClientStr.INPUT_TEXT;
			}
			else
			{
				var = VarClientStr.CHATBOX_TYPED_TEXT;
			}
			text = client.getVar(var);
			if (text == null || "".equals(text))
			{
				return;
			}
			String cleanedText = censorMessage(text);

			if (!cleanedText.equals(text))
			{
				client.setVar(var, cleanedText);
			}

			log.debug("text:{}, censored:{}", text, cleanedText);
		}
	}

	private String censorMessage(String message)
	{
		String strippedMessage = jagexPrintableCharMatcher.retainFrom(message)
				.replace('\u00A0', ' ');

		boolean filtered = false;
		for (Pattern pattern : FILTERED_WORDS)
		{
			Matcher m = pattern.matcher(strippedMessage);

			StringBuffer sb = new StringBuffer();

			while (m.find())
			{
				m.appendReplacement(sb, StringUtils.repeat('*', m.group(0).length()));
				filtered = true;
			}
			m.appendTail(sb);

			strippedMessage = sb.toString();
		}

		return filtered ? strippedMessage : message;
	}

	@Override
	public void keyReleased(KeyEvent e)
	{

	}

	@Override
	public void keyTyped(KeyEvent e)
	{

	}
}

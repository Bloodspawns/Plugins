/*
 * Copyright (c) 2017, Seth <Sethtroll3@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.animatedloginscreen;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.BeforeRender;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@PluginDescriptor(
		name = "Animated login screen",
		description = "Use a video as background on the login screen",
		enabledByDefault = false
)
@Slf4j
public class AnimatedLoginScreen extends Plugin
{
	private static final File CUSTOM_LOGIN_SCREEN_VIDEO = new File(RuneLite.RUNELITE_DIR, "loginscreenvid");
	private static final ImmutableSet<String> VALID_EXTENSIONS = ImmutableSet.of("mp4", "mkv", "flv");

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	private Method setLoginScreenBackgroundPixels = null;
	private Method rightSpriteOverwrite = null;
	private Method leftSpriteOverwrite = null;

	private FrameProvider frameProvider;

	boolean offTick = false;

	private boolean hooked()
	{
		return setLoginScreenBackgroundPixels != null && rightSpriteOverwrite != null && leftSpriteOverwrite != null;
	}

	@Override
	protected void startUp()
	{
		hookMethods();
		if (CUSTOM_LOGIN_SCREEN_VIDEO.listFiles() == null)
		{
			return;
		}
		grabVideo();
		clientThread.invoke(() -> client.setShouldRenderLoginScreenFire(false));
	}

	private void grabVideo()
	{
		for (File file : CUSTOM_LOGIN_SCREEN_VIDEO.listFiles())
		{
			boolean validExtension = false;
			for (String extension : VALID_EXTENSIONS)
			{
				if (file.getName().endsWith("." + extension))
				{
					validExtension = true;
					break;
				}
			}

			if (validExtension)
			{
				try
				{
					frameProvider = new FrameProvider();
					frameProvider.loadVideo(file);
					break;
				}
				catch (RuntimeException ignored)
				{
				}
			}
		}
	}

	@Subscribe
	private void onBeforeRender(BeforeRender render)
	{
		if (!hooked())
		{
			return;
		}

		if (client.getGameState() != GameState.LOGIN_SCREEN &&
				client.getGameState() != GameState.LOGIN_SCREEN_AUTHENTICATOR &&
				client.getGameState() != GameState.LOGGING_IN)
		{
			return;
		}

		offTick = !offTick;
		if (offTick)
		{
			return;
		}

		render();
	}

	private void render()
	{
		if (frameProvider == null || !frameProvider.isReady())
		{
			return;
		}

		var image = frameProvider.getFrame();
		var pixels = ImageUtil.getImageSpritePixels(image, client);
		clientThread.invoke(() ->
		{
			try
			{
				setLoginScreenBackgroundPixels.invoke(client, pixels);
				leftSpriteOverwrite.invoke(client);
				rightSpriteOverwrite.invoke(client);
			}
			catch (IllegalAccessException | InvocationTargetException e)
			{
				log.warn("error on invoke", e);
			}
		});
	}

	private void hookMethods()
	{
		for (Method m : client.getClass().getDeclaredMethods())
		{
			switch (m.getName())
			{
				case "setLoginScreenBackgroundPixels":
					setLoginScreenBackgroundPixels = m;
					break;
				case "rightSpriteOverwrite":
					rightSpriteOverwrite = m;
					break;
				case "leftSpriteOverwrite":
					leftSpriteOverwrite = m;
					break;
			}
		}
		if (setLoginScreenBackgroundPixels == null)
		{
			log.error("Couldn't hook methods");
		}
	}

	@Override
	protected void shutDown()
	{
		restoreLoginScreen();
	}

	private void restoreLoginScreen()
	{
		clientThread.invoke(() ->
		{
			client.setLoginScreen(null);
			client.setShouldRenderLoginScreenFire(true);
		});
	}
}
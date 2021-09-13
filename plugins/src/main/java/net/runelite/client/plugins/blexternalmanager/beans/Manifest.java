package net.runelite.client.plugins.blexternalmanager.beans;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

@Value
@Slf4j
public class Manifest
{
	Artifact[] artifacts;

	static Gson gson = new Gson();

	public static Manifest getManifest(String url) throws IOException
	{
		URL u = new URL(url);

		log.debug("Fetching manifest from {}", url);
		URLConnection conn = u.openConnection();

		try (InputStream i = conn.getInputStream())
		{
			byte[] bytes = ByteStreams.toByteArray(i);

			try
			{ // need this try-catch for json parsing errors, mainly occurs with invalid/not raw manifest links
				return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(bytes)), Manifest.class);
			}
			catch (RuntimeException e)
			{
				log.warn("Invalid JSON for {}", url);
				return null;
			}
		}
	}
}

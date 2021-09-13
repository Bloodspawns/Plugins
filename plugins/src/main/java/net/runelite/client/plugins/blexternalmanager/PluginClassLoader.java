package net.runelite.client.plugins.blexternalmanager;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;

public class PluginClassLoader extends URLClassLoader
{
	private final HashSet<URL> urls = new HashSet<>();

	@Override
	protected void addURL(URL url)
	{
		if (!urls.contains(url))
		{
			super.addURL(url);
			urls.add(url);
		}
	}

	public PluginClassLoader(URL[] urls)
	{
		super(urls, PluginClassLoader.class.getClassLoader());
	}
}

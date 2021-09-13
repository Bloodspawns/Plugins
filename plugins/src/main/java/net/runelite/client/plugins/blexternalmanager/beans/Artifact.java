package net.runelite.client.plugins.blexternalmanager.beans;

import com.google.common.base.Strings;
import lombok.Value;

import javax.annotation.Nullable;

@Value
public class Artifact
{
	String name;
	String[] plugins;
	String version;
	String path;
	String hash;
	@Nullable
	String description;

	public boolean isValid()
	{
		return !Strings.isNullOrEmpty(name)
				&& !Strings.isNullOrEmpty(version)
				&& !Strings.isNullOrEmpty(path)
				&& !Strings.isNullOrEmpty(hash);
	}
}

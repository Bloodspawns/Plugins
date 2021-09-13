package net.runelite.client.plugins.blexternalmanager.swing.components;

import lombok.Value;
import net.runelite.client.plugins.blexternalmanager.beans.Artifact;

@Value
public class InstalledArtifact
{
	String manifestUrl;
	Artifact artifact;
}

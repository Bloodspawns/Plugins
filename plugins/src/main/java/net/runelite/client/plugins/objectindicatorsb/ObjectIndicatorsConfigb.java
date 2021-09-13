/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
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

package net.runelite.client.plugins.objectindicatorsb;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("objectindicatorsb")
public interface ObjectIndicatorsConfigb extends Config
{
	@Alpha
	@ConfigItem(
		keyName = "markerColor",
		name = "Marker color",
		description = "Configures the color of object marker"
	)
	default Color markerColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = "rememberObjectColors",
		name = "Remember color per object",
		description = "Color objects using the color from time of marking"
	)
	default boolean rememberObjectColors()
	{
		return false;
	}

	@ConfigItem(
		keyName = "drawHull",
		name = "Draw hull",
		description = "Draw the convex hull of this object"
	)
	default boolean drawHull()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawTile",
		name = "Draw tile",
		description = "Draw the tile of this object"
	)
	default boolean drawTile()
	{
		return false;
	}

	@ConfigItem(
		keyName = "drawOutline",
		name = "Draw outline",
		description = "Draw the outline of this object"
	)
	default boolean drawOutline()
	{
		return false;
	}

	@ConfigItem(
		keyName = "drawOutlineWidth",
		name = "Outline width",
		description = "The width of the outline of this object"
	)
	default int drawOutlineWidth()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "drawOutlineFeather",
		name = "Outline feather",
		description = "The feather of the outline of this object"
	)
	default int drawOutlineFeather()
	{
		return 0;
	}
}

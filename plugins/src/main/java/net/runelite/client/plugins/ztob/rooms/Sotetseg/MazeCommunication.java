package net.runelite.client.plugins.ztob.rooms.Sotetseg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.client.util.Text;

@Slf4j
class MazeCommunication
{
	private static final String topicPrefix = "sotetseg/maze/";

	static String getMqttTopic(String... names)
	{
		StringBuilder nameString = new StringBuilder();
		Arrays.stream(names).forEach(nameString::append);
		return topicPrefix;
	}

	static boolean isMazeComplete(Set<Point> redTiles)
	{
		if (redTiles.isEmpty())
		{
			return false;
		}

		if (redTiles.size() < 15)
		{
			return false;
		}

		HashMap<Integer, HashSet<Integer>> ymap = new HashMap<>();
		for (Point p : redTiles)
		{
			if (ymap.containsKey(p.getY()))
			{
				ymap.get(p.getY()).add(p.getX());
			}
			else
			{
				HashSet<Integer> xset = new HashSet<>();
				xset.add(p.getX());
				ymap.put(p.getY(), xset);
			}
		}

		if (ymap.keySet().size() < 15)
		{
			return false;
		}

		for (int y = 0; y < 14; y+=2)
		{
			if (!ymap.containsKey(y) || !ymap.containsKey(y+1))
			{
				return false;
			}

			boolean connected = false;
			int x = ymap.get(y).iterator().next();
			for (int x1 : ymap.get(y+1))
			{
				if (x1 == x)
				{
					connected = true;
					break;
				}
			}

			if (!connected)
			{
				return false;
			}
		}
		return true;
	}

	static String getMazeSeed(Set<Point> redTiles)
	{
		String[] hex = new String[8];
		for (Point point : redTiles)
		{
			if (point.getY() % 2 == 0)
			{
				hex[point.getY() / 2] = (String.format("%01x", point.getX() & 0xFF));
			}
		}
		return String.join("" ,hex);
	}

	static HashSet<Point> pointSetFromSeed(byte[] payload)
	{
		if (payload.length < 8)
		{
			System.out.println("Payload containing too little points " + payload.length);
			return new HashSet<>();
		}
		String hex = new String(payload);
		HashSet<Point> result = new HashSet<>();
		Point lastPoint = null;
		for (int y = 0; y < 15; y+= 2)
		{
			String hexNumber = hex.substring(y / 2, y / 2 + 1);
			try
			{
				Point p = new Point(Integer.parseInt(hexNumber, 16), y);
				result.add(p);
				if (y > 0)
				{
					int xmin = Math.min(p.getX(), lastPoint.getX());
					int xmax = Math.max(p.getX(), lastPoint.getX());
					for (int x = xmin; x < xmax + 1; x++)
					{
						result.add(new Point(x, y - 1));
					}
				}
				lastPoint = p;
			}
			catch (NumberFormatException n)
			{
				log.debug("Incorrect input for maze, non hexstring character " + hexNumber);
			}
		}
		return result;
	}

	// Seriously fuck you runescape
	static String unfuckName(String name)
	{
		return Text.removeTags(name).replace('\u00A0', ' ');
	}
}

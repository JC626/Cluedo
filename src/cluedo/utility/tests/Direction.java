package cluedo.utility.tests;

import java.util.Random;

public class Direction
{
	private static Random random = new Random();
	
	public static cluedo.utility.Heading.Direction getRandomDirection()
	{
		int len = cluedo.utility.Heading.Direction.values().length;
		return cluedo.utility.Heading.Direction.values()[random.nextInt(len)];
	}
}

package cluedo.utility.tests;

import java.util.Random;

import org.junit.Before;

import cluedo.utility.Heading.Direction;

public class Cell
{
	private static Random random = new Random();
	
	/**
	 * Randomly allocate 0 .. 4 walls to be used with a Cell.
	 * Doing so randomly prevents bias (only having North as a wall, for example). 
	 */
	@Before
	public static Direction[] setupRandomWalls()
	{
		Direction[] walls;
		
		boolean north = random.nextBoolean();
		boolean south = random.nextBoolean();
		boolean east = random.nextBoolean();
		boolean west = random.nextBoolean();
		
		int wallCount = 0;
		int wallIndex = 0;
		
		if (north)
		{
			wallCount++;
		}
		
		if (south)
		{
			wallCount++;
		}
		
		if (east)
		{
			wallCount++;
		}
		
		if (west)
		{
			wallCount++;
		}
		
		walls = new Direction[wallCount];
		
		if (north)
		{
			walls[wallIndex] = Direction.North;
			wallIndex++;
		}
		
		if (south)
		{
			walls[wallIndex] = Direction.South;
			wallIndex++;
		}
		
		if (east)
		{
			walls[wallIndex] = Direction.East;
			wallIndex++;
		}
		
		if (west)
		{
			walls[wallIndex] = Direction.West;
			wallIndex++;
		}
		
		return walls;
	}
}

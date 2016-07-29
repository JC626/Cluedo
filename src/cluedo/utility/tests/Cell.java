package cluedo.utility.tests;

import java.util.Random;

import cluedo.board.Board;
import cluedo.utility.Heading.Direction;

public class Cell
{
	private static Random random = new Random();
	
	public static cluedo.model.Cell getRandomCell()
	{
		// nextInt method returns 0 .. bound with the bound being exclusive,
		// but we want to include the width and height so we add one to each.
		int x = random.nextInt(Board.WIDTH + 1);
		int y = random.nextInt(Board.HEIGHT + 1);
		
		return new cluedo.model.Cell(x, y, getRandomWalls())
		{
			@Override
			public void display()
			{
				System.out.println("TEST random valid cell");
			}
		};
	}
	
	/**
	 * Randomly allocate 0 .. 4 walls to be used with a Cell.
	 * Doing so randomly prevents bias (only having North as a wall, for example). 
	 */
	public static Direction[] getRandomWalls()
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

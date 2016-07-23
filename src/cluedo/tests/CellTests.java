package cluedo.tests;

import cluedo.model.Cell;
import cluedo.model.Displayable;
import cluedo.utility.Heading.Direction;

import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CellTests
{
	private static Displayable validDisplay;
	private static Random random;
	
	private int validX;
	private int validY;
	private Direction[] validWalls;

	@BeforeClass
	public static void setup()
	{
		// The Displayable doesn't need to be setup separately because only the UI calls display().
		validDisplay = () -> {}; // Empty display method.
		
		// Used for random numbers to (e.g. generate walls)
		random = new Random();
	}
	
	/**
	 * Randomly allocate 0 .. 4 walls to this Cell.
	 * Doing so randomly prevents bias (only having North as a wall, for example). 
	 */
	@Before
	public void setupRandomWalls()
	{
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
		
		validWalls = new Direction[wallCount];
		
		if (north)
		{
			validWalls[wallIndex] = Direction.North;
			wallIndex++;
		}
		
		if (south)
		{
			validWalls[wallIndex] = Direction.South;
			wallIndex++;
		}
		
		if (east)
		{
			validWalls[wallIndex] = Direction.East;
			wallIndex++;
		}
		
		if (west)
		{
			validWalls[wallIndex] = Direction.West;
			wallIndex++;
		}
		System.out.println(Arrays.toString(validWalls));
	}
	
	/**
	 * Randomly allocate a positive value to each x and y.
	 */
	@Before
	public void setupXY()
	{
		validX = random.nextInt(Integer.MAX_VALUE);
		validY = random.nextInt(Integer.MAX_VALUE);
	}

	/*
	 * Invalid tests
	 */

	/*
	 * Constructor arguments
	 */
	
	@Test (expected = IllegalArgumentException.class)
	public void xNegative()
	{
		new Cell(-1, validY, validDisplay, validWalls);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void yNegative()
	{
		new Cell(validX, -1, validDisplay, validWalls);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void displayableNull()
	{
		new Cell(validX, validY, null, validWalls);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void wallsNull()
	{
		new Cell(validX, validY, validDisplay, null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void wallsContainingNull()
	{
		Direction[] invalidWalls = {Direction.South, null, Direction.East};

		new Cell(validX, validY, validDisplay, invalidWalls);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void wallsContainingDuplicate()
	{
		Direction[] invalidWalls = {Direction.North, Direction.East, Direction.North};
		new Cell(validX, validY, validDisplay, invalidWalls);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void wallsContainingDuplicateSize()
	{
		Direction[] invalidWalls = {Direction.North, Direction.West, Direction.East, Direction.South, Direction.North};
		new Cell(validX, validY, validDisplay, invalidWalls);
	}
}

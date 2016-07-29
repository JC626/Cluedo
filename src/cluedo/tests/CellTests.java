package cluedo.tests;

import cluedo.model.Cell;
import cluedo.utility.Heading.Direction;

import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CellTests
{
	private static Random random;
	
	private int validX;
	private int validY;
	private Direction[] validWalls;

	@BeforeClass
	public static void setup()
	{
		// Used for random numbers to (e.g. generate walls)
		random = new Random();
	}
	
	@Before
	public void setupWalls()
	{
		validWalls = cluedo.utility.tests.Cell.getRandomWalls();
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
		new Cell(-1, validY, validWalls){public void display(){}};
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void yNegative()
	{
		new Cell(validX, -1, validWalls){public void display(){}};
	}

	@Test (expected = IllegalArgumentException.class)
	public void wallsNull()
	{
		new Cell(validX, validY, null){public void display(){}};
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void wallsContainingNull()
	{
		Direction[] invalidWalls = {Direction.South, null, Direction.East};

		new Cell(validX, validY, invalidWalls){public void display(){}};
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void wallsContainingDuplicate()
	{
		Direction[] invalidWalls = {Direction.North, Direction.East, Direction.North};
		new Cell(validX, validY, invalidWalls){public void display(){}};
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void wallsContainingDuplicateSize()
	{
		Direction[] invalidWalls = {Direction.North, Direction.West, Direction.East, Direction.South, Direction.North};
		new Cell(validX, validY, invalidWalls){public void display(){}};
	}
}

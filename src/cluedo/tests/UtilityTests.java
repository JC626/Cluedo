package cluedo.tests;

import cluedo.utility.Heading;
import cluedo.utility.Heading.Direction;

import static org.junit.Assert.*;
import org.junit.Test;

public class UtilityTests
{
	/*
	 * Here we test the opposite method in Heading, which returns the opposite direction
	 * for some given direction.
	 */
	
	@Test
	public void oppositeNorthIsSouth()
	{
		Direction initial = Direction.North;
		Direction opposite = Direction.South;
		assertEquals(opposite, Heading.opposite(initial));
	}
	
	@Test
	public void oppositeSouthIsNorth()
	{
		Direction initial = Direction.South;
		Direction opposite = Direction.North;
		assertEquals(opposite, Heading.opposite(initial));
	}
	
	@Test
	public void oppositeEastIsWest()
	{
		Direction initial = Direction.East;
		Direction opposite = Direction.West;
		assertEquals(opposite, Heading.opposite(initial));
	}
	
	@Test
	public void oppositeWestIsEast()
	{
		Direction initial = Direction.West;
		Direction opposite = Direction.East;
		assertEquals(opposite, Heading.opposite(initial));
	}
}

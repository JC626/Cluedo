package cluedo.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for directions.
 * As Players cannot legally move diagonally, the only available directions are:
 * North
 * South
 * East
 * West
 * 
 * This class also provides utility methods for acting on directions, such as opposite(Direction)
 * which returns the direction opposite to the one input (North -> South).
 *
 */
public final class Heading
{
	/**
	 * A direction relative to the orientation of the Board.
	 * North is the "top" of the Board.
	 */
	public enum Direction {North, South, East, West};
	
	/**
	 * North -> South,
	 * South -> North,
	 * East -> West,
	 * West -> East
	 * 
	 * @param d The initial direction.
	 * @return The direction opposite to d.
	 */
	public static Direction opposite(Direction d)
	{
		Direction opposite;
		switch (d)
		{
			case North:
				opposite = Direction.South;
				break;
			case South:
				opposite = Direction.North;
				break;
			case East:
				opposite = Direction.West;
				break;
			case West:
				opposite = Direction.East;
				break;
			default:
				throw new IllegalArgumentException("Direction not recognised - internal error");
		}
		
		return opposite;
	}
	
	/**
	 * Converts a character containing
	 * n, s, e, or w into a list of directions. 
	 * @param s The string, must contain at least one of nsew.
	 * @return The resulting list of Directions.
	 * @throws IllegalArgumentException if s contains zero, any other characters.
	 */
	public static List<Direction> convertStringToDirection(String s)
	{
		if (!Pattern.matches("(n|s|e|w)+", s))
		{
			throw new IllegalArgumentException("String must contain only n, s, e, or w");
		}
		
		List<Direction> converted = new ArrayList<Direction>();
		
		for (char charDirection : s.toCharArray())
		{
			charDirection = Character.toLowerCase(charDirection);
			Direction convertedDirection = null;
			
			switch (charDirection)
			{
				case 'n':
					convertedDirection = Direction.North;
					break;
				case 's':
					convertedDirection = Direction.South;
					break;
				case 'e':
					convertedDirection = Direction.East;
					break;
				case 'w':
					convertedDirection = Direction.West;
					break;
			}
			
			converted.add(convertedDirection);
		}
		
		return converted;
	}
}

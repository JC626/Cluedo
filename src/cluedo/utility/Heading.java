package cluedo.utility;

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
}

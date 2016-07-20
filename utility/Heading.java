package cluedo.utility;

public final class Heading
{
	//TODO class, variable, and method comments.
	public enum Direction {North, South, East, West};
	
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

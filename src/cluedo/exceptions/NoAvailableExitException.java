package cluedo.exceptions;

/**
 * This exception is for when all the exits
 * out of a room are blocked so the player
 * cannot exit the room
 *
 */
public class NoAvailableExitException extends Exception 
{
	private static final long serialVersionUID = 1L;

	public NoAvailableExitException(String arg0) 
	{
		super(arg0);
	}
}

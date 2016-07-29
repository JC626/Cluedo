package cluedo.exceptions;

/**
 * This exception is for when the player has remaining moves
 * and therefore must continue moving. 
 */
public class HasRemainingMovesException extends RuntimeException 
{

	private static final long serialVersionUID = 1L;
	
	public HasRemainingMovesException(String message) 
	{
		super(message);
	}
}

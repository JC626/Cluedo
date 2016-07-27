package cluedo.exceptions;

/**
 * This exception is for when the player 
 * cannot move to the new position
 *
 */
public class InvalidMoveException extends Exception{

	private static final long serialVersionUID = 1L;

	public InvalidMoveException(String arg0) {
		super(arg0);
	}

}

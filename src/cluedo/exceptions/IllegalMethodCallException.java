package cluedo.exceptions;

//TODO IllegalMethodCallException description
/**
 * This exception is thrown when a method is called
 * at the wrong time
 *
 */
public class IllegalMethodCallException extends Exception{

	private static final long serialVersionUID = 1L;

	public IllegalMethodCallException(String arg0) {
		super(arg0);
	}
}

package cluedo.exceptions;

/**
 * This exception is for when a method is called
 * when the conditions for calling the method are incorrect
 *
 */
public class IllegalMethodCallException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public IllegalMethodCallException(String arg0) 
	{
		super(arg0);
	}
}

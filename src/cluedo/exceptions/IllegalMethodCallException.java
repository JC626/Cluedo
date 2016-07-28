package cluedo.exceptions;

//TODO IllegalMethodCallException description
/**
 * This exception is for when a method is called
 * when the conditions are incorrect
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

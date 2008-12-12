package se.l4.crayon.osgi.tooling.core;

/**
 * Exception thrown when a failure has occurred.
 *  
 * @author Andreas Holstenson
 *
 */
public class FailException
	extends Exception
{
	public FailException(String msg)
	{
		super(msg);
	}
}

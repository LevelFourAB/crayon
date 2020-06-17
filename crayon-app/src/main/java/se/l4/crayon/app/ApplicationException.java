package se.l4.crayon.app;

/**
 * Exception thrown on failure in starting the application.
 */
public class ApplicationException
	extends RuntimeException
{

	public ApplicationException()
	{
		super();
	}

	public ApplicationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ApplicationException(String message)
	{
		super(message);
	}

	public ApplicationException(Throwable cause)
	{
		super(cause);
	}

}

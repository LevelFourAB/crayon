package se.l4.crayon.services;

/**
 * Exception throw by managed services.
 */
public class ServiceException
	extends RuntimeException
{
	public ServiceException(String message)
	{
		super(message);
	}

	public ServiceException(Throwable cause)
	{
		super(cause);
	}

	public ServiceException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

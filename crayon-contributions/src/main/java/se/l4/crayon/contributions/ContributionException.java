package se.l4.crayon.contributions;

/**
 * Exception thrown when an issue is encountered for contributions.
 */
public class ContributionException
	extends RuntimeException
{
	public ContributionException()
	{
	}

	public ContributionException(String message)
	{
		super(message);
	}

	public ContributionException(Throwable cause)
	{
		super(cause);
	}

	public ContributionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ContributionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

package se.l4.crayon;

/**
 * Exception thrown on failure in configuration of system.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConfigurationException
	extends RuntimeException
{

	public ConfigurationException()
	{
		super();
	}

	public ConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConfigurationException(String message)
	{
		super(message);
	}

	public ConfigurationException(Throwable cause)
	{
		super(cause);
	}
	
}

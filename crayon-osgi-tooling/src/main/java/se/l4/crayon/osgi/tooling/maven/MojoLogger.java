package se.l4.crayon.osgi.tooling.maven;

import org.apache.maven.plugin.logging.Log;

import se.l4.crayon.osgi.tooling.core.Logger;

/**
 * Logger implementation over the logging within Maven.
 * 
 * @author Andreas Holstenson
 *
 */
public class MojoLogger
	implements Logger
{
	private Log log;
	
	public MojoLogger(Log log)
	{
		this.log = log;
	}
	
	public void debug(String msg)
	{
		log.debug(msg);
	}
	
	public void info(String msg)
	{
		log.info(msg);
	}
	
	public void warn(String msg)
	{
		log.warn(msg);
	}
	
	public void error(String msg)
	{
		log.error(msg);
	}
}

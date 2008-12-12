package se.l4.crayon.osgi.tooling.core;

/**
 * Logger interface to allow for different log backends.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Logger
{
	void info(String msg);
	
	void debug(String msg);
	
	void error(String msg);
	
	void warn(String msg);
}

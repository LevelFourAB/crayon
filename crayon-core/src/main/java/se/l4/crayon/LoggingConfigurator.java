package se.l4.crayon;

import com.google.inject.Module;

/**
 * Interface used for defining a configurator for logging, used by 
 * {@link EntryPoint} after module configuration is done (before contributions
 * are made). To use this support implement this class and bind it in a
 * {@link Module}.
 * 
 * @author andreas
 *
 */
public interface LoggingConfigurator
{
	/**
	 * Configure the logging system used.
	 */
	void configure();
}

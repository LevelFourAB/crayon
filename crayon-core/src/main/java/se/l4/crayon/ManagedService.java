package se.l4.crayon;

/**
 * Managed service, service that is started/stopped by the system kernel.
 * Should be marked as a singleton (via {@link com.google.inject.Singleton}
 * annotation).
 * 
 * @author Andreas Holstenson
 *
 */
public interface ManagedService
{
	/** Start service. */
	void start() throws Exception;
	
	/** Stop service. */
	void stop() throws Exception;
}

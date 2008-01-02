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
	void start();
	
	/** Stop service. */
	void stop();
}

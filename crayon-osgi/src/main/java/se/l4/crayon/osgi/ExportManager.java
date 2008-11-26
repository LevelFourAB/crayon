package se.l4.crayon.osgi;

/**
 * Manager of exported OSGi services, used for exporting services so they
 * are visible to other bundles. Services that are annotated with {@link Export}
 * and bound will be exported automatically.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ExportManager 
{
	/**
	 * Export the given class.
	 * 
	 * @param <T>
	 * 		type
	 * @param type
	 * 		interface of type
	 */
	<T> void export(Class<T> type);
	
	/**
	 * Stop exporting the given class.
	 * 
	 * @param type
	 */
	<T> void remove(Class<T> type);
	
	/**
	 * Remove all exported services.
	 */
	void removeAll();
}

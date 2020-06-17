package se.l4.crayon.types;

/**
 * Collector used during initialization of types provided by
 * {@link TypesModule}.
 */
public interface TypeCollector
{
	/**
	 * Add a package in which types should be scanned.
	 */
	void addPackage(String pkgName);

	/**
	 * Add the package of the class to the automatic scanning.
	 *
	 * @param type
	 */
	void addPackage(Class<?> type);
}

package se.l4.crayon.contributions;

import com.google.inject.Module;

/**
 * Set of contributions. Contributions are methods in {@link Module}s that
 * can be easily run as part of the startup of a system.
 */
public interface Contributions
{
	/**
	 * Run all contributions.
	 *
	 */
	void run();

	/**
	 * Run all contributions but activate the given modules first.
	 *
	 * @param modules
	 */
	void run(Module... modules);
}

package se.l4.crayon;

/**
 * Set of contributions. The contributions are bound via 
 * {@link CrayonModule#bindContributions(Class)} or 
 * {@link CrayonBinder#bindContributions(Class)}.
 *  
 * @author Andreas Holstenson
 *
 */
public interface Contributions
{
	/**
	 * Run all contributions.
	 * 
	 */
	void run();
}

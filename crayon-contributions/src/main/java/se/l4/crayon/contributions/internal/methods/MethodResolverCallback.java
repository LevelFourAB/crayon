package se.l4.crayon.contributions.internal.methods;

import se.l4.crayon.contributions.Order;

/**
 * Callback used for naming of methods, used to provide support for
 * {@link Order} in the {@link MethodResolver}.
 */
public interface MethodResolverCallback
{
	/**
	 * Name the method definition so it can be used in {@link Order}.
	 *
	 * @param method
	 * 		definition to name
	 * @return
	 * 		name of definition
	 */
	String getName(MethodDef def);
}

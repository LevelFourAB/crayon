package se.l4.crayon.internal.methods;

import se.l4.crayon.annotation.Order;

/**
 * Callback used for naming of methods, used to provide support for
 * {@link Order} in the {@link MethodResolver}.
 * 
 * @author Andreas Holstenson
 *
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
	
	/**
	 * Retrieve an instance of the given class, used when resolving
	 * dependencies.
	 * 
	 * @param c
	 * @return
	 */
	Object getInstance(Class<?> c);
}

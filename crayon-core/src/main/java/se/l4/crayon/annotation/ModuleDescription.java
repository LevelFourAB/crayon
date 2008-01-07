package se.l4.crayon.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.Binder;

/**
 * Defines that a method describes part of the module, the method will be given
 * {@link Binder} as an argument.
 * 
 * @author Andreas Holstenson
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ModuleDescription
{
	/** Name of module, can be used for ordering. */
	String name() default "";
	
	/**
	 * List of dependencies that this module description has, will make sure
	 * that these modules are processed.
	 *
	 * @return
	 */
	Class<?>[] dependencies() default {};
}

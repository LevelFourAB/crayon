package se.l4.crayon.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to define the order of module configuration and
 * contribution. This annotation takes a several string that describes its
 * preferred ordering.
 * 
 * <ul>
 * 	<li>{@code before:name} - define that the method should run before {@code name}</li>
 * 	<li>{@code after:name} - define that the method should run before {@code name}</li>
 * </ul>
 * 
 * <p>
 * When the name defined in the order can not be found it is ignored.
 * 
 * @author Andreas Holstenson
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Order
{
	String[] value();
}

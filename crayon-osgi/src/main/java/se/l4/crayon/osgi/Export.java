package se.l4.crayon.osgi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for interfaces that should be exported as OSGi services. The interface
 * needs to be bound to an implementation via Guice.
 * 
 * @author Andreas Holstenson
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Export
{

}

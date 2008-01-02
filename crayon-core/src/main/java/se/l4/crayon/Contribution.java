package se.l4.crayon;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for marking a method that it should be run as part of the
 * contribution process of a module. Methods that are marked need to be
 * public. See {@link EntryPoint} for more information.
 * 
 * @author Andreas Holstenson
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Contribution
{

}

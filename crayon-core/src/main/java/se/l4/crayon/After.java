package se.l4.crayon;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Variation of {@link Order} where all dependencies are treated as
 * {@code after:name}. Useful when the name of a service is a constant in
 * a module.
 *
 * <p>
 * Example:
 *
 * <pre>
 * {@literal @After(OtherModule.SERVICE)}
 * {@literal @Contribution}
 * public void contributeStuff() {}
 * </pre>
 *
 * @author Andreas Holstenson
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface After
{
	String[] value();
}

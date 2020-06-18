package se.l4.crayon.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation used for methods in modules that wish to contribute things
 * to the {@link javax.validation.ValidatorFactory} configuration.
 *
 * <pre>
 * {@literal @ValidationContribution}
 * public void contributeValueExtractor(Configuration<?> config, CustomValueExtractor extractor) {
 *   config.addValueExtractor(extractor);
 * }
 * </pre>
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindingAnnotation
public @interface ValidationContribution
{

}

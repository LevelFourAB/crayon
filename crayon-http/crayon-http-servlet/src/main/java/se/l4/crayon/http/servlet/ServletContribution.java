package se.l4.crayon.http.servlet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Marker for contributions that contribute servlet configuration via
 * {@link ServletBinder}.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@BindingAnnotation
@Documented
public @interface ServletContribution
{
}

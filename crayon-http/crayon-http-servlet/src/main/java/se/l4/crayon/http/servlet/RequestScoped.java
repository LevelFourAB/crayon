package se.l4.crayon.http.servlet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.ScopeAnnotation;

/**
 * Indicate that a class is tied to a request and only one instance may exist
 * for any one request.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ScopeAnnotation
@Documented
public @interface RequestScoped
{
}

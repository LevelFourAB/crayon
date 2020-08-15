package se.l4.crayon.http.servlet;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.map.MapIterable;

import se.l4.crayon.contributions.Contributions;
import se.l4.crayon.http.servlet.internal.ServletBinderImpl;
import se.l4.crayon.module.CrayonModule;
import se.l4.crayon.types.TypesModule;
import se.l4.ylem.types.discovery.TypeDiscovery;

/**
 * Module that activates support for servlets. Depending on this module allows
 * for binding up servlets, filters and websockets using {@link ServletBinder}
 * and {@link ServletContribution}.
 *
 * <h2>Using ServletBinder</h2>
 *
 * <pre>
 * public class ExampleModule extends CrayonModule {
 *   protected void configure() {
 *     install(new ServletModule());
 *
 *     // Servlets should be bound as singletons
 *     bind(ExampleServlet.class).in(Scopes.SINGLETON);
 *   }
 *
 *   {@literal @}ServletContribution
 *   public void contributeServlet(ServletBinder binder) {
 *     binder.serve("/*").with(ExampleServlet.class);
 *   }
 * }
 * </pre>
 *
 * <h2>Using annotations and auto-discovery</h2>
 *
 * This module also supports basic use of the {@link WebServlet} and
 * {@link WebFilter} annotations.
 *
 * <pre>
 * public class ExampleModule extends CrayonModule {
 *   protected void configure() {
 *     install(new ServletModule());
 *
 *     // Activate auto-discovery in this package and its sub-packages
 *     autoDiscover();
 *   }
 * }
 *
 * {@literal @}WebServlet("/*")
 * public class ExampleServlet implements Servlet {
 *   ...
 * }
 * </pre>
 *
 * Using auto-discovery requires the types to be visible to this Java module,
 * requiring that the package is exported or is opened in {@code module-info.java}:
 *
 * <pre>
 * module example.module {
 *   requires se.l4.crayon.http.servlet;
 *
 *   .. other things
 *
 *   opens example.module.servlets to se.l4.crayon.http.servlet;
 * }
 * </pre>
 */
public class ServletModule
	extends CrayonModule
{
	@Override
	protected void configure()
	{
		install(new TypesModule());

		// Bind scopes
		bindScope(SessionScoped.class, WebScopes.SESSION);
		bindScope(RequestScoped.class, WebScopes.REQUEST);

		// Bind own services
		bind(ServletBinder.class).to(ServletBinderImpl.class);

		// Bind up the filter annotations
		bindContributions(ServletContribution.class);
		bindContributions(ContextContribution.class);
	}

	@Provides
	@Singleton
	public ServletConfiguration provideServletConfiguration(
		Injector injector,
		@ServletContribution Contributions contributions,
		TypeDiscovery discovery
	)
	{
		ServletBinderImpl binder = new ServletBinderImpl(injector);

		// Automatic configuration
		discoverFilters(discovery, binder);
		discoverServlets(discovery, binder);

		// Contributions
		contributions.run(b -> b.bind(ServletBinder.class).toInstance(binder));

		return binder.toConfig();
	}

	private void discoverServlets(
		TypeDiscovery discovery,
		ServletBinder binder
	)
	{
		for(Class<?> c : discovery.getTypesAnnotatedWith(WebServlet.class))
		{
			if(! Servlet.class.isAssignableFrom(c)) continue;

			WebServlet annotation = c.getAnnotation(WebServlet.class);

			binder
				.serve(allPatterns(annotation.value(), annotation.urlPatterns()))
				.params(resolveParams(annotation.initParams()))
				.asyncSupported(annotation.asyncSupported())
				.with((Class<? extends Servlet>) c);
		}
	}

	private void discoverFilters(
		TypeDiscovery discovery,
		ServletBinder binder
	)
	{
		for(Class<?> c : discovery.getTypesAnnotatedWith(WebServlet.class))
		{
			if(! Filter.class.isAssignableFrom(c)) continue;

			WebFilter annotation = c.getAnnotation(WebFilter.class);

			binder
				.filter(allPatterns(annotation.value(), annotation.urlPatterns()))
				.params(resolveParams(annotation.initParams()))
				.asyncSupported(annotation.asyncSupported())
				.dispatcherTypes(annotation.dispatcherTypes())
				.with((Class<? extends Filter>) c);
		}
	}

	private Iterable<String> allPatterns(String[] first, String[] second)
	{
		return Lists.immutable.of(first).newWithAll(Lists.immutable.of(second));
	}

	private MapIterable<String, String> resolveParams(WebInitParam[] params)
	{
		return Lists.immutable.of(params)
			.toMap(WebInitParam::name, WebInitParam::value);
	}

	@Provides
	public HttpServletRequest provideHttpServletRequest()
	{
		return WebScopes.getRequest();
	}

	@Provides
	public HttpServletResponse provideHttpServletResponse()
	{
		return WebScopes.getResponse();
	}

	@Provides
	public HttpSession provideHttpSession()
	{
		return WebScopes.getRequest().getSession(true);
	}

	@Provides
	public ServletContext provideServletContext()
	{
		return WebScopes.getContext();
	}
}

package se.l4.crayon.http.servlet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;

import com.google.inject.Provider;
import com.google.inject.Scopes;

import org.eclipse.collections.api.map.MapIterable;

/**
 * Binder for filters and servlet. When used with {@link DustFilter} this
 * allows configuring filters and servlets with Java code and Guice injection
 * during creation.
 *
 * <p>
 * Binding a filter:
 * <pre>
 * binder.filter("/*").with(FilterImpl.class);
 * binder.filter("/*").param("param.key", "value").with(FilterImpl.class);
 * </pre>
 *
 * <p>
 * Binding a servlet:
 * <pre>
 * binder.serve("/*").with(ServletImpl.class);
 * binder.serve("/*").param("param.key", "value").with(ServletImpl.class);
 * </pre>
 *
 */
public interface ServletBinder
{
	/**
	 * Start binding of a servlet on a certain path. The path will match using
	 * the same rules as a definition in {@code web.xml}.
	 *
	 * @param paths
	 * @return
	 */
	ServletBuilder serve(String... paths);

	/**
	 * Start binding of a servlet on a certain path. The path will match using
	 * the same rules as a definition in {@code web.xml}.
	 *
	 * @param paths
	 * @return
	 */
	ServletBuilder serve(Iterable<String> paths);

	/**
	 * Start binding of a filter on a certain path. The path will match using
	 * the same rules as a definition in {@code web.xml}.
	 *
	 * @param paths
	 * @return
	 */
	FilterBuilder filter(String... paths);

	/**
	 * Start binding of a filter on a certain path. The path will match using
	 * the same rules as a definition in {@code web.xml}.
	 *
	 * @param paths
	 * @return
	 */
	FilterBuilder filter(Iterable<String> paths);

	/**
	 * Builder for filters.
	 */
	interface FilterBuilder
	{
		/**
		 * Define several parameters to be given to the filter.
		 *
		 * @param params
		 * @return
		 */
		FilterBuilder params(MapIterable<String, String> params);

		/**
		 * Define a parameter to the filter.
		 *
		 * @param key
		 * @param value
		 * @return
		 */
		FilterBuilder param(String key, String value);

		/**
		 * Set if asynchronous operation is supported.
		 *
		 * @param supported
		 * @return
		 */
		FilterBuilder asyncSupported();

		/**
		 * Set if asynchronous operation is supported.
		 *
		 * @param supported
		 * @return
		 */
		FilterBuilder asyncSupported(boolean supported);

		/**
		 * Set the dispatcher types that the filter will apply to. Defaults to
		 * {@link DispatcherType#REQUEST}.
		 *
		 * @param types
		 * @return
		 */
		FilterBuilder dispatcherTypes(DispatcherType... types);

		/**
		 * Define the filter to bind. This method must be called last as it
		 * will register the definition. The filter should be scoped as
		 * {@link Scopes#SINGLETON}.
		 *
		 * @param type
		 */
		void with(Class<? extends Filter> type);

		/**
		 * Define a provider to use to get an instance of the filter.
		 *
		 * @param provider
		 */
		<T extends Filter> void with(Class<T> type, Provider<T> provider);

		/**
		 * Define an already created instance to use.
		 *
		 * @param instance
		 */
		void with(Filter instance);
	}

	/**
	 * Builder for servlets.
	 */
	interface ServletBuilder
	{
		/**
		 * Define several parameters to be given to the servlet.
		 *
		 * @param params
		 * @return
		 */
		ServletBuilder params(MapIterable<String, String> params);

		/**
		 * Define a parameter to the servlet.
		 *
		 * @param key
		 * @param value
		 * @return
		 */
		ServletBuilder param(String key, String value);

		/**
		 * Set if asynchronous operation is supported.
		 *
		 * @param supported
		 * @return
		 */
		ServletBuilder asyncSupported();

		/**
		 * Set if asynchronous operation is supported.
		 *
		 * @param supported
		 * @return
		 */
		ServletBuilder asyncSupported(boolean supported);

		/**
		 * Define the servlet to bind. This method must be called last as it
		 * will register the definition. The servlet should be scoped as
		 * {@link Scopes#SINGLETON}.
		 *
		 * @param type
		 */
		void with(Class<? extends Servlet> type);

		/**
		 * Define a provider to use to get an instance of the servlet.
		 *
		 * @param provider
		 */
		<T extends Servlet> void with(Class<T> type, Provider<T> provider);

		/**
		 * Define an already created instance to use.
		 *
		 * @param instance
		 */
		void with(Servlet instance);
	}
}

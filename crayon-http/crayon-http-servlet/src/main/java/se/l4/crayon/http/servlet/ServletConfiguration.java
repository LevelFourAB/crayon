package se.l4.crayon.http.servlet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;

import com.google.inject.Provider;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.MapIterable;

public interface ServletConfiguration
{
	ListIterable<BoundServlet> getServlets();

	ListIterable<BoundFilter> getFilters();

	interface BoundServlet
	{
		/**
		 * Get the name of the servlet.
		 *
		 * @return
		 */
		String getName();

		/**
		 * Get the class of the servlet.
		 *
		 * @return
		 */
		Class<? extends Servlet> getType();

		/**
		 * Get the provider that creates this servlet.
		 *
		 * @return
		 */
		Provider<? extends Servlet> getProvider();

		/**
		 * Get the URL patterns of the servlet.
		 *
		 * @return
		 */
		ListIterable<String> getUrlPatterns();

		/**
		 * Get the parameters used to initialize the servlet.
		 *
		 * @return
		 */
		MapIterable<String, String> getInitParams();

		/**
		 * Get if async is supported.
		 *
		 * @return
		 */
		boolean isAsyncSupported();
	}

	interface BoundFilter
	{
		/**
		 * Get the name of the filter.
		 *
		 * @return
		 */
		String getName();

		/**
		 * Get the class of the filter.
		 *
		 * @return
		 */
		Class<? extends Filter> getType();

		/**
		 * Get the provider that creates this filter.
		 *
		 * @return
		 */
		Provider<? extends Filter> getProvider();

		/**
		 * Get the URL patterns of the filter.
		 *
		 * @return
		 */
		ListIterable<String> getUrlPatterns();

		/**
		 * Get the parameters used to initialize the filter.
		 *
		 * @return
		 */
		MapIterable<String, String> getInitParams();

		/**
		 * Get if async is supported.
		 *
		 * @return
		 */
		boolean isAsyncSupported();

		/**
		 * Get the dispatcher types that the filter applies to.
		 *
		 * @return
		 */
		DispatcherType[] getDispatcherTypes();
	}
}

package se.l4.crayon.http.servlet.internal;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

import se.l4.crayon.http.servlet.ServletConfiguration;

public class ServletConfigurationImpl
	implements ServletConfiguration
{
	private final ImmutableList<BoundFilter> filters;
	private final ImmutableList<BoundServlet> servlets;

	public ServletConfigurationImpl(
		ImmutableList<BoundFilter> filters,
		ImmutableList<BoundServlet> servlets
	)
	{
		this.filters = filters;
		this.servlets = servlets;
	}

	@Override
	public ListIterable<BoundFilter> getFilters()
	{
		return filters;
	}

	@Override
	public ListIterable<BoundServlet> getServlets()
	{
		return servlets;
	}
}

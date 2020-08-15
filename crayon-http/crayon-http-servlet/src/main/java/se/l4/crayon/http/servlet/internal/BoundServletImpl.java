package se.l4.crayon.http.servlet.internal;

import javax.servlet.Servlet;

import com.google.inject.Provider;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MapIterable;

import se.l4.crayon.http.servlet.ServletConfiguration;

public class BoundServletImpl
	implements ServletConfiguration.BoundServlet
{
	private final String name;
	private final Class<? extends Servlet> type;
	private final Provider<? extends Servlet> provider;
	private final ImmutableList<String> urlPatterns;
	private final ImmutableMap<String, String> initParams;
	private final boolean asyncSupported;

	public BoundServletImpl(
		String name,
		Class<? extends Servlet> type,
		Provider<? extends Servlet> provider,
		ImmutableList<String> urlPatterns,
		ImmutableMap<String, String> initParams,
		boolean asyncSupported
	)
	{
		this.name = name;
		this.type = type;
		this.provider = provider;
		this.urlPatterns = urlPatterns;
		this.initParams = initParams;
		this.asyncSupported = asyncSupported;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Class<? extends Servlet> getType()
	{
		return type;
	}

	@Override
	public Provider<? extends Servlet> getProvider()
	{
		return provider;
	}

	@Override
	public ListIterable<String> getUrlPatterns()
	{
		return urlPatterns;
	}

	@Override
	public MapIterable<String, String> getInitParams()
	{
		return initParams;
	}

	@Override
	public boolean isAsyncSupported()
	{
		return asyncSupported;
	}
}

package se.l4.crayon.http.servlet.internal;

import java.util.Arrays;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import com.google.inject.Provider;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MapIterable;

import se.l4.crayon.http.servlet.ServletConfiguration;

public class BoundFilterImpl
	implements ServletConfiguration.BoundFilter
{
	private final String name;
	private final Class<? extends Filter> type;
	private final Provider<? extends Filter> provider;
	private final ImmutableList<String> urlPatterns;
	private final ImmutableMap<String, String> initParams;
	private final boolean asyncSupported;
	private final DispatcherType[] dispatcherTypes;

	public BoundFilterImpl(
		String name,
		Class<? extends Filter> type,
		Provider<? extends Filter> provider,
		ImmutableList<String> urlPatterns,
		ImmutableMap<String, String> initParams,
		boolean asyncSupported,
		DispatcherType[] dispatcherTypes
	)
	{
		this.name = name;
		this.type = type;
		this.provider = provider;
		this.urlPatterns = urlPatterns;
		this.initParams = initParams;
		this.asyncSupported = asyncSupported;
		this.dispatcherTypes = dispatcherTypes;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Class<? extends Filter> getType()
	{
		return type;
	}

	@Override
	public Provider<? extends Filter> getProvider()
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

	@Override
	public DispatcherType[] getDispatcherTypes()
	{
		return Arrays.copyOf(dispatcherTypes, dispatcherTypes.length);
	}
}

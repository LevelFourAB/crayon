package se.l4.crayon.http.servlet.internal;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;

import com.google.inject.Injector;
import com.google.inject.Provider;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MapIterable;

import se.l4.crayon.http.servlet.ServletBinder;
import se.l4.crayon.http.servlet.ServletConfiguration;
import se.l4.crayon.http.servlet.ServletConfiguration.BoundFilter;
import se.l4.crayon.http.servlet.ServletConfiguration.BoundServlet;

public class ServletBinderImpl
	implements ServletBinder
{
	private final Injector injector;
	private final MutableList<BoundServlet> servlets;
	private final MutableList<BoundFilter> filters;

	public ServletBinderImpl(Injector injector)
	{
		this.injector = injector;

		servlets = Lists.mutable.empty();
		filters = Lists.mutable.empty();
	}

	@Override
	public FilterBuilder filter(String... path)
	{
		return filter(Lists.immutable.of(path));
	}

	@Override
	public FilterBuilder filter(Iterable<String> paths)
	{
		return new FilterBuilderImpl(
			Lists.immutable.withAll(paths),
			Maps.immutable.empty(),
			false,
			new DispatcherType[] { DispatcherType.REQUEST }
		);
	}

	@Override
	public ServletBuilder serve(String... paths)
	{
		return serve(Lists.immutable.of(paths));
	}

	@Override
	public ServletBuilder serve(Iterable<String> paths)
	{
		return new ServletBuilderImpl(
			Lists.immutable.withAll(paths),
			Maps.immutable.empty(),
			false
		);
	}

	public ServletConfiguration toConfig()
	{
		return new ServletConfigurationImpl(
			filters.toImmutable(),
			servlets.toImmutable()
		);
	}

	private class ServletBuilderImpl
		implements ServletBuilder
	{
		private final ImmutableList<String> path;
		private final ImmutableMap<String, String> initParams;
		private final boolean asyncSupported;

		public ServletBuilderImpl(
			ImmutableList<String> path,
			ImmutableMap<String, String> initParams,
			boolean asyncSupported
		)
		{
			this.path = path;
			this.asyncSupported = asyncSupported;
			this.initParams = initParams;
		}

		@Override
		public ServletBuilder param(String key, String value)
		{
			return new ServletBuilderImpl(
				path,
				initParams.newWithKeyValue(key, value),
				asyncSupported
			);
		}

		@Override
		public ServletBuilder params(MapIterable<String, String> params)
		{
			return new ServletBuilderImpl(
				path,
				initParams.newWithAllKeyValues(params.keyValuesView()),
				asyncSupported
			);
		}

		@Override
		public ServletBuilder asyncSupported()
		{
			return new ServletBuilderImpl(
				path,
				initParams,
				true
			);
		}

		@Override
		public ServletBuilder asyncSupported(boolean supported)
		{
			return new ServletBuilderImpl(
				path,
				initParams,
				supported
			);
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void with(Class<? extends Servlet> type)
		{
			Provider<? extends Servlet> provider = injector.getProvider(type);
			with((Class) type, provider);
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void with(Servlet instance)
		{
			with((Class) instance.getClass(), () -> instance);
		}

		@Override
		public <T extends Servlet> void with(Class<T> type, Provider<T> provider)
		{
			servlets.add(new BoundServletImpl(
				type.getSimpleName() + servlets.size(),
				type,
				provider,
				path,
				initParams,
				asyncSupported
			));
		}
	}

	private class FilterBuilderImpl
		implements FilterBuilder
	{
		private final ImmutableList<String> path;
		private final ImmutableMap<String, String> initParams;
		private final boolean asyncSupported;
		private final DispatcherType[] dispatcherTypes;

		public FilterBuilderImpl(
			ImmutableList<String> path,
			ImmutableMap<String, String> initParams,
			boolean asyncSupported,
			DispatcherType[] dispatcherTypes
		)
		{
			this.path = path;
			this.asyncSupported = asyncSupported;
			this.initParams = initParams;
			this.dispatcherTypes = dispatcherTypes;
		}

		@Override
		public FilterBuilder param(String key, String value)
		{
			return new FilterBuilderImpl(
				path,
				initParams.newWithKeyValue(key, value),
				asyncSupported,
				dispatcherTypes
			);
		}

		@Override
		public FilterBuilder params(MapIterable<String, String> params)
		{
			return new FilterBuilderImpl(
				path,
				initParams.newWithAllKeyValues(params.keyValuesView()),
				asyncSupported,
				dispatcherTypes
			);
		}

		@Override
		public FilterBuilder asyncSupported()
		{
			return new FilterBuilderImpl(
				path,
				initParams,
				true,
				dispatcherTypes
			);
		}

		@Override
		public FilterBuilder asyncSupported(boolean supported)
		{
			return new FilterBuilderImpl(
				path,
				initParams,
				supported,
				dispatcherTypes
			);
		}

		@Override
		public FilterBuilder dispatcherTypes(DispatcherType... types)
		{
			return new FilterBuilderImpl(
				path,
				initParams,
				asyncSupported,
				types
			);
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void with(Class<? extends Filter> type)
		{
			Provider<? extends Filter> provider = injector.getProvider(type);
			with((Class) type, provider);
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void with(Filter instance)
		{
			with((Class) instance.getClass(), () -> instance);
		}

		@Override
		public <T extends Filter> void with(Class<T> type, Provider<T> provider)
		{
			filters.add(new BoundFilterImpl(
				type.getSimpleName() + servlets.size(),
				type,
				provider,
				path,
				initParams,
				asyncSupported,
				dispatcherTypes
			));
		}
	}
}

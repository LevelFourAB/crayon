package se.l4.crayon.http.server.undertow;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceHandle;
import reactor.core.publisher.Mono;
import se.l4.crayon.http.servlet.ServletConfiguration;
import se.l4.crayon.services.ManagedService;
import se.l4.crayon.services.RunningService;
import se.l4.crayon.services.ServiceException;

@Singleton
public class UndertowService
	implements ManagedService
{
	private final ServletConfiguration servletConfig;
	private final UndertowConfig config;

	@Inject
	public UndertowService(
		ServletConfiguration servletConfig,
		UndertowConfig config
	)
	{
		this.config = config;
		this.servletConfig = servletConfig;
	}

	@Override
	public Mono<RunningService> start()
	{
		return Mono.fromSupplier(() -> {
			DeploymentInfo servletBuilder = Servlets
				.deployment()
				.setDefaultEncoding("UTF-8")
				.setContextPath("/")
				.setDeploymentName("main")
				.setClassLoader(getClass().getClassLoader());

			addFilters(servletBuilder);
			addServlets(servletBuilder);

			DeploymentManager manager = Servlets.defaultContainer()
				.addDeployment(servletBuilder);

			manager.deploy();

			PathHandler path = Handlers.path();
			try
			{
				path.addPrefixPath("/", manager.start());
			}
			catch(ServletException e)
			{
				throw new ServiceException("Unable to create servlet environment; " + e.getMessage(), e);
			}

			Undertow.Builder builder = Undertow.builder()
				.setServerOption(UndertowOptions.ENABLE_HTTP2, true)
				.setHandler(path)
				.addHttpListener(config.getPort(), "0.0.0.0");

			Undertow server = builder.build();
			return RunningService.stoppable(server::stop);
		});
	}

	@Override
	public String toString()
	{
		return "Undertow HTTP server, port " + config.getPort();
	}

	/**
	 * Get all of the configured servlets and make them available using
	 * Undertow.
	 *
	 * @param info
	 */
	private void addServlets(DeploymentInfo info)
	{
		for(ServletConfiguration.BoundServlet servlet : servletConfig.getServlets())
		{
			ServletInfo servletInfo = Servlets.servlet(
				servlet.getName(),
				servlet.getType(),
				new ProviderInstanceFactory<>(servlet.getProvider())
			)
				.setAsyncSupported(servlet.isAsyncSupported());

			servlet.getInitParams().forEachKeyValue(servletInfo::addInitParam);

			info.addServlet(servletInfo);

			for(String path : servlet.getUrlPatterns())
			{
				info.addFilterServletNameMapping(
					servlet.getName(),
					path,
					DispatcherType.REQUEST
				);
			}
		}
	}

	/**
	 * Get all of the configured filters and make them available using
	 * Undertow.
	 *
	 * @param info
	 * @return
	 */
	private void addFilters(DeploymentInfo info)
	{
		for(ServletConfiguration.BoundFilter filter : servletConfig.getFilters())
		{
			FilterInfo filterInfo = Servlets.filter(
				filter.getName(),
				filter.getType(),
				new ProviderInstanceFactory<>(filter.getProvider())
			)
				.setAsyncSupported(filter.isAsyncSupported());

			filter.getInitParams().forEachKeyValue(filterInfo::addInitParam);

			info.addFilter(filterInfo);

			for(String path : filter.getUrlPatterns())
			{
				for(DispatcherType dt : filter.getDispatcherTypes())
				{
					info.addFilterServletNameMapping(
						filter.getName(),
						path,
						dt
					);
				}
			}
		}
	}

	private static class ProviderInstanceFactory<T>
		implements InstanceFactory<T>
	{
		private final Provider<T> provider;

		public ProviderInstanceFactory(Provider<T> provider)
		{
			this.provider = provider;
		}

		@Override
		public InstanceHandle<T> createInstance()
			throws InstantiationException
		{
			T instance = provider.get();
			return new ImmediateInstanceHandle<T>(instance);
		}
	}
}

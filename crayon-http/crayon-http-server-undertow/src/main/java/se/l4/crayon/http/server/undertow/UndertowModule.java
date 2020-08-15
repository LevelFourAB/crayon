package se.l4.crayon.http.server.undertow;

import com.google.inject.name.Named;

import se.l4.crayon.module.CrayonModule;
import se.l4.crayon.services.ServiceCollector;
import se.l4.crayon.services.ServiceContribution;

/**
 * Module that provides a HTTP server powered by Undertow.
 */
public class UndertowModule
	extends CrayonModule
{
	@Override
	protected void configure()
	{
		bindConfig(UndertowConfig.class)
			.withDefault(new UndertowConfig())
			.to("http.server");
	}

	@ServiceContribution
	@Named("http-server")
	public void contributeService(
		ServiceCollector collector,
		UndertowService service
	)
	{
		collector.add(service);
	}
}

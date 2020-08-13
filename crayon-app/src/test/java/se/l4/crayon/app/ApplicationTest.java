package se.l4.crayon.app;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.inject.Singleton;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;
import se.l4.crayon.module.CrayonModule;
import se.l4.crayon.services.ManagedService;
import se.l4.crayon.services.RunningService;
import se.l4.crayon.services.ServiceCollector;
import se.l4.crayon.services.ServiceContribution;
import se.l4.crayon.services.ServiceManager;
import se.l4.crayon.services.ServiceStatus;

public class ApplicationTest
{
	@Test
	public void testStartupWithService()
	{
		Application app = Application.create("test")
			.add(new TestModule())
			.start();

		ServiceManager manager = app.getInjector().getInstance(ServiceManager.class);
		ServiceStatus info = manager.get(TestService.class).block();
		assertThat(info.getState(), is(ServiceStatus.State.RUNNING));
	}

	public static class TestModule
		extends CrayonModule
	{
		@ServiceContribution
		public void contributeService(ServiceCollector collector, TestService service)
		{
			collector.add(service);
		}
	}

	@Singleton
	public static class TestService
		implements ManagedService
	{
		private boolean started;

		public Mono<RunningService> start()
		{
			return Mono.just(RunningService.unstoppable());
		}
	}
}

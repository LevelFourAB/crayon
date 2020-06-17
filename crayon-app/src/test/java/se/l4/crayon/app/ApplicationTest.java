package se.l4.crayon.app;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.inject.Singleton;

import org.junit.Test;

import se.l4.crayon.module.CrayonModule;
import se.l4.crayon.services.ManagedService;
import se.l4.crayon.services.ServiceCollector;
import se.l4.crayon.services.ServiceContribution;
import se.l4.crayon.services.ServiceEncounter;
import se.l4.crayon.services.ServiceInfo;
import se.l4.crayon.services.ServiceManager;
import se.l4.crayon.services.ServiceStatus;

public class ApplicationTest
{
	@Test
	public void test()
	{
		Application app = Application.withIdentifier("test")
			.add(new TestModule())
			.start();

		ServiceManager manager = app.getInjector().getInstance(ServiceManager.class);
		ServiceInfo info = manager.get(TestService.class).get();
		assertThat(info.getStatus(), is(ServiceStatus.RUNNING));
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

		public void start(ServiceEncounter e)
		{
			started = true;
		}

		public void stop()
		{
			started = false;
		}
	}
}
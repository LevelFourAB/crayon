package se.l4.crayon.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.junit.Test;

import se.l4.crayon.contributions.ContributionsBinder;
import se.l4.exoconf.Config;

public class ConfigModuleTest
{
	@Test
	public void testNoPaths()
	{
		Injector injector = Guice.createInjector(new ConfigModule());

		Config config = injector.getInstance(Config.class);
		assertThat(config, notNullValue());
	}

	@Test
	public void testContribution()
	{
		Injector injector = Guice.createInjector(
			new ConfigModule(),
			new ContributionTestModule()
		);

		Config config = injector.getInstance(Config.class);
		assertThat(config, notNullValue());

		assertThat(config.get("test", boolean.class).get(), is(true));
	}

	public static class ContributionTestModule
		implements Module
	{
		@Override
		public void configure(Binder binder)
		{
			ContributionsBinder.newBinder(binder, this);
		}

		@ConfigContribution
		public void contributeConfig(ConfigCollector collector)
		{
			collector.addProperty("test", true);
		}
	}
}

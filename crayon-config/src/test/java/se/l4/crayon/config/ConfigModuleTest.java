package se.l4.crayon.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.junit.Test;

import se.l4.commons.config.Config;
import se.l4.commons.config.ConfigBuilder;
import se.l4.crayon.contributions.ContributionsBinder;

public class ConfigModuleTest
{
	@Test
	public void testNoPaths()
	{
		Injector injector = Guice.createInjector(new ConfigModule(
			Collections.emptyList()
		));

		Config config = injector.getInstance(Config.class);
		assertThat(config, notNullValue());
	}

	@Test
	public void testContribution()
	{
		Injector injector = Guice.createInjector(
			new ConfigModule(Collections.emptyList()),
			new ContributionTestModule()
		);

		Config config = injector.getInstance(Config.class);
		assertThat(config, notNullValue());

		assertThat(config.asObject("test", boolean.class), is(true));
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
		public void contributeConfig(ConfigBuilder builder)
		{
			builder.with("test", true);
		}
	}
}

package se.l4.crayon.test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;

/**
 * Basic test for running startup and shutdown methods.
 *
 * @author andreas
 *
 */
public class ContributionsTest
{
	@Test
	public void testStartup()
	{
		TestModule testModule = new TestModule();

		Injector i = Guice.createInjector(testModule);

		Contributions c = i.getInstance(Key.get(Contributions.class, TestAnnotation.class));
		c.run();

		assert testModule.ranStartup : "Startup function not run";
	}

	@Test
	public void testWithExtraBinding()
	{
		TestModule2 testModule = new TestModule2();

		Injector i = Guice.createInjector(testModule);

		Contributions c = i.getInstance(Key.get(Contributions.class, TestAnnotation.class));
		c.run(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				bind(String.class).annotatedWith(Names.named("cookie")).toInstance("hello");
			}
		});

		assert testModule.ranStartup : "Startup function not run";
	}

	public static class TestModule
		extends CrayonModule
	{
		private boolean ranStartup;

		@Override
		protected void configure()
		{
			bindContributions(TestAnnotation.class);
		}

		@TestAnnotation
		public void startup()
		{
			ranStartup = true;
		}
	}

	public static class TestModule2
		extends CrayonModule
	{
		private boolean ranStartup;

		@Override
		protected void configure()
		{
			bindContributions(TestAnnotation.class);
		}

		@TestAnnotation
		public void startup(@Named("cookie") String cookie)
		{
			ranStartup = true;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Qualifier
	public @interface TestAnnotation
	{
	}
}

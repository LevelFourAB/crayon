package se.l4.crayon.contributions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import org.junit.Test;

/**
 * Basic test for running startup and shutdown methods.
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

		assertThat(testModule.ranStartup, is(true));
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

		assertThat(testModule.ranStartup, is(true));
	}

	@Test
	public void testExtraBindingWithDependency()
	{
		TestModule3 testModule = new TestModule3();

		Injector i = Guice.createInjector(new TestModule(), testModule);

		Contributions c = i.getInstance(Key.get(Contributions.class, TestAnnotation.class));
		c.run(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				bind(String.class).annotatedWith(Names.named("cookie")).toInstance("hello");
			}
		});

		assertThat(testModule.singleton, notNullValue());
		assertThat(i.getInstance(TestSingleton.class), is(testModule.singleton));
	}

	public static class TestModule
		extends AbstractModule
	{
		private boolean ranStartup;

		@Override
		protected void configure()
		{
			ContributionsBinder.newBinder(this.binder(), this)
				.bindContributions(TestAnnotation.class);
		}

		@TestAnnotation
		public void startup()
		{
			ranStartup = true;
		}
	}

	public static class TestModule2
		extends AbstractModule
	{
		private boolean ranStartup;

		@Override
		protected void configure()
		{
			ContributionsBinder.newBinder(this.binder(), this)
				.bindContributions(TestAnnotation.class);
		}

		@TestAnnotation
		public void startup(@Named("cookie") String cookie)
		{
			ranStartup = "hello".equals(cookie);
		}
	}

	public static class TestModule3
		extends AbstractModule
	{
		private TestSingleton singleton;

		@Override
		protected void configure()
		{
			ContributionsBinder.newBinder(this.binder(), this);
		}

		@TestAnnotation
		public void startup(
			@Named("cookie") String cookie,
			TestSingleton singleton
		)
		{
			this.singleton = singleton;
		}
	}

	@Singleton
	public static class TestSingleton
	{
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Qualifier
	public @interface TestAnnotation
	{
	}
}

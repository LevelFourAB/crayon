package se.l4.crayon.test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

import org.testng.annotations.Test;

import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

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
	
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Qualifier
	public @interface TestAnnotation
	{
	}
}

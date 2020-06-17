package se.l4.crayon.contributions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import org.junit.Test;

public class DependencyResolverTest
{
	@Test
	public void testABCD()
	{
		String A = "A";
		String B = "B";
		String C = "C";
		String D = "D";

		DependencyResolver<String> resolver = new DependencyResolver<String>();

		resolver.addDependency(A, B);
		resolver.addDependency(C, D);
		resolver.addDependency(B, D);
		resolver.addDependency(C, A);

		Set<String> result = resolver.getOrder();

		assertThat(result.toArray(), is(new Object[] { D, B, A, C }));
	}

	@Test
	public void testDependencies()
	{
		String A = "A";
		String B = "B";
		String C = "C";
		String D = "D";

		DependencyResolver<String> resolver = new DependencyResolver<String>();

		resolver.addDependency(A, B);
		resolver.addDependency(C, D);
		resolver.addDependency(B, D);
		resolver.addDependency(C, A);

		Set<String> result = resolver.getDependencies(A);

		assertThat(result.toArray(), is(new Object[] { D, B }));
	}
}

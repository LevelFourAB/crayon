package se.l4.crayon.test;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import se.l4.crayon.internal.DependencyResolver;

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
		
		Assert.assertEquals(result.toArray(), new Object[] { D, B, A, C }, 
			"Arrays are not equal");
	}
}

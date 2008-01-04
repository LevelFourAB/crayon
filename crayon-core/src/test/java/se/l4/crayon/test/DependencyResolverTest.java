package se.l4.crayon.test;

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import se.l4.crayon.internal.DependencyData;
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
		
		DependencyResolver resolver = new DependencyResolver();
		
		
		DependencyData dA = new DependencyData(A);
		DependencyData dB = new DependencyData(B);
		DependencyData dC = new DependencyData(C);
		DependencyData dD = new DependencyData(D);
		
		dA.addDependency(dB);
		dC.addDependency(dD);
		dB.addDependency(dD);
		dC.addDependency(dA);
		
		Set<DependencyData> data = new HashSet<DependencyData>();
		data.add(dA);
		data.add(dC);
		
		Set<Object> result = resolver.getDependencyOrder(data);
		
		Assert.assertEquals(result.toArray(), new Object[] { D, B, A, C }, 
			"Arrays are not equal");
	}
}

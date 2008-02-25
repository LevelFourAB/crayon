/*
 * Copyright 2008 Andreas Holstenson
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

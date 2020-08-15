package se.l4.crayon.types;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.jupiter.api.Test;

import se.l4.ylem.types.discovery.TypeDiscovery;

public class TypesModuleTest
{
	@Test
	public void testNoSetBind()
	{
		Injector injector = Guice.createInjector(new TypesModule());
		injector.getInstance(TypeDiscovery.class);
	}
}

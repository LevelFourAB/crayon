package se.l4.crayon.types;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import se.l4.crayon.contributions.Contributions;
import se.l4.crayon.module.CrayonModule;
import se.l4.ylem.types.discovery.TypeDiscovery;
import se.l4.ylem.types.instances.InstanceFactory;
import se.l4.ylem.types.instances.guice.InstanceFactoryModule;

/**
 * Module for activating supports for type related things, such as {@link TypeFinder}
 * and {@link InstanceFactory}.
 */
public class TypesModule
	extends CrayonModule
{
	@Override
	public void configure()
	{
		install(new InstanceFactoryModule());

		bindContributions(TypeContribution.class);
	}

	@Singleton
	@Provides
	public TypeDiscovery provideTypeDiscovery(
		@TypeContribution Contributions contributions,
		InstanceFactory instanceFactory
	)
	{
		CollectorImpl impl = new CollectorImpl();
		contributions.run(binder -> binder.bind(TypeCollector.class).toInstance(impl));

		return TypeDiscovery.create()
			.setInstanceFactory(instanceFactory)
			.addPackages(impl.packages)
			.build();
	}

	private static class CollectorImpl
		implements TypeCollector
	{
		private final Set<String> packages;

		public CollectorImpl()
		{
			packages = new HashSet<>();
		}

		@Override
		public void addPackage(String pkgName)
		{
			packages.add(pkgName);
		}

		@Override
		public void addPackage(Class<?> type)
		{
			packages.add(type.getPackageName());
		}
	}
}

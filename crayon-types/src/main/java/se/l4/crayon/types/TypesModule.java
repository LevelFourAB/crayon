package se.l4.crayon.types;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import se.l4.crayon.contributions.Contributions;
import se.l4.crayon.contributions.ContributionsBinder;
import se.l4.ylem.types.discovery.TypeDiscovery;
import se.l4.ylem.types.instances.InstanceFactory;
import se.l4.ylem.types.instances.guice.InstanceFactoryModule;

/**
 * Module for activating supports for type related things, such as {@link TypeFinder}
 * and {@link InstanceFactory}.
 */
public class TypesModule
	implements Module
{
	public void configure(Binder binder)
	{
		binder.install(new InstanceFactoryModule());

		Multibinder.newSetBinder(binder, String.class, Names.named("crayon-type-discovery"));

		ContributionsBinder.newBinder(binder).bindContributions(TypeContribution.class);
	}

	@Singleton
	@Provides
	public TypeDiscovery provideTypeDiscovery(
		@TypeContribution Contributions contributions,
		@Named("crayon-type-discovery") Set<String> packages,
		InstanceFactory instanceFactory
	)
	{
		CollectorImpl impl = new CollectorImpl();
		contributions.run(binder -> binder.bind(TypeCollector.class).toInstance(impl));

		return TypeDiscovery.create()
			.setInstanceFactory(instanceFactory)
			.addPackages(impl.packages)
			.addPackages(packages)
			.build();
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof TypesModule;
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

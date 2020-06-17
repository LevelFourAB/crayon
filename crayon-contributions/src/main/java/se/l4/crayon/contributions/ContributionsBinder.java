package se.l4.crayon.contributions;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import se.l4.crayon.contributions.internal.ContributionsManager;

/**
 * Class used to add support for contributions to any Guice module.
 *
 * <p>
 * Example usage:
 * <pre>
 * public class SampleModule implements Module {
 * 	public void configure(Binder binder) {
 * 		CrayonBinder.newBinder(binder, this); // bind own instance
 *
 * 		// any other bindings
 * 	}
 *
 * 	{@literal @SomeContributionAnnotation}
 * 	public void sampleContribution(ContributionReceiver r) {
 * 		r.addEntry("test");
 * 	}
 * }
 * </pre>
 */
public abstract class ContributionsBinder
{
	public static final TypeLiteral<Set<Object>> TYPE =
		new TypeLiteral<Set<Object>>() {};

	public static final Key<Set<Object>> KEY
		= Key.get(TYPE, Names.named("crayon-modules"));

	public static ContributionsBinder newBinder(Binder binder)
	{
		binder = binder.skipSources(ContributionsBinder.class, RealBinder.class);

		RealBinder b = new RealBinder(binder);
		binder.install(b);
		return b;
	}

	public static ContributionsBinder newBinder(Binder binder, Module module)
	{
		binder = binder.skipSources(ContributionsBinder.class, RealBinder.class);

		RealBinder b = new RealBinder(binder);
		b.module(module);
		binder.install(b);
		return b;
	}

	/**
	 * Bind an instance of {@link Contributions}.
	 *
	 * @param annotation
	 */
	public abstract void bindContributions(Class<? extends Annotation> annotation);

	private static class RealBinder
		extends ContributionsBinder
		implements Module, Provider<Set<Object>>
	{
		private static final AtomicInteger count = new AtomicInteger();

		private final Binder binder;
		private List<Provider<?>> providers;

		public RealBinder(Binder binder)
		{
			this.binder = binder;
		}

		@Override
		public void configure(Binder binder)
		{
			binder.bind(KEY).toProvider(this);
			binder.bind(ContributionsManager.class).asEagerSingleton();
		}

		@Inject
		public void setup(Injector injector)
		{
			providers = new LinkedList<Provider<?>>();

			for(Binding<?> b : injector.findBindingsByType(TypeLiteral.get(Object.class)))
			{
				Key<?> key = b.getKey();
				Annotation a = key.getAnnotation();
				if(a instanceof Named && ((Named) a).value().startsWith("crayon-module-"))
				{
					providers.add(b.getProvider());
				}
			}
		}

		@Override
		public Set<Object> get()
		{
			Set<Object> result = new HashSet<Object>();
			for(Provider<?> p : providers)
			{
				result.add(p.get());
			}

			return result;
		}

		public void module(Object module)
		{
			binder.bind(
				Key.get(Object.class, Names.named("crayon-module-" + count.incrementAndGet()))
			).toInstance(module);
		}

		@Override
		public void bindContributions(final Class<? extends Annotation> annotation)
		{
			// Bind via a provider and delegate to CrayonImpl
			binder.bind(Contributions.class).annotatedWith(annotation)
				.toProvider(new Provider<Contributions>()
				{
					private ContributionsManager crayon;

					@Inject
					private void setup(ContributionsManager crayon)
					{
						this.crayon = crayon;
					}

					@Override
					public Contributions get()
					{
						return crayon.createContributions(annotation);
					}
				}).in(Scopes.SINGLETON);
		}

		@Override
		public int hashCode()
		{
			return KEY.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof RealBinder;
		}
	}
}

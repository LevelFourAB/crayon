/*
 * Copyright 2009 Andreas Holstenson
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
package se.l4.crayon;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.internal.CrayonImpl;

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

/**
 * Class used to add support for contributions and other Crayon features to
 * any Guice module. When the binder has been used any methods annotated with
 * {@link Contribution} will be run after the creation of the {@link Injector}.
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
 * 	{@literal @Contribution}
 * 	public void sampleContribution(ContributionReceiver r) {
 * 		r.addEntry("test");
 * 	}
 * }
 * </pre>
 * 
 * @author Andreas Holstenson
 * @see CrayonModule
 */
public abstract class CrayonBinder
{
	public static final TypeLiteral<Set<Object>> TYPE =
		new TypeLiteral<Set<Object>>() {};
		
	public static final Key<Set<Object>> KEY
		= Key.get(TYPE, Names.named("crayon-modules"));
		
	public static CrayonBinder newBinder(Binder binder)
	{
		binder = binder.skipSources(CrayonBinder.class, RealBinder.class);

		RealBinder b = new RealBinder(binder);
		binder.install(b);
		return b;
	}
	
	public static CrayonBinder newBinder(Binder binder, Module module)
	{
		binder = binder.skipSources(CrayonBinder.class, RealBinder.class);

		RealBinder b = new RealBinder(binder);
		b.module(module);
		binder.install(b);
		return b;
	}
	
	public abstract void module(Object module);

	/**
	 * Bind an instance of {@link Contributions}.
	 * 
	 * @param annotation
	 */
	public abstract void bindContributions(Class<? extends Annotation> annotation);
	
	private static class RealBinder
		extends CrayonBinder
		implements Module, Provider<Set<Object>>
	{
		private static final AtomicInteger count = new AtomicInteger();
		
		private final Binder binder;
		private List<Provider<?>> providers;

		public RealBinder(Binder binder)
		{
			this.binder = binder;
		}
		
		public void configure(Binder binder)
		{
			binder.bind(KEY).toProvider(this);
			binder.bind(Crayon.class).to(CrayonImpl.class)
				.asEagerSingleton();
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

		public Set<Object> get()
		{
			Set<Object> result = new HashSet<Object>();
			for(Provider<?> p : providers)
			{
				result.add(p.get());
			}
			
			return result;
		}
		
		@Override
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
					private CrayonImpl crayon;

					@Inject
					private void setup(CrayonImpl crayon)
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

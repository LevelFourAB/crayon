package se.l4.crayon.osgi;

import org.osgi.framework.BundleContext;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

import se.l4.crayon.osgi.internal.ServiceImportBuilderImpl;

/**
 * OSGi services for use with {@link OSGiConfigurator}.
 * 
 * @author Andreas Holstenson
 *
 */
public final class OSGi
{
	private OSGi()
	{
	}
	
	/**
	 * Create a provider that is used to import an OSGi service from another
	 * bundle. This can be used to directly inject any OSGi service without
	 * using {@link ServiceRef}.
	 * 
	 * @param <T>
	 * 		bundle
	 * @param service
	 * 		service type
	 * @return
	 * 		builder to use for creating the provider
	 */
	public static <T> ServiceImportBuilder<T> importService(Class<T> service)
	{
		return new ServiceImportBuilderImpl<T>(service);
	}
	
	/**
	 * Get a type literal that represents a {@link ServiceRef}.
	 * 
	 * @param <T>
	 * 		type
	 * @param service
	 * 		class of service
	 * @return
	 * 		type literal matching the servjce
	 */
	@SuppressWarnings("unchecked")
	public static <T> TypeLiteral<ServiceRef<T>> ref(Class<T> service)
	{
		return (TypeLiteral<ServiceRef<T>>) TypeLiteral.get(
			Types.newParameterizedType(ServiceRef.class, service)
		);
	}
	
	/**
	 * Retrieve a provider for importing a {@link ServiceRef} for the given
	 * service.
	 * 
	 * @param <T>
	 * @param service
	 * @return
	 */
	public static <T> Provider<? extends ServiceRef<T>> importServiceRef(final Class<T> service)
	{
		return new Provider<ServiceRef<T>>()
		{
			@Inject
			private ServiceRefManager manager;
			
			public ServiceRef<T> get()
			{
				return manager.get(service);
			}
		};
	}
}

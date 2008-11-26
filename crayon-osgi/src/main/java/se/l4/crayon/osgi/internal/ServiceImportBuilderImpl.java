package se.l4.crayon.osgi.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import se.l4.crayon.osgi.ServiceImportBuilder;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ServiceImportBuilderImpl<T>
	implements ServiceImportBuilder<T>
{
	private Class<T> type;
	
	public ServiceImportBuilderImpl(Class<T> type) 
	{
		this.type = type;
	}
	
	public Provider<T> single()
	{
		return new SingleServiceProvider<T>(type);
	}

	private static final class SingleServiceProvider<T>
		implements Provider<T>
	{
		@Inject
		private BundleContext context;
		
		private Class<T> type;
		
		public SingleServiceProvider(Class<T> type)
		{
			this.type = type;
		}
	
		@SuppressWarnings("unchecked")
		public T get()
		{
			ServiceReference ref = context.getServiceReference(type.getName());
			
			return (T) context.getService(ref);
		}
		
		@Override
		public String toString()
		{
			return "ExternalServiceProvider[single]";
		}
	}
}

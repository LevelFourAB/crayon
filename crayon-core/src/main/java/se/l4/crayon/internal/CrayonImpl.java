package se.l4.crayon.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import se.l4.crayon.ConfigurationException;
import se.l4.crayon.Crayon;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Shutdown;
import se.l4.crayon.internal.methods.MethodDef;
import se.l4.crayon.internal.methods.MethodResolver;
import se.l4.crayon.internal.methods.MethodResolverCallback;

@Singleton
public class CrayonImpl
	implements Crayon
{
	private static final Logger logger = LoggerFactory.getLogger(CrayonImpl.class);
	
	private final Set<Object> modules;
	private final Injector injector;

	@Inject
	public CrayonImpl(Injector injector, @Named("crayon-modules") Set<Object> modules)
	{
		this.injector = injector;
		this.modules = modules;
	}
	
	public void start()
	{
		performContributions();
	}
	
	/**
	 * Run all contributions on all modules.
	 */
	private void performContributions()
	{
		logger.debug("Performing contributions");
		
		MethodResolver resolver = new MethodResolver(Contribution.class,
				new MethodResolverCallback()
				{
					public String getName(MethodDef def)
					{
						String s = 
							def.getMethod()
								.getAnnotation(Contribution.class)
								.name();
						
						return "".equals(s) 
							? def.getObject().getClass() + "-" + def.getMethod().getName()
							: s;
					}
				}
			);
		
		callMethods(resolver);
	}
	
	public void shutdown()
	{
		logger.info("Shutting down");
		
		MethodResolver resolver = new MethodResolver(Shutdown.class,
			new MethodResolverCallback()
			{
				public String getName(MethodDef def)
				{
					String s = 
						def.getMethod()
							.getAnnotation(Shutdown.class)
							.name();
					
					return "".equals(s) 
						? def.getObject().getClass() + "-" + def.getMethod().getName()
						: s;
				}
			}
		);
		
		callMethods(resolver);
	}
	
	private void callMethods(MethodResolver resolver)
	{
		for(Object c : modules)
		{
			resolver.add(c);
		}
		
		final Set<MethodDef> defs = resolver.getOrder();
		resolver = null;
		
		// run all contributions in order
		for(MethodDef def : defs)
		{
			Method method = def.getMethod();
			Object object = def.getObject();
			
			Type[] var = method.getGenericParameterTypes();
				
			Annotation[][] annotations = method.getParameterAnnotations();
				
			Object[] params = new Object[var.length];
				
			for(int i=0, n=var.length; i<n; i++)
			{
				// Use the correct method
				Key<?> key = annotations[i].length == 0
					? Key.get(var[i])
					: Key.get(var[i], annotations[i][0]);
					
				params[i] = injector.getInstance(key);
			}
				
			try
			{
				method.setAccessible(true);
				method.invoke(object, params);
			}
			catch(IllegalArgumentException e)
			{
				throw new ConfigurationException(e.getMessage(), e);
			}
			catch(IllegalAccessException e)
			{
				throw new ConfigurationException(e.getMessage(), e);
			}
			catch(InvocationTargetException e)
			{
				Throwable cause = e.getCause();
				
				throw new ConfigurationException(cause.getMessage(), cause);
			}
		}
	}
}

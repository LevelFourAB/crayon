package se.l4.crayon.contributions.internal.methods;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.l4.crayon.contributions.ContributionException;
import se.l4.crayon.contributions.DependencyResolver;

/**
 * Class for discovering methods annotated with a special annotation and the
 * order they should be invoked in.
 */
public class MethodResolver
{
	private HashSet<Class<?>> classes;

	private Map<String, MethodDef> methods;
	private Map<Class<?>, List<MethodDef>> defs;

	private Class<? extends Annotation>[] annotations;
	private MethodResolverCallback callback;

	public MethodResolver(
		MethodResolverCallback callback,
		Class<? extends Annotation>... annotations)
	{
		this.annotations = annotations;
		this.callback = callback;

		methods = new HashMap<String, MethodDef>();
		defs = new HashMap<Class<?>, List<MethodDef>>();

		classes = new HashSet<Class<?>>();
	}

	public void add(Object instance)
	{
		Class<?> type = instance.getClass();
		// Do nothing if the class is already added
		if(false == classes.add(type))
		{
			return;
		}

		// Otherwise create an instance and loop through methods
		Method[] declared = type.getMethods();
		List<MethodDef> defs = getMethodDefs(type);

		_outer:
		for(Method m : declared)
		{
			for(Class<? extends Annotation> a : annotations)
			{
				if(false == m.isAnnotationPresent(a))
				{
					continue _outer;
				}
			}

			MethodDef def = new MethodDef(instance, m);
			String name = callback.getName(def);

			// Store the definition
			methods.put(name, def);
			defs.add(def);
		}
	}

	private List<MethodDef> getMethodDefs(Class<?> c)
	{
		List<MethodDef> result = defs.get(c);

		if(result == null)
		{
			result = new LinkedList<MethodDef>();
			defs.put(c, result);
		}

		return result;
	}

	public Set<MethodDef> getOrder()
	{
		DependencyResolver<MethodDef> resolver =
			new DependencyResolver<MethodDef>();

		for(Map.Entry<String, MethodDef> e : methods.entrySet())
		{
			MethodDef def = e.getValue();

			// Always add without any dependencies
			resolver.add(def);

			// Take care of order dependencies
			for(String s : def.getOrder())
			{
				handleOrderEntry(resolver, def, s);
			}
		}

		return resolver.getOrder();
	}

	private void handleOrderEntry(DependencyResolver<MethodDef> resolver, MethodDef def, String s)
	{
		if(s.startsWith("before:"))
		{
			s = s.substring(7);

			MethodDef d = methods.get(s);
			if(d != null)
			{
				resolver.addDependency(d, def);
			}
		}
		else if(s.startsWith("after:"))
		{
			s = s.substring(6);

			MethodDef d = methods.get(s);
			if(d != null)
			{
				resolver.addDependency(def, d);
			}
		}
		else if(s.equals("last"))
		{
			for(MethodDef d : methods.values())
			{
				boolean ok = true;
				for(String sd : d.getOrder())
				{
					if(sd.equals("last"))
					{
						ok = false;
						break;
					}
				}

				if(ok)
				{
					resolver.addDependency(def, d);
				}
			}
		}
		else if(s.equals("first"))
		{
			for(MethodDef d : methods.values())
			{
				boolean ok = true;
				for(String sd : d.getOrder())
				{
					if(sd.equals("first"))
					{
						ok = false;
						break;
					}
				}

				if(ok)
				{
					resolver.addDependency(d, def);
				}
			}
		}
		else
		{
			throw new ContributionException("Invalid order `" + s
				+ "` in " + def.getMethod().getName() + " ("
				+ def.getMethod().getDeclaringClass() + ")");
		}
	}

}

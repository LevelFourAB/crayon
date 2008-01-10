package se.l4.crayon.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Singleton;

/**
 * Implementation of {@link TypeConverter}, supports chaining of conversions
 * to reach the desired output type.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class TypeConverterImpl
	implements TypeConverter
{
	private final Conversion<Object, Object> NULL =
		new Conversion<Object, Object>()
		{
			public Object convert(Object in)
			{
				return null;
			}

			public Class<Object> getInput()
			{
				return null;
			}

			public Class<Object> getOutput()
			{
				return null;
			}
		};
		
	private Map<Class<?>, List<Conversion<?, ?>>> conversions;
	
	public TypeConverterImpl()
	{
		conversions = new HashMap<Class<?>, List<Conversion<?,?>>>();
	}
	
	private List<Conversion<?, ?>> getListFor(Class<?> c)
	{
		synchronized(conversions)
		{
			List<Conversion<?, ?>> list = conversions.get(c);
			if(list == null)
			{
				list = new LinkedList<Conversion<?,?>>();
				conversions.put(c, list);
			}
			
			return list;
		}
	}
	
	public void add(Conversion<?, ?> conversion)
	{
		List<Conversion<?, ?>> list = getListFor(conversion.getInput());
		list.add(conversion);
	}

	@SuppressWarnings("unchecked")
	public <T> T convert(Object in, Class<T> output)
	{
		Class<?> type = in == null ? void.class : in.getClass();
		
		// Check if it is assignable
		if(output.isAssignableFrom(type))
		{
			return (T) in;
		}
		
		Conversion tc = findConversion(type, output);
		
		return (T) tc.convert(in);
	}

	@SuppressWarnings("unchecked")
	private <I, O> Conversion<I, O> findConversion(Class<I> in, Class<O> out)
	{
		Set<Conversion<I, O>> tested = new HashSet<Conversion<I, O>>();
		LinkedList<Conversion<I, O>> queue = new LinkedList<Conversion<I, O>>();
		
		// Add initial conversions that should be checked
		for(Class<?> c : getInheritance(in))
		{
			List<Conversion<?, ?>> list = getListFor(c);
			
			queue.addAll((Collection<? extends Conversion<I, O>>) list);
			tested.addAll((Collection<? extends Conversion<I, O>>) list);
		}
		
		while(false == queue.isEmpty())
		{
			Conversion tc = queue.removeFirst();
		
			// check if this is ok
			if(out.isAssignableFrom(tc.getOutput()))
			{
				return tc;
			}
			
			// otherwise continue
			for(Class<?> c : getInheritance(tc.getOutput()))
			{
				List<Conversion<?, ?>> list = getListFor(c);
				
				for(Conversion<?, ?> possible : list)
				{
					CompoundTypeConversion ctc = new CompoundTypeConversion(
						tc, (Conversion<Object, Object>) possible
					);
					
					queue.add((Conversion<I, O>) ctc);
					tested.add((Conversion<I, O>) ctc);
				};
			}
		}
		
		return (Conversion<I, O>) NULL;
	}
	
	private static Set<Class<?>> getInheritance(Class<?> in)
	{
		LinkedHashSet<Class<?>> result = new LinkedHashSet<Class<?>>();
		
		result.add(in);
		getInheritance(in, result);
		
		return result;
	}
	
	private static void getInheritance(Class<?> in, Set<Class<?>> result)
	{
		Class<?> superclass = getSuperclass(in);
		
		if(superclass != null)
		{
			result.add(superclass);
			getInheritance(superclass, result);
		}
		
		getInterfaceInheritance(in, result);
	}
	
	private static void getInterfaceInheritance(Class<?> in, Set<Class<?>> result)
	{
		for(Class<?> c : in.getInterfaces())
		{
			result.add(c);
			
			getInterfaceInheritance(c, result);
		}
	}
	
	private static Class<?> getSuperclass(Class<?> in)
	{
		if(in == null)
		{
			return null;
		}
		
		if(in.isArray() && in != Object[].class)
		{
			Class<?> type = in.getComponentType();
			
			while(type.isArray())
			{
				type = type.getComponentType();
			}
			
			return type;
		}
		
		return in.getSuperclass();
	}
	
}

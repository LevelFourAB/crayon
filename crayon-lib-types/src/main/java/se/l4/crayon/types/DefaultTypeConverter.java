package se.l4.crayon.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Implementation of {@link TypeConverter}, supports chaining of conversions
 * to reach the desired output type.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultTypeConverter
	implements TypeConverter
{
	private Map<Class<?>, List<Conversion<?, ?>>> conversions;
	private Injector injector;
	
	@Inject
	public DefaultTypeConverter(Injector injector)
	{
		this.injector = injector;
		
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
	
	public void add(Class<? extends Conversion<?, ?>> conversion)
	{
		Conversion<?, ?> c = injector.getInstance(conversion);
		add(c);
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

	/**
	 * Find the conversion to use for converting {@code in} to {@code out}.
	 * 
	 * @param <I>
	 * 		in type
	 * @param <O>
	 * 		out type
	 * @param in
	 * 		input class
	 * @param out
	 * 		output class
	 * @return
	 * 		conversion to use, {@code null} if none was found
	 */
	@SuppressWarnings("unchecked")
	private <I, O> Conversion<I, O> findConversion(Class<I> in, Class<O> out)
	{
		in = (Class) wrap(in);
		out = (Class) wrap(out);
		
		Set<Conversion<I, O>> tested = new HashSet<Conversion<I, O>>();
		LinkedList<Conversion<I, O>> queue = new LinkedList<Conversion<I, O>>();
		
		// Add initial conversions that should be checked
		for(Class<?> c : getInheritance(in))
		{
			List<Conversion<?, ?>> list = getListFor(c);
			
			queue.addAll((Collection) list);
			tested.addAll((Collection) list);
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
		
		return null;
	}
	
	private static Set<Class<?>> getInheritance(Class<?> in)
	{
		LinkedHashSet<Class<?>> result = new LinkedHashSet<Class<?>>();
		
		result.add(in);
		getInheritance(in, result);
		
		return result;
	}
	
	/**
	 * Get inheritance of type.
	 * 
	 * @param in
	 * @param result
	 */
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
	
	/**
	 * Get interfaces that the type inherits from.
	 * 
	 * @param in
	 * @param result
	 */
	private static void getInterfaceInheritance(Class<?> in, Set<Class<?>> result)
	{
		for(Class<?> c : in.getInterfaces())
		{
			result.add(c);
			
			getInterfaceInheritance(c, result);
		}
	}
	
	/**
	 * Get superclass of class.
	 * 
	 * @param in
	 * @return
	 */
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
	
	/**
	 * Wrap the given primitive in its object equivalent.
	 * 
	 * @param in
	 * @return
	 */
	private static Class<?> wrap(Class<?> in)
	{
		if(false == in.isPrimitive())
		{
			return in;
		}
		else if(in == boolean.class)
		{
			return Boolean.TYPE;
		}
		else if(in == int.class)
		{
			return Integer.TYPE;
		}
		else if(in == float.class)
		{
			return Float.TYPE;
		}
		else if(in == double.class)
		{
			return Double.TYPE;
		}
		else if(in == byte.class)
		{
			return Byte.TYPE;
		}
		else if(in == short.class)
		{
			return Short.TYPE;
		}
		else if(in == long.class)
		{
			return Long.TYPE;
		}
		else
		{
			throw new ConversionException("Unsupported type " + in);
		}
	}
}

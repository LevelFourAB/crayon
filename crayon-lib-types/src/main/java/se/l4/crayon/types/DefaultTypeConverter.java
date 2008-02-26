/*
 * Copyright 2008 Andreas Holstenson
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
	
	private static Map<Class<?>, Class<?>> primitives;
	
	static
	{
		primitives = new HashMap<Class<?>, Class<?>>();
		primitives.put(boolean.class, Boolean.class);
		primitives.put(byte.class, Byte.class);
		primitives.put(short.class, Short.class);
		primitives.put(int.class, Integer.class);
		primitives.put(long.class, Long.class);
		primitives.put(float.class, Float.class);
		primitives.put(double.class, Double.class);
		primitives.put(void.class, Void.class);
	}
	
	@Inject
	public DefaultTypeConverter()
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
	
	public boolean canConvertBetween(Class<?> in, Class<?> out)
	{
		return findConversion(in, out) != null;
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
		
		Class<?> c = primitives.get(in);
		if(c != null)
		{
			return c;
		}
		else
		{
			throw new ConversionException("Unsupported type " + in);
		}
	}
}

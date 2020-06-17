package se.l4.crayon.contributions.internal.methods;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.l4.crayon.contributions.After;
import se.l4.crayon.contributions.Before;
import se.l4.crayon.contributions.Order;

public class MethodDef
{
	private final Object object;
	private final Method method;

	public MethodDef(Object object, Method method)
	{
		this.object = object;
		this.method = method;
	}

	public Method getMethod()
	{
		return method;
	}

	public Object getObject()
	{
		return object;
	}

	public List<String> getOrder()
	{
		List<String> order = new ArrayList<String>();
		handle(method.getAnnotations(), order, new HashSet<Class<? extends Annotation>>());
		return order;
	}

	private void handle(Annotation[] annotations, List<String> order, Set<Class<? extends Annotation>> visited)
	{
		for(Annotation a : annotations)
		{
			if(a instanceof Order)
			{
				order.addAll(Arrays.asList(((Order) a).value()));
			}
			else if(a instanceof After)
			{
				for(String s : ((After) a).value())
				{
					order.add("after:" + s);
				}
			}
			else if(a instanceof Before)
			{
				for(String s : ((Before) a).value())
				{
					order.add("before:" + s);
				}
			}
			else
			{
				if(visited.add(a.annotationType()))
				{
					handle(a.annotationType().getAnnotations(), order, visited);
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return method.getName() + " (" + getObject().getClass() + ")";
	}
}

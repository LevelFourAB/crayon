package se.l4.crayon.internal.methods;

import java.lang.reflect.Method;

import se.l4.crayon.annotation.ModuleDescription;
import se.l4.crayon.annotation.Order;

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
	
	public String[] getOrder()
	{
		Order order = method.getAnnotation(Order.class);
		
		return order == null ? new String[0] : order.value();
	}
	
	public Class<?>[] getDependencies()
	{
		ModuleDescription module = method.getAnnotation(ModuleDescription.class);
		
		return module == null ? new Class[0] : module.dependencies();
	}
	
	@Override
	public String toString()
	{
		return method.getName() + " (" + getObject().getClass() + ")";
	}
}

package se.l4.crayon.osgi.remoting.internal.proxy;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Type;

import se.l4.crayon.osgi.remoting.internal.def.MethodDef;

/**
 * Class that helps analyze the services that are to be exported, will create
 * a list of all imports that are required to actually utilize it in an OSGi
 * bundle.
 * 
 * @author Andreas Holstenson
 *
 */
public class Analyzer 
{
	private Set<String> requiredImports;
	private Set<String> analyzed;
	private List<MethodDef> methods;
	
	public Analyzer()
	{
		requiredImports = new HashSet<String>();
		analyzed = new HashSet<String>();
		methods = new LinkedList<MethodDef>();
	}
	
	/**
	 * Start analyzing the given service interface.
	 * 
	 * @param type
	 */
	public void analyzeService(Class<?> service)
	{
		analyzeClass(service, true);
	}
	
	private void analyzeClass(Class<?> type, boolean service)
	{
		if(type == null || type.getPackage() == null || false == analyzed.add(type.getName()))
		{
			return;
		}
		
		String pkg = type.getPackage().getName();
		requiredImports.add(pkg);
		
		for(Method m : type.getMethods())
		{
			for(Class<?> param : m.getParameterTypes())
			{
				analyzeClass(param, false);
			}
			
			analyzeClass(m.getReturnType(), false);
			
			if(service)
			{
				methods.add(new MethodDef(m.getName(), Type.getMethodDescriptor(m)));
			}
		}
	}
	
	public Set<String> getRequiredImports()
	{
		return requiredImports;
	}
	
	public List<MethodDef> getMethods()
	{
		return methods;
	}
}

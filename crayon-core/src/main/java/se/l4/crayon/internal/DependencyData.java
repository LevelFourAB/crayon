package se.l4.crayon.internal;

import java.util.HashSet;
import java.util.Set;

/**
 * Class holding information about a module and its dependencies. Used by the
 * entry point to decide in which order modules should be configured and in
 * which order contributions are to be made.
 * 
 * @author Andreas Holstenson
 *
 */
public class DependencyData
{
	private Set<DependencyData> dependencies;
	
	private Object data;
	
	public DependencyData(Object module)
	{
		this.data = module;
		
		dependencies = new HashSet<DependencyData>();
	}
	
	public void addDependency(DependencyData dep)
	{
		dependencies.add(dep);
	}
	
	public Set<DependencyData> getDependencies()
	{
		return dependencies;
	}
	
	public Object getModule()
	{
		return data;
	}
}

package se.l4.crayon.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Dependency resolver, takes a set of defined dependencies and decides in
 * which order they should be used.
 * 
 * @author Andreas Holstenson
 *
 */
public class DependencyResolver
{
	private Map<Object, Node> nodes;
	
	public DependencyResolver()
	{
		nodes = new HashMap<Object, Node>();
	}
	
	/**
	 * Retrieve a set which contains all dependencies in their logical order.
	 * 
	 * @param topLevelDeps
	 * 		dependencies to start with
	 * @return
	 * 		ordered set with dependencies
	 */
	public Set<Object> getDependencyOrder(Set<DependencyData> topLevelDeps)
	{
		for(DependencyData dep : topLevelDeps)
		{
			buildGraph(dep);
		}
		
		Set<Object> result = new LinkedHashSet<Object>();
		
		for(Node n : nodes.values())
		{
			if(n.to.isEmpty())
			{
				add(n, result);
			}
		}
		
		return result;
	}
	
	private void add(Node node, Set<Object> result)
	{
		if(result.contains(node.type))
		{
			return;
		}
		
		for(Node n : node.to)
		{
			add(n, result);
		}
		
		result.add(node.type);
		
		for(Node n : node.from)
		{
			add(n, result);
		}
	}
	
	private void buildGraph(DependencyData in)
	{
		Node node = getNode(in);
		
		for(DependencyData d : in.getDependencies())
		{
			Node toDep = getNode(d);
			
			node.to.add(toDep);
			toDep.from.add(node);
			
			buildGraph(d);
		}
	}
	
	private Node getNode(DependencyData in)
	{
		Object m = in.getModule();
		Node node = nodes.get(m);
		if(node == null)
		{
			node = new Node(m);
			nodes.put(m, node);
		}
		
		return node;
	}
	
	private static class Node
	{
		Set<Node> to;
		Set<Node> from;
		Object type;
		
		public Node(Object type)
		{
			this.type = type;
			
			to = new HashSet<Node>();
			from = new HashSet<Node>();
		}
	}
	
	public static void main(String[] args)
	{
		String A = "A";
		String B = "B";
		String C = "C";
		String D = "D";
		
		DependencyResolver resolver = new DependencyResolver();
		
		
		DependencyData dA = new DependencyData(A);
		DependencyData dB = new DependencyData(B);
		DependencyData dC = new DependencyData(C);
		DependencyData dD = new DependencyData(D);
		
		dA.addDependency(dB);
		dC.addDependency(dD);
		dB.addDependency(dD);
		dC.addDependency(dA);
		
		Set<DependencyData> data = new HashSet<DependencyData>();
		data.add(dA);
		data.add(dC);
		
		resolver.getDependencyOrder(data);
	}
}

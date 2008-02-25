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
package se.l4.crayon.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import se.l4.crayon.ConfigurationException;

/**
 * Dependency resolver, takes a set of defined dependencies and decides in
 * which order they should be used.
 * 
 * <pre>
 * DependencyResolver&lt;String&gt; resolver = 
 * 		new DependencyResolver&lt;String&gt;();
 * 
 * resolver.addDependency(A, B);
 * resolver.addDependency(B, C);
 * 
 * resolver.getOrder(); // retrieve ordered set
 * </pre>
 * 
 * @author Andreas Holstenson
 *
 */
public class DependencyResolver<T>
{
	private Map<T, Node> nodes;
	
	public DependencyResolver()
	{
		nodes = new HashMap<T, Node>();
	}
	
	/**
	 * Add a dependency from an object to another object.
	 * 
	 * @param from
	 * 		object to add from
	 * @param on
	 * 		the object that {@code from} depends on
	 */
	public void addDependency(T from, T on)
	{
		Node fromDep = getNode(from);
		Node toDep = getNode(on);
		
		if(toDep.to.contains(fromDep))
		{
			throw new ConfigurationException("Cyclic dependency between "
				+ from + " and " + on);
		}
		
		fromDep.to.add(toDep);
		toDep.from.add(fromDep);
	}
	
	public void add(T object)
	{
		getNode(object);
	}
	
	/**
	 * Retrieve a set which contains all dependencies in their logical order.
	 * 
	 * @param topLevelDeps
	 * 		dependencies to start with
	 * @return
	 * 		ordered set with dependencies
	 */
	public Set<T> getOrder()
	{
		Set<T> result = new LinkedHashSet<T>();
		
		for(Node n : nodes.values())
		{
			if(n.to.isEmpty())
			{
				add(n, result);
			}
		}
		
		return result;
	}
	
	/** Add node to final result. */
	private void add(Node node, Set<T> result)
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

	/** Retrieve node for object. */
	private Node getNode(T m)
	{
		Node node = nodes.get(m);
		
		if(node == null)
		{
			node = new Node(m);
			nodes.put(m, node);
		}
		
		return node;
	}
	
	private class Node
	{
		Set<Node> to;
		Set<Node> from;
		T type;
		
		public Node(T type)
		{
			this.type = type;
			
			to = new HashSet<Node>();
			from = new HashSet<Node>();
		}
	}

}

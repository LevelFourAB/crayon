package se.l4.crayon.osgi.tooling.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;

public class AttributesMap
	implements Map<String, String>
{
	private final Attributes attrs;
	
	public AttributesMap(Attributes attrs)
	{
		this.attrs = attrs;
	}

	public void clear()
	{
		attrs.clear();
	}

	public boolean containsKey(Object name)
	{
		return attrs.containsKey(new Attributes.Name((String) name));
	}

	public boolean containsValue(Object value)
	{
		return attrs.containsValue(value);
	}

	public Set<java.util.Map.Entry<String, String>> entrySet()
	{
		throw new IllegalArgumentException("Unsupported operation");
	}

	public String get(Object name)
	{
		return (String) attrs.get(new Attributes.Name((String) name));
	}

	public boolean isEmpty()
	{
		return attrs.isEmpty();
	}

	public Set<String> keySet()
	{
		throw new IllegalArgumentException("Unsupported operation");
	}

	public String put(String name, String value)
	{
		return (String) attrs.put(new Attributes.Name((String) name), value);
	}

	public String remove(Object name)
	{
		return (String) attrs.remove(new Attributes.Name((String) name));
	}

	public int size()
	{
		return attrs.size();
	}

	public String toString()
	{
		return attrs.toString();
	}

	public Collection<String> values()
	{
		return (Collection) attrs.values();
	}

	public void putAll(Map<? extends String, ? extends String> t)
	{
		throw new IllegalArgumentException("Unsupported operation");
	}
	
	
}

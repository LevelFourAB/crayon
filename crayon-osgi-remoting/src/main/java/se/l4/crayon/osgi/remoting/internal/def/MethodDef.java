package se.l4.crayon.osgi.remoting.internal.def;

import java.io.Serializable;

/**
 * Definition of a method that should be used in a proxy.
 * 
 * @author Andreas Holstenson
 *
 */
public class MethodDef
	implements Serializable
{
	private final String name;
	private final String signature;
	
	public MethodDef(String name, String signature)
	{
		this.name = name;
		this.signature = signature;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSignature()
	{
		return signature;
	}
	
	@Override
	public String toString()
	{
		return "MethodDef[" + name + ", " + signature + "]";
	}
}

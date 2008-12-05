package se.l4.crayon.osgi.remoting.internal.net;

import java.io.Serializable;
import java.util.Dictionary;

import se.l4.crayon.osgi.remoting.internal.def.ProxyDef;

public class RegisterService
	implements Serializable
{
	private final ProxyDef def;
	private final String id;
	private final Dictionary<String, Object> properties;
	
	public RegisterService(ProxyDef def, String id, Dictionary<String, Object> properties)
	{
		this.def = def;
		this.id = id;
		this.properties = properties;
	}
	
	public ProxyDef getDef()
	{
		return def;
	}
	
	public String getId()
	{
		return id;
	}
	
	public Dictionary<String, Object> getProperties()
	{
		return properties;
	}
}

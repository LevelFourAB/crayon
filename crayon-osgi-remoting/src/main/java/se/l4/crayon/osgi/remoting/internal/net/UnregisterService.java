package se.l4.crayon.osgi.remoting.internal.net;

import java.io.Serializable;

public class UnregisterService
	implements Serializable
{
	private final String id;
	
	public UnregisterService(String id)
	{
		this.id = id;
	}
	
	public String getId()
	{
		return id;
	}
}

package se.l4.crayon.osgi.remoting.internal.net;

import java.io.Serializable;

public class InvokeMethod
	implements Serializable
{
	private final int xid;
	private final String serviceId;
	private final String name;
	private final String signature;
	private final Object[] args;
	
	public InvokeMethod(int xid, String serviceId, String name, String signature, Object[] args)
	{
		this.xid = xid;
		this.serviceId = serviceId;
		this.name = name;
		this.signature = signature;
		this.args = args;
	}
	
	public int getXid()
	{
		return xid;
	}
	
	public String getServiceId()
	{
		return serviceId;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSignature()
	{
		return signature;
	}
	
	public Object[] getArgs()
	{
		return args;
	}
}

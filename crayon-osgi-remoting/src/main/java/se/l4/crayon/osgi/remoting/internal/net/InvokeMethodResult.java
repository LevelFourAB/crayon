package se.l4.crayon.osgi.remoting.internal.net;

import java.io.Serializable;

public class InvokeMethodResult
	implements Serializable
{
	private final int xid;
	private final Object result;
	private final boolean exception;
	
	public InvokeMethodResult(int xid, Object result, boolean exception)
	{
		this.xid = xid;
		this.result = result;
		this.exception = exception;
	}
	
	public int getXid()
	{
		return xid;
	}
	
	public Object getResult()
	{
		return result;
	}
	
	public boolean isException()
	{
		return exception;
	}
}

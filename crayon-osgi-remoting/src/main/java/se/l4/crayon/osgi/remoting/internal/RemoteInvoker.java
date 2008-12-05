package se.l4.crayon.osgi.remoting.internal;

import se.l4.crayon.osgi.remoting.MethodInvoker;

/**
 * Invoker that uses the {@link NetworkHandler} to perform remote
 * invocations.
 * 
 * @author Andreas Holstenson
 *
 */
public class RemoteInvoker
	implements MethodInvoker
{
	private final NetworkHandler handler;
	private final String id;

	public RemoteInvoker(NetworkHandler handler, String id)
	{
		this.handler = handler;
		this.id = id;
	}
	
	public Object invoke(String name, String signature, Object[] args)
		throws Throwable
	{
		return handler.invoke(id, name, signature, args);
	}
}

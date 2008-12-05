package se.l4.crayon.osgi.remoting.internal;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.objectweb.asm.Type;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;

import se.l4.crayon.osgi.remoting.OSGiRemoteManager;
import se.l4.crayon.osgi.remoting.RemoteServiceException;
import se.l4.crayon.osgi.remoting.internal.OSGiRemoteManagerImpl.ExportedService;
import se.l4.crayon.osgi.remoting.internal.def.ProxyDef;
import se.l4.crayon.osgi.remoting.internal.net.InvokeMethod;
import se.l4.crayon.osgi.remoting.internal.net.InvokeMethodResult;
import se.l4.crayon.osgi.remoting.internal.net.ListServices;
import se.l4.crayon.osgi.remoting.internal.net.NetworkCallback;
import se.l4.crayon.osgi.remoting.internal.net.NetworkChannel;
import se.l4.crayon.osgi.remoting.internal.net.RegisterService;
import se.l4.crayon.osgi.remoting.internal.net.ServerNetworkChannel;
import se.l4.crayon.osgi.remoting.internal.net.UnregisterService;
import se.l4.crayon.osgi.remoting.internal.proxy.BundleGenerator;
import se.l4.crayon.osgi.remoting.internal.proxy.ProxyGenerator;
import se.l4.crayon.osgi.remoting.internal.proxy.BundleGenerator.BundleDef;

public class NetworkHandler
	implements NetworkCallback
{
	private final OSGiRemoteManagerImpl manager;
	private final BundleContext ctx;
	private final NetworkChannel channel;
	
	private final Map<Integer, Tuple> invocations;
	private final Executor executor;
	
	private final Map<String, ServiceRegistration> registrations;
	
	private boolean closing;
	private int xid;
	
	public NetworkHandler(OSGiRemoteManagerImpl manager, NetworkChannel channel, Executor executor)
	{
		this.manager = manager;
		this.ctx = manager.getContext();
		
		this.channel = channel;
		this.executor = executor;
		
		invocations = new HashMap<Integer, Tuple>();
		registrations = new HashMap<String, ServiceRegistration>();
		
		channel.setCallback(this);
	}
	
	public Object invoke(String id, String name, String signature, Object[] args)
		throws Throwable
	{
		if(false == channel.isAlive())
		{
			throw new RemoteServiceException("Connection closed");
		}
		
		Tuple t;
		int ownXid;
		
		synchronized(invocations)
		{
			
			while(invocations.containsKey(xid))
			{
				if(xid == Integer.MAX_VALUE)
				{
					xid = 0;
				}
				
				xid++;
			}
			
			ownXid = xid;
			
			t = new Tuple();
			invocations.put(ownXid, t);
		}
		
		// Wait for a result
		synchronized(t)
		{
			channel.send(new InvokeMethod(ownXid, id, name, signature, args));
			
			while(t.result == null)
			{
				t.wait();
			}
		}
		
		synchronized(invocations)
		{
			invocations.remove(ownXid);
		}
		
		// Unpack the value
		InvokeMethodResult result = t.result;
		if(result.isException())
		{
			throw (Throwable) result.getResult();
		}
		else
		{
			return result.getResult();
		}
	}
	
	public void messageReceived(final Object object)
		throws IOException 
	{
		executor.execute(new Runnable()
		{
			public void run()
			{
				if(object instanceof ListServices)
				{
					handleListServices((ListServices) object);
				}
				else if(object instanceof InvokeMethod)
				{
					handleInvokeMethod((InvokeMethod) object);
				}
				else if(object instanceof InvokeMethodResult)
				{
					handleInvokeMethodResult((InvokeMethodResult) object);
				}
				else if(object instanceof RegisterService)
				{
					handleRegisterService((RegisterService) object);
				}
				else if(object instanceof UnregisterService)
				{
					handleUnregisterService((UnregisterService) object);
				}
				
			}
		});
	}
	
	private void handleListServices(ListServices services)
	{
		try 
		{
			manager.handleListServices(channel, services);
		}
		catch(IOException e)
		{
		}
	}
	
	private void handleInvokeMethod(InvokeMethod method)
	{
		String id = method.getServiceId();
		ExportedService service = manager.getExportedService(id);
		
		Object localService = service.getService();
		
		int xid = method.getXid();
		String name = method.getName();
		String signature = method.getSignature();
		Object[] args = method.getArgs();
		
		for(Method m : localService.getClass().getMethods())
		{
			if(name.equals(m.getName()) && signature.equals(Type.getMethodDescriptor(m)))
			{
				try 
				{
					Object o = m.invoke(localService, args);
					
					try
					{
						channel.send(new InvokeMethodResult(xid, o, false));
					} 
					catch(IOException e1)
					{
					}
				}
				catch(InvocationTargetException e)
				{
					try
					{
						channel.send(new InvokeMethodResult(xid, e.getCause(), true));
					} 
					catch(IOException e1)
					{
					}
				}
				catch(Throwable t)
				{
					try
					{
						channel.send(new InvokeMethodResult(xid, t, true));
					} 
					catch(IOException e1)
					{
					}
				}
			}
		}
	}
	
	private void handleInvokeMethodResult(InvokeMethodResult result)
	{
		synchronized(invocations)
		{
			Tuple t = invocations.get(result.getXid());
			
			synchronized(t)
			{
				t.result = result;
				t.notifyAll();
			}
		}
	}
	
	private void handleRegisterService(RegisterService register)
	{
		ProxyDef def = register.getDef();
		
		BundleGenerator generator = new BundleGenerator();
		try
		{
			BundleDef bundleDef = generator.generateBundle(def);
			
			String bundleName = bundleDef.getSymbolicName();
			boolean updated = false;
			Bundle bundle = null;
			
			for(Bundle b : ctx.getBundles())
			{
				if(bundleName.equals(b.getSymbolicName()))
				{
					updated = true;
					
					b.update(bundleDef.getStream());
					bundle = b;
				}
			}
			
			if(false == updated)
			{
				bundle = ctx.installBundle("reference:unknown", bundleDef.getStream());
			}
			
			Class<?> c = bundle.loadClass(bundle.getSymbolicName() + ".ServiceProxy");
			
			// We can now create an instance and set the invoker
			Object o = c.newInstance();
			
			Field f = c.getDeclaredField(ProxyGenerator.INVOKERS_NAME);
			f.setAccessible(true);
			f.set(o, new RemoteInvoker(NetworkHandler.this, register.getId()));
			
			// Export the service
			String[] interfaces = def.getClassNames()
				.toArray(new String[0]);
			
			Dictionary<String, Object> table = register.getProperties();
			table.put(OSGiRemoteManager.REMOTE_SERVICE, Boolean.TRUE);
			table.remove(OSGiRemoteManager.REMOTE_PUBLISH);
			
			ServiceRegistration reg = ctx.registerService(interfaces, o, table);
			
			synchronized(registrations)
			{
				registrations.put(register.getId(), reg);
			}
		}
		catch(IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(BundleException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private void handleUnregisterService(UnregisterService unregister)
	{
		String id = unregister.getId();
		
		ServiceRegistration reg;
		synchronized(registrations)
		{
			reg = registrations.remove(id);
		}
		
		if(reg != null)
		{
			reg.unregister();
		}
	}
	
	public void close()
	{
		synchronized(invocations)
		{
			closing = true;
			
			channel.close();
		}
	}
	
	public void closed()
	{
		// Try to reconnect if we lose the connection
		synchronized(invocations)
		{
			for(Map.Entry<Integer, Tuple> e : invocations.entrySet())
			{
				Integer i = e.getKey();
				Tuple t = e.getValue();
				
				synchronized(t)
				{
					RemoteServiceException ex = new RemoteServiceException("Connection closed");
					t.result = new InvokeMethodResult(i, ex, true);
					t.notifyAll();
				}
			}
			
			if(closing)
			{
				return;
			}
		}
		
		if(channel instanceof ServerNetworkChannel)
		{
			// Don't try to reconnect for server-side connections
			return;
		}
		
		try
		{
			Thread.sleep(500);
			
			channel.start();
		}
		catch(IOException e)
		{
		}
		catch(InterruptedException e)
		{
		}
	}
	
	private static class Tuple
	{
		InvokeMethodResult result;
	}
}

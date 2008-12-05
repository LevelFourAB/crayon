package se.l4.crayon.osgi.remoting.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import se.l4.crayon.osgi.remoting.OSGiRemoteManager;
import se.l4.crayon.osgi.remoting.RemoteConnection;
import se.l4.crayon.osgi.remoting.internal.def.ProxyDef;
import se.l4.crayon.osgi.remoting.internal.net.ClientNetworkChannel;
import se.l4.crayon.osgi.remoting.internal.net.ListServices;
import se.l4.crayon.osgi.remoting.internal.net.NetworkCallback;
import se.l4.crayon.osgi.remoting.internal.net.NetworkChannel;
import se.l4.crayon.osgi.remoting.internal.net.NetworkServer;
import se.l4.crayon.osgi.remoting.internal.net.RegisterService;
import se.l4.crayon.osgi.remoting.internal.net.UnregisterService;
import se.l4.crayon.osgi.remoting.internal.proxy.ProxyDefBuilder;

public class OSGiRemoteManagerImpl
	implements OSGiRemoteManager
{
	private BundleContext ctx;
	
	private Map<String, ExportedService> exported;
	private NetworkServer server;
	private List<RemoteConnection> connections;
	
	private ProxyDefBuilder builder;
	private ExecutorService executor;
	
	public OSGiRemoteManagerImpl(BundleContext ctx)
	{
		this.ctx = ctx;
		
		builder = new ProxyDefBuilder();
		exported = new HashMap<String, ExportedService>();
		connections = new CopyOnWriteArrayList<RemoteConnection>();
		
		server = new NetworkServer(this, DEFAULT_PORT);
	}
	
	public void start()
	{
		executor = Executors.newCachedThreadPool();
		server.start();
	}
	
	public void stop()
	{
		for(RemoteConnection c : connections)
		{
			c.close();
		}
		
		server.stop();
		executor.shutdownNow();
	}
	
	public RemoteConnection connect(String hostname, int port)
		throws IOException
	{
		final NetworkChannel channel = new ClientNetworkChannel(hostname, port);
		final ExecutorService service = Executors.newCachedThreadPool();
		
		final NetworkHandler handler = new NetworkHandler(this, channel, service);
		channel.setCallback(handler);
		
		channel.start();
		channel.send(new ListServices());
		
		RemoteConnection c = new RemoteConnection()
		{
			public void close()
			{
				handler.close();
				
				service.shutdownNow();
			}
		};
		
		connections.add(c);
		
		return c;
	}

	public void registerExported(ServiceReference ref, Object service)
	{
		ProxyDef def = builder.build(ref, service);
		
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		for(String s : ref.getPropertyKeys())
		{
			properties.put(s, ref.getProperty(s));
		}
		
		String id = UUID.randomUUID().toString();
		ExportedService e = new ExportedService(ref, id, def, service, properties);
		
		synchronized(exported)
		{
			exported.put(id, e);
		}
		
		try 
		{
			server.send(new RegisterService(def, id, properties));
		}
		catch(IOException e1)
		{
		}
	}
	
	public void unregisterExported(ServiceReference ref)
	{
		ExportedService service = null;
		
		synchronized(exported)
		{
			for(ExportedService es : exported.values())
			{
				if(ref.equals(es.getRef()))
				{
					service = es;
					break;
				}
			}
		}
		
		if(service != null)
		{
			String id = service.getId();
			exported.remove(id);
			
			try 
			{
				server.send(new UnregisterService(id));
			}
			catch(IOException e)
			{
			}
		}
	}
	
	public NetworkCallback createServerCallback(final NetworkChannel channel)
	{
		return new NetworkHandler(this, channel, executor);
	}
	
	public void handleListServices(final NetworkChannel channel, ListServices object)
		throws IOException
	{
		synchronized(exported)
		{
			for(ExportedService es : exported.values())
			{
				channel.send(new RegisterService(es.getDef(), es.getId(), es.getProperties()));
			}
		}
	}
	
	public ExportedService getExportedService(String id)
	{
		synchronized(exported)
		{
			return exported.get(id);
		}
	}
	
	public BundleContext getContext()
	{
		return ctx;
	}
	
	public static class ExportedService
	{
		private final ServiceReference ref;
		private final String id;
		private final ProxyDef def;
		private final Object service;
		private final Dictionary<String, Object> properties;
		
		public ExportedService(ServiceReference ref, 
			String id,
			ProxyDef def, 
			Object service, 
			Dictionary<String, Object> properties)
		{
			this.ref = ref;
			this.id = id;
			this.def = def;
			this.service = service;
			this.properties = properties;
		}
		
		public String getId()
		{
			return id;
		}
		
		public ServiceReference getRef()
		{
			return ref;
		}
		
		public ProxyDef getDef()
		{
			return def;
		}
		
		public Object getService()
		{
			return service;
		}
		
		public Dictionary<String, Object> getProperties()
		{
			return properties;
		}
	}
}

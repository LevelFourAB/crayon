package se.l4.crayon.osgi.remoting.internal.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import se.l4.crayon.osgi.remoting.internal.OSGiRemoteManagerImpl;

public class NetworkServer 
{
	private final OSGiRemoteManagerImpl manager;
	private int port;
	
	private final List<NetworkChannel> channels;
	private Thread thread;
	private ServerAcceptor acceptor;
	
	public NetworkServer(OSGiRemoteManagerImpl manager, int port)
	{
		if(port < 0 || port > 65535)
		{
			throw new IllegalArgumentException("port must be in range 0...65535");
		}
		
		this.manager = manager;
		this.port = port;
		
		channels = new LinkedList<NetworkChannel>();
	}
	
	public int getPort()
	{
		return port;
	}
	
	public void start()
	{
		acceptor = new ServerAcceptor();
		thread = new Thread(acceptor, "crayon-osgi-remoting-server");
		thread.start();
	}
	
	public void stop()
	{
		if(acceptor != null)
		{
			for(NetworkChannel channel : channels)
			{
				channel.close();
			}
			
			acceptor.interrupt();
			thread.interrupt();
			
			try
			{
				thread.join();
			}
			catch(InterruptedException e)
			{
			}
		}
	}
	
	public void send(Object object)
		throws IOException
	{
		Iterator<NetworkChannel> it = channels.iterator();
		
		while(it.hasNext())
		{
			NetworkChannel channel = it.next();
			if(false == channel.isAlive())
			{
				it.remove();
			}
			else
			{
				channel.send(object);
			}
		}
	}
	
	private class ServerAcceptor
		implements Runnable
	{
		private ServerSocket socket;
		
		public ServerAcceptor()
		{
		}
		
		public void run()
		{
			try
			{
				socket = new ServerSocket(port);
				port = socket.getLocalPort();
				
				while(false == Thread.interrupted())
				{
					try
					{
						Socket clientSocket = socket.accept();
					
						NetworkChannel channel = new ServerNetworkChannel(clientSocket);
						channel.setCallback(manager.createServerCallback(channel));
						channel.start();
						
						channel.send(new ListServices());
						
						channels.add(channel);
					}
					catch(IOException e)
					{
						// Just try again
					}
				}
			}
			catch(IOException e)
			{
				
			}
		}
		
		public void interrupt()
		{
			try
			{
				if(socket != null)
				{
					socket.close();
				}
			}
			catch(IOException e)
			{
			}
		}
	}
}

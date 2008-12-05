package se.l4.crayon.osgi.remoting.internal.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

public abstract class NetworkChannel
{
	private static final NetworkCallback NOP_CALLBACK = new NetworkCallback()
	{
		public void messageReceived(Object object) {};
		
		public void closed() {}
	};
	
	private LinkedList<Object> queue;
	private NetworkCallback callback;
	
	private Socket socket;
	private Thread reader;
	private Thread writer;

	private boolean alive;
	
	public NetworkChannel()
	{
		queue = new LinkedList<Object>();
		callback = NOP_CALLBACK;
	}
	
	public void setCallback(NetworkCallback callback)
	{
		this.callback = callback == null ? NOP_CALLBACK : callback;
	}
	
	public boolean isAlive()
	{
		return alive;
	}
	
	public synchronized void start()
		throws IOException
	{
		if(alive)
		{
			return;
		}
		
		alive = true;
		
		socket = getSocket();
		
		reader = new Thread(new IoReader(), "crayon-io-reader[" + socket + "]");
		reader.start();
		
		writer = new Thread(new IoWriter(), "crayon-io-writer[" + socket + "]");
		writer.start();
	}

	protected abstract Socket getSocket()
		throws IOException;
	
	public void send(Object object)
		throws IOException
	{
		synchronized(queue)
		{
			queue.add(object);
			queue.notifyAll();
		}
	}
	
	public void close()
	{
		synchronized(this)
		{
			if(false == alive)
			{
				return;
			}
		
			alive = false;
		}
		
		try
		{
			socket.close();
		
			writer.interrupt();
			reader.interrupt();
			
			writer.join();
			reader.join();
		}
		catch(InterruptedException e)
		{
		}
		catch(IOException e)
		{
		}
	}
	
	private void close0()
	{
		synchronized(this)
		{
			if(false == alive)
			{
				return;
			}
			
			alive = false;
		}
		
		boolean oldAlive = alive;
		
		
		try
		{
			socket.close();
		
			writer.interrupt();
			reader.interrupt();
		}
		catch(IOException e)
		{
		}
		
		if(oldAlive == true)
		{
			callback.closed();
		}
	}
	
	private class IoReader
		implements Runnable
	{
		public void run()
		{
			try
			{
				if(false == socket.isConnected() || socket.isClosed())
				{
					return;
				}
				
				InputStream stream = socket.getInputStream();
				ObjectInputStream in = new ObjectInputStream(stream);
				
				while(false == Thread.interrupted())
				{
					try
					{
						Object object = in.readObject();
						
						callback.messageReceived(object);
					}
					catch(ClassNotFoundException e)
					{
						// TODO: Handle in some way
					}
				}
			}
			catch(IOException e)
			{
			}
			
			close0();
		}
	}
	
	private class IoWriter
		implements Runnable
	{
		public void run()
		{
			try
			{
				if(false == socket.isConnected())
				{
					return;
				}
				
				OutputStream stream = socket.getOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(stream);
				
				while(false == Thread.interrupted())
				{
					Object object;
					
					synchronized(queue)
					{
						while(queue.isEmpty() && socket.isConnected())
						{
							queue.wait();
						}
						
						if(false == socket.isConnected())
						{
							Thread.currentThread().interrupt();
							continue;
						}
						
						object = queue.removeFirst();
					}
					
					out.writeObject(object);
				}
			}
			catch(IOException e)
			{
				Thread.currentThread().interrupt();
				
			}
			catch(InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			
			close0();
		}
	}
}

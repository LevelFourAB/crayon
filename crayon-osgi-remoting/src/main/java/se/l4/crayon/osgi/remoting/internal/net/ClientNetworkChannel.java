package se.l4.crayon.osgi.remoting.internal.net;

import java.io.IOException;
import java.net.Socket;

public class ClientNetworkChannel
	extends NetworkChannel
{
	private String hostname;
	private int port;
	
	private Socket socket;
	
	public ClientNetworkChannel(String hostname, int port)
	{
		this.hostname = hostname;
		this.port = port;
	}
	
	@Override
	protected Socket getSocket()
		throws IOException 
	{
		if(socket == null || false == socket.isConnected())
		{
			socket = new Socket(hostname, port);
		}
		
		return socket;
	}

}

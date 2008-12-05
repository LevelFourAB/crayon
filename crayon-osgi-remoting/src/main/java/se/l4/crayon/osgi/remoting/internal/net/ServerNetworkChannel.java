package se.l4.crayon.osgi.remoting.internal.net;

import java.net.Socket;

public class ServerNetworkChannel
	extends NetworkChannel
{
	private final Socket socket;
	
	public ServerNetworkChannel(Socket socket)
	{
		this.socket = socket;
	}
	
	protected Socket getSocket()
	{
		return socket;
	}
}

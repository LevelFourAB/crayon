package se.l4.crayon.osgi.remoting.internal.net;

import java.io.IOException;

public interface NetworkCallback
{
	void messageReceived(Object object)
		throws IOException;
	
	void closed();
}

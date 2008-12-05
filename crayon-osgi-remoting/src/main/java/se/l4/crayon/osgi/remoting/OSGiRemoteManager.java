package se.l4.crayon.osgi.remoting;

import java.io.IOException;

public interface OSGiRemoteManager 
{
	static final String REMOTE_PUBLISH = "crayon.publish";
	
	static final String REMOTE_SERVICE = "crayon.remote";
	
	static final int DEFAULT_PORT = 33400;
	
	RemoteConnection connect(String hostname, int port)
		throws IOException;
}

package se.l4.crayon.osgi.remoting;

public interface MethodInvoker
{
	Object invoke(String name, String signature, Object[] args)
		throws Throwable;
}

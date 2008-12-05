package se.l4.crayon.osgi.remoting.internal.proxy;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import se.l4.crayon.osgi.remoting.MethodInvoker;


/**
 * Proxy generator, will generate a proxy that makes remote procedure calls.
 * 
 * @author Andreas Holstenson
 *
 */
public class ProxyGenerator
{
	/** Name of the static variable containing {@link MethodInvoker}s. */
	public static final String INVOKERS_NAME = "RPC$$INVOKER";
	
	private String className;
	private String byteClassName;
	
	private List<RemoteMethod> remoteMethods;
	private String[] interfaces;
	
	public ProxyGenerator(String name, String[] interfaces)
	{
		this.interfaces = new String[interfaces.length];
		
		for(int i=0, n=interfaces.length; i<n; i++)
		{
			this.interfaces[i] = translateClassName(interfaces[i]); 
		}
		
		remoteMethods = new LinkedList<RemoteMethod>();
		
		className = name;
		byteClassName = translateClassName(className);
	}
	
	private static String translateClassName(String className)
	{
		return className.replace('.', '/');
	}
	
	public byte[] createProxy()
	{
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES );
		
		// Create the class
		cw.visit(Opcodes.V1_5, 
			Opcodes.ACC_PUBLIC, 
			byteClassName, 
			null, 
			"java/lang/Object", 
			interfaces
		);
		
		cw.visitSource("<generated>", null);

		// Create field for invokers
		String invokerName = Type.getDescriptor(MethodInvoker.class);
		FieldVisitor fv = cw.visitField(
			Opcodes.ACC_PRIVATE, 
			INVOKERS_NAME, invokerName, null, null
		);
		fv.visitEnd();
		
		// Generate a default constructor
		generateConstructor(cw);
		
		// Generate the actual methods
		createMethods(cw);
		
		cw.visitEnd();
		
		return cw.toByteArray();
	}
	
	public String getClassName()
	{
		return className;
	}
	
	private void generateConstructor(ClassWriter cw)
	{
		Method method = 
			new Method(
				"<init>",
				"()V"
			);
		
		GeneratorAdapter adapter = new GeneratorAdapter(
			Opcodes.ACC_PUBLIC,
			method,
			null,
			null,
			cw
		);
		
		adapter.loadThis();
		adapter.invokeConstructor(Type.getType(Object.class), method);
		adapter.returnValue();
		
		adapter.endMethod();
	}
	
	private void createMethod(ClassWriter cw, RemoteMethod m)
	{
		Method method = 
			new Method(
				m.name,
				m.signature
			);
		
		GeneratorAdapter adapter = new GeneratorAdapter(
			Opcodes.ACC_PUBLIC,
			method,
			null,
			null,
			cw
		);
		
		// Get the MethodInvoker to use
		adapter.loadThis();
		adapter.getField(
			Type.getObjectType(byteClassName), 
			INVOKERS_NAME,
			Type.getType(MethodInvoker.class)
		);
		
		// Load this and the argument array
		
		adapter.push(m.name);
		adapter.push(m.signature);
		
		adapter.loadArgArray();
		
		// Call the invoke method on the MethodInvoker
		Method invoke = new Method("invoke",
			Type.getObjectType("java/lang/Object"),
			Type.getArgumentTypes("(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;")
		);
		
		Type invoker = Type.getObjectType("se/l4/crayon/osgi/remoting/MethodInvoker");
		adapter.invokeInterface(invoker, invoke);
		
		Type type = Type.getReturnType(m.signature);
		if(type.getSort() != Type.VOID)
		{
			adapter.unbox(type);
			adapter.returnValue();
		}
		else
		{
			adapter.pop();
			adapter.returnValue();
		}
		
		adapter.endMethod();
	}
	
	private void createMethods(ClassWriter cw)
	{
		for(RemoteMethod m : remoteMethods)
		{
			createMethod(cw, m);
		}
	}
	
	public void addMethod(String name, String signature)
	{
		RemoteMethod m = new RemoteMethod();
		m.name = name;
		m.signature = signature;
		
		remoteMethods.add(m);
	}
	
	private static class RemoteMethod
	{
		private String name;
		private String signature;
	}
	
}


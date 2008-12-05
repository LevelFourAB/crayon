package se.l4.crayon.osgi.remoting.internal.def;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

public class ProxyDef
	implements Serializable
{
	private static final char[] DIGITS = { 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'a', 'b', 'c', 'd', 'e', 'f' 
	};
	
	private List<String> classNames;
	private List<MethodDef> methods;
	private List<ImportDef> imports;
	
	public ProxyDef()
	{
		classNames = new LinkedList<String>();
		
		methods = new LinkedList<MethodDef>();
		imports = new LinkedList<ImportDef>();
	}
	
	public String getName()
	{
		try 
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");
			for(String s : classNames)
			{
				digest.update(s.getBytes());
			}
			
			byte[] digested = digest.digest();
			
			StringBuilder builder = new StringBuilder();
			for(int i=0; i<digested.length; i++)
			{
				builder.append(DIGITS[(0xF0 & digested[i]) >>> 4]);
				builder.append(DIGITS[0x0F & digested[i]]);
			}
			
			return builder.toString();
		}
		catch(NoSuchAlgorithmException e)
		{
		}
		
		return null;
	}
	
	public void addClassName(String className)
	{
		classNames.add(className);
	}
	
	public List<String> getClassNames()
	{
		return classNames;
	}
	
	public void addImport(ImportDef def)
	{
		imports.add(def);
	}
	
	public List<ImportDef> getImports()
	{
		return imports;
	}
	
	public void addMethod(MethodDef def)
	{
		methods.add(def);
	}
	
	public void addMethod(String name, String signature)
	{
		methods.add(new MethodDef(name, signature));
	}
	
	public List<MethodDef> getMethods()
	{
		return methods;
	}
	
	@Override
	public String toString()
	{
		return "ProxyDef[classes=" + classNames + ", methods=" + methods + ", imports=" + imports + "]";
	}
}

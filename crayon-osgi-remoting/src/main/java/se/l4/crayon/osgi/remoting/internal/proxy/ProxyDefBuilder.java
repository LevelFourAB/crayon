package se.l4.crayon.osgi.remoting.internal.proxy;

import java.util.Dictionary;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import se.l4.crayon.osgi.remoting.internal.def.ImportDef;
import se.l4.crayon.osgi.remoting.internal.def.MethodDef;
import se.l4.crayon.osgi.remoting.internal.def.ProxyDef;

/**
 * Builder for proxy definitions.
 * 
 * @author Andreas Holstenson
 *
 */
public class ProxyDefBuilder 
{
	public ProxyDefBuilder()
	{
		
	}
	
	public ProxyDef build(ServiceReference ref, Object service)
	{
		Bundle bundle = ref.getBundle();
		ProxyDef def = new ProxyDef();
		
		Object objectClass = ref.getProperty("objectclass");
		String[] types = (String[]) objectClass;
		
		for(String s : types)
		{
			def.addClassName(s);
		}
		
		// Analyze the imports
		ClassLoader loader = service.getClass().getClassLoader();
		
		Analyzer analyzer = new Analyzer();
		for(String s : types)
		{
			try 
			{
				Class<?> type = loader.loadClass(s);
				analyzer.analyzeService(type);
			}
			catch(ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		
		for(MethodDef md : analyzer.getMethods())
		{
			def.addMethod(md);
		}
		
		// Compare analyzed imports to the actual imports
		@SuppressWarnings("unchecked")
		Dictionary<String, String> headers = bundle.getHeaders();
		Set<String> requiredImports = analyzer.getRequiredImports();
		
		Pattern pattern = Pattern.compile("version=\"?(.*?)\"?;?");
		for(String i : headers.get(Constants.IMPORT_PACKAGE).split(","))
		{
			Matcher matcher = pattern.matcher(i);
			if(matcher.find())
			{
				String pkg = i.substring(0, i.indexOf(';'));
				
				if(requiredImports.contains(pkg))
				{
					String vString = matcher.group(1);
					def.addImport(new ImportDef(pkg, vString));
				}
			}
			else
			{
				if(requiredImports.contains(i))
				{
					def.addImport(new ImportDef(i, ""));
				}
			}
		}
		
		return def;
	}
}

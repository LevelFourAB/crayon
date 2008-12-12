package se.l4.crayon.osgi.tooling.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Builder;

public class ManifestGenerator
{
	public void generate(Logger logger, File xml, File manifest, boolean embedXml, File embedXmlLocation)
		throws FailException
	{
		FileInputStream in = null;
		FileOutputStream out = null;
		
		try
		{
			in = new FileInputStream(xml);
			out = new FileOutputStream(manifest);
			
			generate(logger, in, out);
		}
		catch(IOException e)
		{
			throw new FailException("Unable to generate; " + e.getMessage());
		}
		catch(JDOMException e)
		{
			throw new FailException("Unable to parse XML; " + e.getMessage());
		}
		finally
		{
			if(in != null)
			{
				try
				{
					in.close();
				}
				catch(IOException e)
				{
				}
			}
			
			if(out != null)
			{
				try
				{
					out.close();
				}
				catch(IOException e)
				{
				}
			}
		}
		
		if(embedXml)
		{
			try
			{
				in = new FileInputStream(xml);
				out = new FileOutputStream(embedXmlLocation);
				
				logger.info("Copying Bundle XML to output folder");
				
				byte[] buffer = new byte[1024];
				int len;
				
				while((len = in.read(buffer)) != -1)
				{
					out.write(buffer, 0, len);
				}
				
			}
			catch(IOException e)
			{
				throw new FailException("Unable to generate; " + e.getMessage());
			}
			finally
			{
				if(in != null)
				{
					try
					{
						in.close();
					}
					catch(IOException e)
					{
					}
				}
				
				if(out != null)
				{
					try
					{
						out.close();
					}
					catch(IOException e)
					{
					}
				}
			}
		}
	}
	
	public void generate(Logger logger, InputStream input, OutputStream output)
		throws IOException, JDOMException, FailException
	{
		SAXBuilder builder = new SAXBuilder(false);
		Document document = builder.build(input);
		
		generate(logger, document.getRootElement(), output);
	}
	
	public void generate(Logger logger, Element bundle, OutputStream output)
		throws IOException, FailException
	{
		logger.info("Generating manifest file");
		
		Manifest manifest = new Manifest();
		Attributes attrs = manifest.getMainAttributes();
		Map<String, String> map = new AttributesMap(attrs);
		
		attrs.putValue("Manifest-Version", "1.0");
		
		generate0(logger, bundle, map);

		manifest.write(output);
	}
	
	private void generate0(Logger logger, Element bundle, Map<String, String> attrs)
		throws FailException
	{
		String name = bundle.getChildText("name");
		String symbolicName = bundle.getChildText("symbolicName");
		String version = bundle.getChildText("version");
		String activator = bundle.getChildText("activator");
		
		attrs.put("Bundle-ManifestVersion", "2");
		attrs.put("Bundle-SymbolicName", symbolicName);
		
		if(symbolicName == null)
		{
			throw new FailException("Bundle needs a symbolic name");
		}

		if(name != null)
		{
			attrs.put("Bundle-Name", name);
		}
		
		if(version != null)
		{
			attrs.put("Bundle-Version", version);
		}
		else
		{
			logger.warn("No version defined for bundle");
		}
		
		if(activator != null)
		{
			attrs.put("Bundle-Activator", activator);
		}
		
		// Put all requirements in the Manifest
		Element requires = bundle.getChild("requires");
		if(requires != null)
		{
			List<Element> children = requires.getChildren();
			for(Element e : children)
			{
				String rn = e.getName();
				if("package".equals(rn))
				{
					addPackage("Import-Package", attrs, e);
				}
				else if("environment".equals(rn))
				{
					addEnvironment(attrs, e);
				}
			}
		}
		
		// Put all everything that is provided in the manifest
		Element provides = bundle.getChild("provides");
		if(provides != null)
		{
			List<Element> children = requires.getChildren();
			for(Element e : children)
			{
				String rn = e.getName();
				if("package".equals(rn))
				{
					addPackage("Export-Package", attrs, e);
				}
			}
		}
		
		Element nativeCode = bundle.getChild("nativeCode");
		if(nativeCode != null)
		{
			handleNativeCode(attrs, nativeCode);
		}
		
	}
	
	private void addPackage(String attrName, Map<String, String> attrs, Element pkg)
		throws FailException
	{
		String name = pkg.getAttributeValue("name");
		if(name == null)
		{
			throw new FailException("Defined packages needs to have a name-attribute");
		}
		
		StringBuilder b = new StringBuilder();
		b.append(name);
		
		List<Attribute> list = pkg.getAttributes();
		for(Attribute a : list)
		{
			String n = a.getName();
			if(false == "name".equals(n))
			{
				b.append(";")
					.append(n)
					.append("=\"")
					.append(a.getValue())
					.append("\"");
			}
		}
		
		String s = attrs.get(attrName);
		if(s == null)
		{
			attrs.put(attrName, b.toString());
		}
		else
		{
			attrs.put(attrName, s + "," + b.toString());
		}
	}
	
	private void addEnvironment(Map<String, String> attrs, Element env)
		throws FailException
	{
		String attrName = "Bundle-RequiredExecutionEnvironment";
		String name = env.getAttributeValue("name");
		if(name == null)
		{
			throw new FailException("Environment needs to have a name-attribute");
		}
		
		String s = attrs.get(attrName);
		if(s == null)
		{
			attrs.put(attrName, name);
		}
		else
		{
			attrs.put(attrName, s + "," + name);
		}
	}
	
	private void handleNativeCode(Map<String, String> attrs, Element nativeCode)
		throws FailException
	{
		List<Element> platforms = nativeCode.getChildren("platform");
		StringBuilder result = new StringBuilder();
		
		for(Element platform : platforms)
		{
			if(result.length() != 0)
			{
				result.append(",");
			}
			
			StringBuilder builder = new StringBuilder();
			
			List<Element> libs = platform.getChildren("library");
			List<Element> os = platform.getChildren("osname");
			List<Element> processor = platform.getChildren("processor");
			
			if(libs.isEmpty())
			{
				throw new FailException("Platform definitions needs to have atleast one library");
			}
			
			for(Element e : libs)
			{
				if(builder.length() > 0)
				{
					builder.append(";");
				}
				
				builder.append(e.getText());
			}
			
			for(Element e : os)
			{
				if(builder.length() > 0)
				{
					builder.append(";");
				}
				
				builder.append("osname=").append(e.getText());
			}
			
			for(Element e : processor)
			{
				if(builder.length() > 0)
				{
					builder.append(";");
				}
				
				builder.append("processor=").append(e.getText());
			}
			
			result.append(builder);
		}
		
		attrs.put("Bundle-NativeCode", result.toString());
	}

	
	private void analyzeAndUpdate(Logger logger, Element bundle, File[] classpath)
		throws FailException, IOException, Exception
	{
		Analyzer analyzer;
		
		Properties properties = new Properties();
		Map<String, String> map = (Map) properties;

		// Generate a "manifest" but store it our properties
		generate0(logger, bundle, map);
		
		Builder builder = new Builder();
		builder.setProperties(properties);
		
		builder.setClasspath(classpath);
		
		File f = File.createTempFile("bundle", "jar");
		
		try
		{
//			builder.setJar(f);
			
			builder.build();
			
			Manifest mf = builder.getJar().getManifest();
			
			mf.write(System.out);
			System.out.println();
		}
		finally
		{
			f.delete();
		}
	}
	
	public static void main(String[] args)
		throws Exception
	{
		SAXBuilder builder = new SAXBuilder(false);
		Document document = builder.build(new File("bundle.xml"));
		
		ManifestGenerator generator = new ManifestGenerator();
		generator.analyzeAndUpdate(new Logger()
		{

			public void debug(String msg) {
				// TODO Auto-generated method stub
				
			}

			public void error(String msg) {
				// TODO Auto-generated method stub
				
			}

			public void info(String msg) {
				// TODO Auto-generated method stub
				
			}

			public void warn(String msg) {
				// TODO Auto-generated method stub
				
			}
			
		}, document.getRootElement(), new File[] { new File("src/main/java") });
		
	}
}

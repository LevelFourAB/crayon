package se.l4.crayon.osgi.tooling.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
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
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import aQute.lib.header.OSGiHeader;
import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Builder;

/**
 * Main generator, will take a Bundle XML and generate a MANIFEST.MF file
 * and a new updated XML. This class utilizes BND to allow automatic
 * expansion of the imported and exported packages.
 * 
 * @author Andreas Holstenson
 *
 */
public class BundleProcessor
{
	private final Logger logger;
	private File xml;
	private File manifest;
	private boolean embedXml;
	private File embedXmlLocation;
	private File[] classpath;
	
	private File outputDirectory;
	private File[] embeddedFiles;
	
	public BundleProcessor(Logger logger)
	{
		this.logger = logger;
	}
	
	public File getXmlLocation()
	{
		return xml;
	}
	
	public void setXmlLocation(File xml)
	{
		this.xml = xml;
	}
	
	public File getManifestLocation()
	{
		return manifest;
	}
	
	public void setManifestLocation(File manifest)
	{
		this.manifest = manifest;
	}
	
	public boolean isEmbedXml()
	{
		return embedXml;
	}
	
	public void setEmbedXml(boolean embedXml)
	{
		this.embedXml = embedXml;
	}
	
	public File getEmbedXmlLocation()
	{
		return embedXmlLocation;
	}
	
	public void setEmbedXmlLocation(File embedXmlLocation)
	{
		this.embedXmlLocation = embedXmlLocation;
	}

	public File[] getClasspath()
	{
		return classpath;
	}
	
	public void setClasspath(File[] classpath)
	{
		this.classpath = classpath;
	}
	
	public void setClasspath(List<File> classpath)
	{
		this.classpath = classpath.toArray(new File[classpath.size()]);
	}
	
	public File getOutputDirectory()
	{
		return outputDirectory;
	}
	
	public void setOutputDirectory(File outputDirectory)
	{
		this.outputDirectory = outputDirectory;
	}
	
	public File[] getEmbeddedFiles()
	{
		return embeddedFiles;
	}
	
	public void setEmbeddedFiles(File[] embeddedFiles)
	{
		this.embeddedFiles = embeddedFiles;
	}
	
	public void setEmbeddedFiles(List<File> embeddedFiles)
	{
		this.embeddedFiles = embeddedFiles.toArray(new File[embeddedFiles.size()]);
	}
	
	public void generate()
		throws FailException
	{
		FileInputStream in = null;
		FileOutputStream out = null;
		
		Element bundle;
		
		try
		{
			manifest.getParentFile().mkdirs();
			
			in = new FileInputStream(xml);
			out = new FileOutputStream(manifest);
			
			bundle = generate(in, out);
		}
		catch(JDOMException e)
		{
			throw new FailException("Unable to parse XML; " + e.getMessage());
		}
		catch(FailException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new FailException("Unable to generate; " 
				+ e.getClass().getSimpleName() + ": " + e.getMessage());
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
			// First remove things that should be in the finished XML
			bundle.removeChildren("classpath");
			
			// Write XML
			try
			{
				embedXmlLocation.getParentFile().mkdirs();
				out = new FileOutputStream(embedXmlLocation);
				
				logger.info("Generating Bundle XML in output folder");
				
				XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
				outputter.output(bundle.getDocument(), out);
			}
			catch(IOException e)
			{
				throw new FailException("Unable to generate; " + e.getMessage());
			}
			finally
			{
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
	
	public Element generate(InputStream input, OutputStream output)
		throws Exception, FailException
	{
		SAXBuilder builder = new SAXBuilder(false);
		Document document = builder.build(input);
		
		generate(document.getRootElement(), output);
		
		return document.getRootElement();
	}
	
	public void generate(Element bundle, OutputStream output)
		throws Exception, FailException
	{
		if(embeddedFiles != null && embeddedFiles.length > 0)
		{
			logger.info("Copying embedded dependencies");
			
			copyEmbeddedDependencies(bundle);			
		}
		
		logger.info("Analyzing classes");
		
		expandVariables(bundle);
		
		analyzeAndUpdate(bundle);
		
		logger.info("Generating manifest file");
		
		Manifest manifest = new Manifest();
		Attributes attrs = manifest.getMainAttributes();
		Map<String, String> map = new AttributesMap(attrs);
		
		attrs.putValue("Manifest-Version", "1.0");
		
		generate0(bundle, map);

		manifest.write(output);
	}
	
	private void generate0(Element bundle, Map<String, String> attrs)
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
					addPackage("Import-Package", attrs, e, false);
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
			List<Element> children = provides.getChildren();
			for(Element e : children)
			{
				String rn = e.getName();
				if("package".equals(rn))
				{
					String priv = e.getAttributeValue("private");
					
					if(priv != null && "true".equals(priv))
					{
						addPackage("Private-Package", attrs, e, false);
						addPackage("Export-Package", attrs, e, true);
					}
					else
					{
						addPackage("Export-Package", attrs, e, false);
					}
				}
			
			}
		}
		
		Element nativeCode = bundle.getChild("nativeCode");
		if(nativeCode != null)
		{
			handleNativeCode(attrs, nativeCode);
		}
		
		Element classpath = bundle.getChild("classpath");
		if(classpath != null)
		{
			addClasspath(attrs, classpath);
		}
	}
	
	private void addClasspath(Map<String, String> attrs, Element classpath)
		throws FailException
	{
		StringBuilder builder = new StringBuilder();
		StringBuilder resources = new StringBuilder();
		
		List<Element> paths = classpath.getChildren("path");
		
		String defaults = classpath.getAttributeValue("defaults");
		if(defaults == null || "true".equals(defaults))
		{
			builder.append(".");
		}
		else if(defaults != null && false == "false".equals(defaults))
		{
			throw new FailException("defaults-attribute of <classpath> may only have value true or false");
		}
		
		for(Element path : paths)
		{
			if(builder.length() != 0)
			{
				builder.append(", ");
			}
			
			if(resources.length() != 0)
			{
				resources.append(", ");
			}
			
			String text = path.getTextTrim();
			if(text == null || "".equals(text))
			{
				throw new FailException("Classpath entry can not be null or empty");
			}
			
			resources.append(text);
			builder.append(text);
		}
		
		attrs.put("Bundle-ClassPath", builder.toString());
		attrs.put(Analyzer.INCLUDE_RESOURCE, resources.toString());
	}
	
	private void addPackage(String attrName, Map<String, String> attrs, Element pkg, boolean invert)
		throws FailException
	{
		String name = pkg.getAttributeValue("name");
		if(name == null)
		{
			throw new FailException("Defined packages needs to have a name-attribute");
		}
		
		StringBuilder b = new StringBuilder();
		if(invert)
		{
			b.append("!");
		}
		
		b.append(name);
		
		List<Attribute> list = pkg.getAttributes();
		for(Attribute a : list)
		{
			String n = a.getName();
			if(false == "name".equals(n) && false == "private".equals(n))
			{
				b.append(";")
					.append(n);
				
				if(false == "version".equals(n))
				{
					b.append(":");
				}
				
				b
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
		else if(invert)
		{
			attrs.put(attrName, b.toString() + "," + s);
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

	private void expandVariables(Element bundle)
		throws FailException
	{
		Map<String, String> properties = new HashMap<String, String>();
		
		// Create map of properties
		Element props = bundle.getChild("properties");
		if(props != null)
		{
			List<Element> children = bundle.getChildren("property");
			for(Element e : children)
			{
				String id = e.getAttributeValue("id");
				
				if(id == null || "".equals(id))
				{
					throw new FailException("id-attribute for property is either missing or empty string");
				}
				
				properties.put(id, e.getText());
			}
		}
		
		String[] names = { "name", "symbolicName", "version" };
		
		for(String s : names)
		{
			Element e = bundle.getChild(s);
			if(e != null)
			{
				properties.put(s, e.getText());
			}
		}
		
		// Now perform actual expansion
		expand(properties, bundle);
	}
	
	private void expand(Map<String, String> props, Element e)
		throws FailException
	{
		for(Attribute a : (List<Attribute>) e.getAttributes())
		{
			a.setValue(expand(props, a.getValue()));
		}
		
		List<Element> children = e.getChildren();
		
		if(children.isEmpty())
		{
			e.setText(expand(props, e.getText()));
		}
		else
		{
			for(Element c : children)
			{
				expand(props, c);
			}
		}
	}
	
	private String expand(Map<String, String> props, String in)
		throws FailException
	{
		if(in == null)
		{
			return null;
		}
		
		String s = in;
		
		int idx = s.indexOf("${");
		while(idx != -1)
		{
			int end = s.indexOf('}', idx);
			if(end == -1)
			{
				throw new FailException("Unable to expand value, missing }; Entire text was: " + in);
			}
			
			String key = s.substring(idx+2, end);
			
			String value = props.get(key);
			if(value == null)
			{
				throw new FailException("Unable to expand value ${" + key + "}, no such property");
			}
			
			s = s.replace("${" + key + "}", value);
			
			idx = s.indexOf("${");
		}
		
		return s;
	}
	
	private void analyzeAndUpdate(Element bundle)
		throws FailException, IOException, Exception
	{
		Properties properties = new Properties();
		properties.put("-nouses", "true");
		
		Map<String, String> map = (Map) properties;

		// Generate a "manifest" but store it our properties
		generate0(bundle, map);
		
		Builder builder = new Builder();
//		builder.setBase(outputDirectory.getAbsoluteFile());
//		builder.setJar(outputDirectory);
		
		builder.setProperties(properties);
		builder.setClasspath(classpath);
		
		builder.build();
		
		for(String s : (List<String>) builder.getWarnings())
		{
			logger.warn(s);
		}
		
		for(String s : (List<String>) builder.getErrors())
		{
			logger.error(s);
		}
		
		if(false == builder.getErrors().isEmpty())
		{
			throw new FailException("Unable to generate due to previous errors");
		}
		
		Manifest mf = builder.getJar().getManifest();
		Attributes attrs = mf.getMainAttributes();
		
		Element provides = bundle.getChild("provides");
		if(provides == null)
		{
			provides = new Element("provides");
			bundle.addContent(provides);
		}
		
		copyPackageValues(provides, attrs.getValue("Export-Package"));
		
		Element requires = bundle.getChild("requires");
		if(requires == null)
		{
			requires = new Element("requires");
			bundle.addContent(requires);
		}
		
		copyPackageValues(requires, attrs.getValue("Import-Package"));
	}
	
	/**
	 * Copy values from a given package string into a root-elemnt (either
	 * provides or requires-element).
	 * 
	 * @param root
	 * @param mf
	 */
	private void copyPackageValues(Element root, String pkgString)
	{
		root.removeChildren("package");
		
		if(pkgString == null || "".equals(pkgString))
		{
			return;
		}
		
		Map<String, Map<String, String>> header = OSGiHeader.parseHeader(pkgString);
		
		for(Map.Entry<String, Map<String, String>> pkg : header.entrySet())
		{
			Element el = new Element("package");
			el.setAttribute("name", pkg.getKey());
			
			for(Map.Entry<String, String> attr : pkg.getValue().entrySet())
			{
				String key = attr.getKey();
				
				if(key.endsWith(":"))
				{
					key = key.substring(0, key.length() - 1);
				}
				
				el.setAttribute(key, attr.getValue());
			}
			
			root.addContent(el);
		}
	}
	
	private void copyEmbeddedDependencies(Element bundle)
		throws FailException
	{
		if(embeddedFiles.length == 0)
		{
			return;
		}
		
		Element classpath = bundle.getChild("classpath");
		if(classpath == null)
		{
			classpath = new Element("classpath");
			bundle.addContent(classpath);
		}
		
		for(File f : embeddedFiles)
		{
			File output = new File(outputDirectory, f.getName());
			if(output.exists() && output.lastModified() < f.lastModified())
			{
				continue;
			}
			
			FileInputStream in = null;
			FileOutputStream out = null;
			
			try
			{
				in = new FileInputStream(f);
				out = new FileOutputStream(output);
				
				byte[] buffer = new byte[1024];
				int len;
				
				while((len = in.read(buffer)) != -1)
				{
					out.write(buffer, 0, len);
				}
				
				Element path = new Element("path");
				path.setText(f.getName());
				classpath.addContent(path);
			}
			catch(IOException e)
			{
				try
				{
					if(in != null)
					{
						in.close();
					}
				}
				catch(IOException e2)
				{
				}
				
				try
				{
					if(out != null)
					{
						out.close();
					}
				}
				catch(IOException e2)
				{
				}
			}
		}
	}
}

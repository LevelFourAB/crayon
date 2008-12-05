package se.l4.crayon.osgi.remoting.internal.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;

import se.l4.crayon.osgi.remoting.internal.def.ImportDef;
import se.l4.crayon.osgi.remoting.internal.def.MethodDef;
import se.l4.crayon.osgi.remoting.internal.def.ProxyDef;

public class BundleGenerator
{
	public BundleGenerator()
	{
	}
	
	public BundleDef generateBundle(ProxyDef def)
		throws IOException
	{
		String name = def.getName();
		String pkg = "crayon.proxy." + name;
		
		String[] interfaces = def.getClassNames().toArray(new String[0]);
		ProxyGenerator generator = new ProxyGenerator(pkg + ".ServiceProxy", interfaces);
		for(MethodDef d : def.getMethods())
		{
			generator.addMethod(d.getName(), d.getSignature());
		}
		
		byte[] proxy = generator.createProxy();
		
		Manifest manifest = new Manifest();
		
		Attributes attrs = manifest.getMainAttributes();
		attrs.putValue("Manifest-Version", "1.0");
		attrs.putValue("Created-By", "Crayon OSGi Remoting");
		attrs.putValue("Bundle-ManifestVersion", "2");
		attrs.putValue("Bundle-SymbolicName", pkg);
		
		// Store the actual services that are provided
		StringBuilder classes = new StringBuilder();
		for(String className : def.getClassNames())
		{
			classes.append(className).append(" ");
		}
		
		attrs.putValue("Remote-Services", classes.toString());
		
		// Store the imports of the bundle
		StringBuilder imports = new StringBuilder();
		imports.append("se.l4.crayon.osgi.remoting, ");
		for(ImportDef id : def.getImports())
		{
			imports.append(id.getPackageName());
			
			if(false == "".equals(id.getVersion()))
			{
				imports.append(";version=").append(id.getVersion());
			}
			
			imports.append(",");
		}
		
		attrs.putValue("Import-Package", imports.toString());
		attrs.putValue("Export-Package", pkg);
		
		// Create the Bundle
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JarOutputStream jar = new JarOutputStream(out, manifest);

		CRC32 crc = new CRC32();
		crc.update(proxy);
		
		JarEntry entry = new JarEntry(generator.getClassName().replace('.', '/') + ".class");
		entry.setSize(proxy.length);
		entry.setCrc(crc.getValue());
		
		jar.putNextEntry(entry);
		jar.write(proxy);
		jar.flush();
		jar.closeEntry();
		
		jar.finish();
		jar.close();
		
		byte[] jarData = out.toByteArray();
		
		return new BundleDef(
			pkg,
			new ByteArrayInputStream(jarData)
		);
	}
	
	public static class BundleDef
	{
		private final InputStream stream;
		private final String symbolicName;
		
		public BundleDef(String symbolicName, InputStream stream)
		{
			this.symbolicName = symbolicName;
			this.stream = stream;
		}
		
		public String getSymbolicName()
		{
			return symbolicName;
		}
		
		public InputStream getStream()
		{
			return stream;
		}
	}
}

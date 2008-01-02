package se.l4.crayon.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import se.l4.crayon.ConfigurationException;

/**
 * Class loader that can look for Jar-files in a directory.
 * 
 * @author Andreas Holstenson
 *
 */
public class CustomClassLoader
	extends URLClassLoader
{

	public CustomClassLoader(ClassLoader parent)
	{
		super(new URL[0], parent);
	}

	public void addDirectory(File file)
	{
		if(file.isDirectory())
		{
			throw new ConfigurationException(file + " is not a directory");
		}
		
		for(File f : file.listFiles())
		{
			if(f.isDirectory())
			{
				addDirectory(f);
			}
			else if(f.getName().toLowerCase().endsWith(".jar"))
			{
				try
				{
					addURL(f.toURI().toURL());
				}
				catch(MalformedURLException e)
				{
				}
			}
		}
	}
}

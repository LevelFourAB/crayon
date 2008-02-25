/*
 * Copyright 2008 Andreas Holstenson
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.l4.crayon.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.crayon.ConfigurationException;

/**
 * Class that helps in locating modules in Jar-files, used by the micro-kernel
 * to locate which modules should be run.
 * 
 * @author Andreas Holstenson
 *
 */
public class ClassLocator
{
	private static Logger logger = LoggerFactory.getLogger(ClassLocator.class);
	
	private ClassLocator()
	{
		
	}
	
	/**
	 * Retrieve a list of classes implementing or extending the given class.
	 * 
	 * @param <T>
	 * @param loader
	 * @param type
	 * @param manifestKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<Class<T>> getClassModules(ClassLoader loader,
			Class<T> type, 
			String manifestKey)
	{
		List<String> classes = getModules(loader, manifestKey);
		List<Class<T>> result = new LinkedList<Class<T>>();
		
		for(String s : classes)
		{
			try
			{
				Class<?> c = loader.loadClass(s);
				c.asSubclass(type);
				
				result.add((Class<T>) c);
			}
			catch(ClassCastException e)
			{
				logger.info("Unable to load " + s + ", wrong type");
			}
			catch(ClassNotFoundException e)
			{
				logger.info("Unable to load " + s + ", can not find class");
			}
		}
		
		return result;
	}
	
	/**
	 * Scan through libraries and locate class names given in a certain
	 * manifest key.
	 * 
	 * @param loader
	 * 		class loader to use for locating resources
	 * @param manifestKey
	 * 		manifest key to look in
	 * @return
	 */
	public static List<String> getModules(ClassLoader loader, 
			String manifestKey)
	{
		// list to store result in
		List<String> result = new LinkedList<String>();
		
		try
		{
			// retrieve resources and iterate through them
			Enumeration<URL> urls = loader.getResources("META-INF/MANIFEST.MF");

			while (urls.hasMoreElements())
			{
				URL url = urls.nextElement();

				addFromManifest(manifestKey, url, result);
			}
			
			return result;
		}
		catch(IOException e)
		{
			throw new ConfigurationException(e.getMessage(), e);
		}
	}
	
	private static void addFromManifest(String manifestKey,
			URL manifestUrl, 
			List<String> result)
		throws IOException
	{
		InputStream stream = manifestUrl.openStream();
		
		try
		{
			Manifest manifest = new Manifest(stream);
			
			String list = manifest.getMainAttributes().getValue(manifestKey);
			
			if(list == null)
			{
				return;
			}
			
			String[] data = list.split(",");
			for(String s : data)
			{
				result.add(s.trim());
			}
		}
		finally
		{
			stream.close();
		}
	}
}

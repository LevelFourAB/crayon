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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger =
		LoggerFactory.getLogger(CustomClassLoader.class);
	
	public CustomClassLoader(ClassLoader parent)
	{
		super(new URL[0], parent);
	}

	public void addDirectory(File file)
	{
		if(false == file.isDirectory())
		{
			logger.error("Tried adding non-directory: " + file);
			throw new ConfigurationException(file + " is not a directory");
		}
		
		if(false == file.canRead())
		{
			logger.info("Unable to read directory: " + file);
			return;
		}
		
		logger.info("Adding directory: " + file);
		
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
					logger.debug("Added jar-file: " + f);
				}
				catch(MalformedURLException e)
				{
				}
			}
		}
	}
}

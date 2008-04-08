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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.crayon.annotation.Module;

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
	 * Retrieve all classes that have been annotated with {@link Module}.
	 * 
	 * @return
	 */
	public static List<Class<?>> getAnnotated()
	{
		List<Class<?>> result = new LinkedList<Class<?>>();
		
		try
		{
			AnnotationDB db = new AnnotationDB();
			db.setScanClassAnnotations(true);
			
			db.scanArchives(
				ClasspathUrlFinder.findClassPaths()
			);
			
			Map<String, Set<String>> annotations = db.getAnnotationIndex();
			Set<String> modules =
				annotations.get(se.l4.crayon.annotation.Module.class.getName());
			
			if(modules != null)
			{
				for(String s : modules)
				{
					try
					{
						Class<?> c = Class.forName(s);
						result.add(c);
					}
					catch(ClassNotFoundException e)
					{
						logger.info("Unable to load " + s + ", can not find class");
					}
				}
			}
		}
		catch(IOException e)
		{
			logger.warn("Unable to scan for classes; {}", e.getMessage());
		}
		
		return result;
	}
}

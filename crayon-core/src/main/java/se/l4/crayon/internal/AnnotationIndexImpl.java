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
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import se.l4.crayon.AnnotationIndex;
import se.l4.crayon.annotation.AutoLoad;

@Singleton
public class AnnotationIndexImpl
	implements AnnotationIndex
{
	private static final Logger logger = 
		LoggerFactory.getLogger(AnnotationIndexImpl.class);
	
	private Map<String, Set<String>> annotations;
	
	public AnnotationIndexImpl()
		throws IOException
	{
		AnnotationDB db = new AnnotationDB();
		db.setScanClassAnnotations(true);
		db.setScanFieldAnnotations(false);
		db.setScanMethodAnnotations(false);
		db.setScanParameterAnnotations(false);
		
		db.scanArchives(
			ClasspathUrlFinder.findClassPaths()
		);
		
		annotations = db.getAnnotationIndex();
	}
	
	public Set<String> getAnnotatedClasses(Class<? extends Annotation> type)
	{
		return getAnnotatedClasses(type.getName());
	}

	public Set<String> getAnnotatedClasses(String type)
	{
		Set<String> result = annotations.get(type);
		return result == null ? Collections.EMPTY_SET : result;
	}

	public Set<Class<?>> loadAnnotatedClasses(Class<? extends Annotation> type)
	{
		return loadAnnotatedClasses(type.getName());
	}

	public Set<Class<?>> loadAnnotatedClasses(String type)
	{
		Set<String> classes = getAnnotatedClasses(type);
		Set<Class<?>> result = new HashSet<Class<?>>();
		
		for(String s : classes)
		{
			try
			{
				Class<?> c = Class.forName(s);
				result.add(c);
			}
			catch(ClassNotFoundException e)
			{
				logger.warn("Unable to load " + s + ", can not find class");
			}
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> Set<Class<T>> getAutoLoaded(Class<T> type)
	{
		Set<Class<T>> result = new HashSet<Class<T>>();
		
		for(Class<?> c : loadAnnotatedClasses(AutoLoad.class))
		{
			if(type.isAssignableFrom(c))
			{
				result.add((Class<T>) c);
			}
		}
		
		return result;
	}
	
}

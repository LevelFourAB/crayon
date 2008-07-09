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
package se.l4.crayon;

import java.lang.annotation.Annotation;
import java.util.Set;

import se.l4.crayon.annotation.AutoLoad;

/**
 * A search index for annotations that have been found in the classpath. This
 * index is built the first time it is accessed or when 
 * {@link Configurator#discover()} is run.
 * 
 * @author Andreas Holstenson
 *
 */
public interface AnnotationIndex
{
	/**
	 * Get all the classes that have been annotated with the given
	 * annotation.
	 * 
	 * @param type
	 * 		type of annotation
	 * @return
	 * 		set with class names
	 */
	Set<String> getAnnotatedClasses(Class<? extends Annotation> type);
	
	/**
	 * Get all the classes that have been annotated with the given
	 * annotation.
	 * 
	 * @param type
	 * 		type of annotation
	 * @return
	 * 		set with class names
	 */
	Set<String> getAnnotatedClasses(String type);
	
	/**
	 * Load all the classes that have been annotated with the given annotation.
	 * 
	 * @param type
	 * 		type of annotation
	 * @return
	 * 		set with classes
	 */
	Set<Class<?>> loadAnnotatedClasses(Class<? extends Annotation> type);
	
	/**
	 * Load all the classes that have been annotated with the given annotation.
	 * 
	 * @param type
	 * 		class name of annotation
	 * @return
	 * 		set with classes
	 */
	Set<Class<?>> loadAnnotatedClasses(String type);

	/**
	 * Get classes that have been marked with {@link AutoLoad} that are of
	 * a certain type.
	 * 
	 * @param <T>
	 * 		type
	 * @param type
	 * 		class that should be searched for
	 * @return
	 * 		set with classes matching {@code type}
	 */
	<T> Set<Class<T>> getAutoLoaded(Class<T> type);
}

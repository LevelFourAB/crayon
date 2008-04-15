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
package se.l4.crayon.persist;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method that an annotation should be automatically created around
 * it. If a transaction is already active the method will join that transaction.
 * If there is none a new transaction will be created, this transaction will
 * be committed if the method returns without throwing any exceptions. If
 * an exception is thrown the transaction is canceled, if it is the top-level
 * transactional method.
 * 
 * <p>
 * It is possible to specify valid exceptions by using the
 * {@link #rollbackOn()} value. When such an exception is encountered the
 * transaction will be committed even though the exception was thrown.
 * 
 * @author Andreas Holstenson
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactional
{
	Class<? extends Throwable>[] rollbackOn() default {};
}

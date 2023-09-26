/*
 * Copyright (c) 2023 Kevin Zuman (Greenadine)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package co.aikar.commands.javacord.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link SubcommandOptions} annotation is used to define the options for a <b>slash</b> subcommand.
 *
 * @since 0.5.0
 * @author Greenadine
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SubcommandOptions {
    boolean isAutoCompletable() default false;
    long longMinValue() default Long.MIN_VALUE;
    long longMaxValue() default Long.MAX_VALUE;
    double decimalMinValue() default Double.MIN_VALUE;
    double decimalMaxValue() default Double.MAX_VALUE;
    long minLength() default 0; // TODO check what the actual default value is
    long maxLength() default Integer.MAX_VALUE;
}

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
 * The {@link Choices} annotation is used to define the choices (possible options) for a slash command parameter.
 * <p>
 * This annotation can be used on any parameters of any numeric type (i.e., {@code int}, {@code long}, {@code double}),
 * as well as their respective wrapper classes and {@code String}s.
 * <p>
 * The value of the annotation is a {@code String} that must contain the choices separated by a comma (','). Each choice contains
 * the name of the choice and the value of the choice separated by an equals ('=') sign. When the command is executed,
 * the value of the choice will be passed to the method.
 *
 * @since 0.5.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Choices {
    String value();
}

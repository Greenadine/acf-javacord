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
 * The {@link BotUser} annotation is used to mark that a parameter should be resolved in relation to the bots user object.
 * <p>
 * Putting this annotation on a parameter will force the parameter to be resolved in relation to the bots user object,
 * instead of being resolved from regular command context. For example, if a parameter of type {@link org.javacord.api.entity.user.User}
 * is marked with the {@link BotUser} annotation, it will be resolved to the bots user object, regardless of the command input.
 *
 * @since 0.3.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface BotUser {
}

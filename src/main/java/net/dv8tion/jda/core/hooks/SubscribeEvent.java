/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.hooks;

import java.lang.annotation.*;

/**
 * Annotation used by the {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager}
 * this is only picked up if the event manager implementation has been set to use the {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager}
 * via {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder.setEventManager(IEventManager)}
 *
 * @see net.dv8tion.jda.core.hooks.AnnotatedEventManager
 * @see net.dv8tion.jda.core.JDABuilder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SubscribeEvent
{
}

/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.api.hooks

import java.lang.annotation.Inherited

/**
 * Annotation used by the [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager]
 * this is only picked up if the event manager implementation has been set to use the [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager]
 * via [JDABuilder.setEventManager(IEventManager)][net.dv8tion.jda.api.JDABuilder.setEventManager]
 *
 * @see net.dv8tion.jda.api.hooks.AnnotatedEventManager
 *
 * @see net.dv8tion.jda.api.JDABuilder
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Inherited
annotation class SubscribeEvent

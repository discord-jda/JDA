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

/**
 * EventManager and EventListener implementations and interfaces.
 *
 * <p>Every JDA instance has an {@link net.dv8tion.jda.core.hooks.IEventManager EventManager} implementation
 * that deals with the handling and forwarding of {@link net.dv8tion.jda.core.events.Event Events}.
 *
 * <p>The default manager is the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventManager}
 * which uses the {@link net.dv8tion.jda.core.hooks.EventListener EventListener} to listen for events.
 * <br>The {@link net.dv8tion.jda.core.hooks.ListenerAdapter ListenerAdapter} is an implementation which provides
 * methods for each event of {@link net.dv8tion.jda.core.events}
 *
 * <p>The {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager}
 * can forward events directly to methods that have the {@link net.dv8tion.jda.core.hooks.SubscribeEvent SubscribeEvent} annotation.
 *
 * <p><b>Note: All of the standard EventManager implementations are synchronized</b>
 */
package net.dv8tion.jda.core.hooks;

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
 * Package which contains all utilities for the JDA library.
 * These are used by JDA itself and can also be useful for the library consumer!
 *
 * <p>List of utilities:
 * <ul>
 *     <li>{@link net.dv8tion.jda.core.utils.IOUtil IOUtil}
 *     <br>Allows to read from files (use case: sending files)</li>
 *
 *     <li>{@link net.dv8tion.jda.core.utils.MiscUtil MiscUtil}
 *     <br>Various operations that don't have specific utility classes yet, mostly internals that are accessible from JDA entities</li>
 *
 *     <li>{@link net.dv8tion.jda.core.utils.PermissionUtil PermissionUtil}
 *     <br>Focused all around the {@link net.dv8tion.jda.core.Permission Permissions enum},
 *         used to check whether a certain entity has case-by-case permissions</li>
 *
 *     <li>{@link net.dv8tion.jda.core.utils.WidgetUtil WidgetUtil}
 *     <br>This is not bound to a JDA instance and can view the {@link net.dv8tion.jda.core.utils.WidgetUtil.Widget Widget}
 *         for a specified Guild. (by id)</li>
 * </ul>
 */
package net.dv8tion.jda.core.utils;

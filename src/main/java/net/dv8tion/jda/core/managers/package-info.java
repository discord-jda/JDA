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
 * Managers that allow to modify (PATCH) existing entities
 * with either an update task or atomic setters. This also includes classes which allow to
 * operate on entities like the moderation of a {@link net.dv8tion.jda.core.entities.Guild Guild}.
 *
 * <p>Manager types allow to directly modify one or more values
 *
 * <p>All managers require a RestAction execution.
 */
package net.dv8tion.jda.core.managers;

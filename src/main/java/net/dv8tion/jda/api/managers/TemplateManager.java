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

package net.dv8tion.jda.api.managers;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface TemplateManager extends Manager<TemplateManager>
{
    long NAME = 0x1;
    long DESCRIPTION = 0x2;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(TemplateManager.NAME | TemplateManager.DESCRIPTION);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #DESCRIPTION}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return TemplateManager for chaining convenience
     */
    @Nonnull
    @Override
    TemplateManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(TemplateManager.NAME, TemplateManager.DESCRIPTION);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #DESCRIPTION}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return TemplateManager for chaining convenience
     */
    @Nonnull
    @Override
    TemplateManager reset(long... fields);

    /**
     * Sets the name of this {@link net.dv8tion.jda.api.entities.Template Template}.
     *
     * @param  name
     *         The new name for this {@link net.dv8tion.jda.api.entities.Template Template}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 1-100 characters long
     *
     * @return TemplateManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    TemplateManager setName(@Nonnull String name);

    /**
     * Sets the description of this {@link net.dv8tion.jda.api.entities.Template Template}.
     *
     * @param  description
     *         The new description for this {@link net.dv8tion.jda.api.entities.Template Template}
     *
     * @throws IllegalArgumentException
     *         If the provided description is not between 0-120 characters long
     *
     * @return TemplateManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    TemplateManager setDescription(@Nullable String description);
}

/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.events.channel.category.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;

import javax.annotation.Nonnull;

/**
 * Indicates that the position of a {@link net.dv8tion.jda.api.entities.Category Category} was updated.
 *
 * <p>Can be used to retrieve the old position
 *
 * <p>Identifier: {@code position}
 */
public class CategoryUpdatePositionEvent extends GenericCategoryUpdateEvent<Integer>
{
    public static final String IDENTIFIER = "position";

    public CategoryUpdatePositionEvent(@Nonnull JDA api, long responseNumber, @Nonnull Category category, int oldPosition)
    {
        super(api, responseNumber, category, oldPosition, category.getPositionRaw(), IDENTIFIER);
    }

    /**
     * The previous position of this {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @return The previous position
     */
    public int getOldPosition()
    {
        return getOldValue();
    }

    /**
     * The new position of this {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @return The new position
     */
    public int getNewPosition()
    {
        return getNewValue();
    }
}

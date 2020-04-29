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

package net.dv8tion.jda.api.events.channel.category;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Category Category} was created/deleted/updated.
 * <br>Every category event is a subclass of this event and can be casted
 *
 * <p>Can be used to detect that any category event was fired
 */
public abstract class GenericCategoryEvent extends Event
{
    protected final Category category;

    public GenericCategoryEvent(@Nonnull JDA api, long responseNumber, @Nonnull Category category)
    {
        super(api, responseNumber);
        this.category = category;
    }

    /**
     * The responsible {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @return The Category
     */
    @Nonnull
    public Category getCategory()
    {
        return category;
    }

    /**
     * The snowflake ID for the responsible {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @return The ID for the category
     */
    @Nonnull
    public String getId()
    {
        return Long.toUnsignedString(getIdLong());
    }

    /**
     * The snowflake ID for the responsible {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @return The ID for the category
     */
    public long getIdLong()
    {
        return category.getIdLong();
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild}
     * the responsible {@link net.dv8tion.jda.api.entities.Category Category} is part of.
     *
     * @return The {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    @Nonnull
    public Guild getGuild()
    {
        return category.getGuild();
    }
}

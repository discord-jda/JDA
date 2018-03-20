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

package net.dv8tion.jda.core.events.channel.category.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.events.UpdateEvent;
import net.dv8tion.jda.core.events.channel.category.GenericCategoryEvent;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.Category Category} was updated.
 * <br>Every category update event derived from this event and can be casted.
 *
 * <p>Can be used to detect any category update event
 */
public abstract class GenericCategoryUpdateEvent<T> extends GenericCategoryEvent implements UpdateEvent<Category, T>
{
    protected final T previous;
    protected final T next;
    protected final String identifier;

    public GenericCategoryUpdateEvent(
        JDA api, long responseNumber, Category category,
        T previous, T next, String identifier)
    {
        super(api, responseNumber, category);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @Override
    public Category getEntity()
    {
        return getCategory();
    }

    @Override
    public String getPropertyIdentifier()
    {
        return identifier;
    }

    @Override
    public T getOldValue()
    {
        return previous;
    }

    @Override
    public T getNewValue()
    {
        return next;
    }

    @Override
    public String toString()
    {
        return "CategoryUpdate[" + getPropertyIdentifier() + "](" + getOldValue() + "->" + getNewValue() + ')';
    }
}

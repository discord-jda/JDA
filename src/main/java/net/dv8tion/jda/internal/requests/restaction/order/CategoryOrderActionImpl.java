/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.requests.restaction.order;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Collectors;

public class CategoryOrderActionImpl<T extends GuildChannel>
    extends ChannelOrderActionImpl<T>
    implements CategoryOrderAction<T>
{
    protected final Category category;

    /**
     * Creates a new CategoryOrderAction for the specified {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @param  category
     *         The target {@link net.dv8tion.jda.api.entities.Category Category}
     *         which the new CategoryOrderAction will order channels from.
     * @param  type
     *         The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType} that
     *         matches the returning value of {@link net.dv8tion.jda.api.entities.GuildChannel#getType() GuildChannel#getType()}
     *         for the generic {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel} type {@code T}.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the {@code ChannelType} is not one that can be retrieved from a {@code Category}.
     *         Currently the only two allowed are {@link ChannelType#TEXT} and {@link ChannelType#VOICE}.
     */
    @SuppressWarnings("unchecked")
    public CategoryOrderActionImpl(Category category, ChannelType type)
    {
        super(category.getGuild(), type, (Collection<T>) getChannelsOfType(category, type));
        this.category = category;
    }

    @Nonnull
    @Override
    public Category getCategory()
    {
        return category;
    }

    @Override
    protected void validateInput(T entity)
    {
        Checks.notNull(entity, "Provided channel");
        Checks.check(getCategory().equals(entity.getParent()), "Provided channel's Category is not this Category!");
        Checks.check(orderList.contains(entity), "Provided channel is not in the list of orderable channels!");
    }

    @Nonnull
    private static Collection<? extends GuildChannel> getChannelsOfType(Category category, ChannelType type)
    {
        Checks.notNull(type, "ChannelType");
        Checks.notNull(category, "Category");
        return ChannelOrderActionImpl.getChannelsOfType(category.getGuild(), type).stream()
             .filter(it -> category.equals(it.getParent()))
             .sorted()
             .collect(Collectors.toList());
    }
}

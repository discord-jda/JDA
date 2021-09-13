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

package net.dv8tion.jda.internal.requests.restaction.order;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.StandardGuildChannel;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Collectors;

public class CategoryOrderActionImpl
    extends ChannelOrderActionImpl
    implements CategoryOrderAction
{
    protected final Category category;

    /**
     * Creates a new CategoryOrderAction for the specified {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @param  category
     *         The target {@link net.dv8tion.jda.api.entities.Category Category}
     *         which the new CategoryOrderAction will order channels from.
     * @param  bucket
     *         The sorting bucket
     */
    public CategoryOrderActionImpl(Category category, int bucket)
    {
        super(category.getGuild(), bucket, getChannelsOfType(category, bucket));
        this.category = category;
    }

    @Nonnull
    @Override
    public Category getCategory()
    {
        return category;
    }

    @Override
    protected void validateInput(StandardGuildChannel entity)
    {
        Checks.notNull(entity, "Provided channel");
        Checks.check(getCategory().equals(entity.getParentCategory()), "Provided channel's Category is not this Category!");
        Checks.check(orderList.contains(entity), "Provided channel is not in the list of orderable channels!");
    }

    @Nonnull
    private static Collection<StandardGuildChannel> getChannelsOfType(Category category, int bucket)
    {
        Checks.notNull(category, "Category");
        return ChannelOrderActionImpl.getChannelsOfType(category.getGuild(), bucket).stream()
             .filter(it -> category.equals(it.getParentCategory()))
             .sorted()
             .collect(Collectors.toList());
    }
}

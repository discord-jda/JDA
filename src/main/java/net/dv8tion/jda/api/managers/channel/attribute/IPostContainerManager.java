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

package net.dv8tion.jda.api.managers.channel.attribute;

import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer.SortOrder;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Manager abstraction to configure settings related to forum post containers, such as {@link ForumChannel}.
 *
 * @param <T> The channel type
 * @param <M> The manager type
 */
public interface IPostContainerManager<T extends IPostContainer, M extends IPostContainerManager<T, M>> extends IThreadContainerManager<T, M>, IPermissionContainerManager<T, M>
{
    /**
     * Sets the tag requirement state of this {@link IPostContainer}.
     * <br>If true, all new posts must have at least one tag.
     *
     * @param  requireTag
     *         The new tag requirement state for the selected {@link IPostContainer}
     *
     * @return ChannelManager for chaining convenience.
     *
     * @see    IPostContainer#isTagRequired()
     */
    @Nonnull
    @CheckReturnValue
    M setTagRequired(boolean requireTag);

    /**
     * Sets the <b><u>available tags</u></b> of the selected {@link IPostContainer}.
     * <br>Tags will be ordered based on the provided list order.
     *
     * <p>This is a full replacement of the tags list, all missing tags will be removed.
     * You can use {@link ForumTagData} to create new tags or update existing ones.
     *
     * <p><b>Example</b>
     * <pre>{@code
     * List<BaseForumTag> tags = new ArrayList<>(channel.getAvailableTags());
     * tags.add(new ForumTagData("question").setModerated(true)); // add a new tag
     * tags.set(0, ForumTagData.from(tags.get(0)).setName("bug report")); // update an existing tag
     * // Update the tag list
     * channel.getManager().setAvailableTags(tags).queue();
     * }</pre>
     *
     * @param  tags
     *         The new available tags in the desired order.
     *
     * @throws IllegalArgumentException
     *         If the provided list is null or contains null elements
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    IPostContainer#getAvailableTags()
     */
    @Nonnull
    @CheckReturnValue
    M setAvailableTags(@Nonnull List<? extends BaseForumTag> tags);

    /**
     * Sets the <b><u>default reaction emoji</u></b> of the selected {@link IPostContainer}.
     * <br>This does not support custom emoji from other guilds.
     *
     * @param  emoji
     *         The new default reaction emoji, or null to unset.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    IPostContainer#getDefaultReaction()
     */
    @Nonnull
    @CheckReturnValue
    M setDefaultReaction(@Nullable Emoji emoji);

    /**
     * Sets the <b><u>default sort order</u></b> of the selected {@link IPostContainer}.
     *
     * @param  sortOrder
     *         The new {@link SortOrder}
     *
     * @throws IllegalArgumentException
     *         If null or {@link SortOrder#UNKNOWN} is provided
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    IPostContainer#getDefaultSortOrder()
     */
    @Nonnull
    @CheckReturnValue
    M setDefaultSortOrder(@Nonnull SortOrder sortOrder);
}

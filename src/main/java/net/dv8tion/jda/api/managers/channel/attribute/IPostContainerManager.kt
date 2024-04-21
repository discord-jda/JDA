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
package net.dv8tion.jda.api.managers.channel.attribute

import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag
import net.dv8tion.jda.api.entities.emoji.Emoji
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager abstraction to configure settings related to forum post containers, such as [ForumChannel].
 *
 * @param <T> The channel type
 * @param <M> The manager type
</M></T> */
interface IPostContainerManager<T : IPostContainer?, M : IPostContainerManager<T, M>?> : IThreadContainerManager<T, M>,
    IPermissionContainerManager<T, M> {
    /**
     * Sets the tag requirement state of this [IPostContainer].
     * <br></br>If true, all new posts must have at least one tag.
     *
     * @param  requireTag
     * The new tag requirement state for the selected [IPostContainer]
     *
     * @return ChannelManager for chaining convenience.
     *
     * @see IPostContainer.isTagRequired
     */
    @Nonnull
    @CheckReturnValue
    fun setTagRequired(requireTag: Boolean): M

    /**
     * Sets the **<u>available tags</u>** of the selected [IPostContainer].
     * <br></br>Tags will be ordered based on the provided list order.
     *
     *
     * This is a full replacement of the tags list, all missing tags will be removed.
     * You can use [ForumTagData] to create new tags or update existing ones.
     *
     *
     * **Example**
     * <pre>`List<BaseForumTag> tags = new ArrayList<>(channel.getAvailableTags());
     * tags.add(new ForumTagData("question").setModerated(true)); // add a new tag
     * tags.set(0, ForumTagData.from(tags.get(0)).setName("bug report")); // update an existing tag
     * // Update the tag list
     * channel.getManager().setAvailableTags(tags).queue();
    `</pre> *
     *
     * @param  tags
     * The new available tags in the desired order.
     *
     * @throws IllegalArgumentException
     * If the provided list is null or contains null elements
     *
     * @return ChannelManager for chaining convenience
     *
     * @see IPostContainer.getAvailableTags
     */
    @Nonnull
    @CheckReturnValue
    fun setAvailableTags(@Nonnull tags: List<BaseForumTag?>?): M

    /**
     * Sets the **<u>default reaction emoji</u>** of the selected [IPostContainer].
     * <br></br>This does not support custom emoji from other guilds.
     *
     * @param  emoji
     * The new default reaction emoji, or null to unset.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see IPostContainer.getDefaultReaction
     */
    @Nonnull
    @CheckReturnValue
    fun setDefaultReaction(emoji: Emoji?): M

    /**
     * Sets the **<u>default sort order</u>** of the selected [IPostContainer].
     *
     * @param  sortOrder
     * The new [SortOrder]
     *
     * @throws IllegalArgumentException
     * If null or [SortOrder.UNKNOWN] is provided
     *
     * @return ChannelManager for chaining convenience
     *
     * @see IPostContainer.getDefaultSortOrder
     */
    @Nonnull
    @CheckReturnValue
    fun setDefaultSortOrder(@Nonnull sortOrder: IPostContainer.SortOrder?): M
}

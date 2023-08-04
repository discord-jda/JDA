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

package net.dv8tion.jda.api.events.channel.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelField;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Indicates that the tags applied to a {@link ThreadChannel forum post thread} have been updated.
 *
 * <p>Limited to {@link ThreadChannel ThreadChannels} inside of {@link ForumChannel ForumChannels}
 *
 * @see ThreadChannel#getAppliedTags()
 * @see ChannelField#APPLIED_TAGS
 */
public class ChannelUpdateAppliedTagsEvent extends GenericChannelUpdateEvent<List<Long>>
{
    public static final ChannelField FIELD = ChannelField.APPLIED_TAGS;
    public static final String IDENTIFIER = FIELD.getFieldName();

    public ChannelUpdateAppliedTagsEvent(@Nonnull JDA api, long responseNumber, @Nonnull ThreadChannel channel, @Nonnull List<Long> oldValue, @Nonnull List<Long> newValue)
    {
        super(api, responseNumber, channel, FIELD, oldValue, newValue);
    }

    /**
     * The newly added tags.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
     *
     * @return The tags that were added to the post
     */
    @Nonnull
    public List<ForumTag> getAddedTags()
    {
        List<ForumTag> newTags = new ArrayList<>(getNewTags());
        newTags.removeAll(getOldTags());
        return newTags;
    }

    /**
     * The removed tags.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
     *
     * @return The tags that were removed from the post
     */
    @Nonnull
    public List<ForumTag> getRemovedTags()
    {
        List<ForumTag> oldTags = new ArrayList<>(getOldTags());
        oldTags.removeAll(getNewTags());
        return oldTags;
    }

    /**
     * The new list of applied tags.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
     *
     * @return The updated list of applied tags
     */
    @Nonnull
    public List<ForumTag> getNewTags()
    {
        SortedSnowflakeCacheView<ForumTag> cache = getChannel().asThreadChannel().getParentChannel().asForumChannel().getAvailableTagCache();
        return getNewValue().stream()
                .map(cache::getElementById)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * The old list of applied tags.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
     *
     * @return The previous list of applied tags
     */
    @Nonnull
    public List<ForumTag> getOldTags()
    {
        SortedSnowflakeCacheView<ForumTag> cache = getChannel().asThreadChannel().getParentChannel().asForumChannel().getAvailableTagCache();
        return getOldValue().stream()
                .map(cache::getElementById)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public List<Long> getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public List<Long> getNewValue()
    {
        return super.getNewValue();
    }
}

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
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Indicates that the tags applied to a {@link ThreadChannel} forum post have been updated.
 */
public class ChannelUpdateAppliedTagsEvent extends GenericChannelUpdateEvent<List<ForumTag>>
{
    public ChannelUpdateAppliedTagsEvent(@Nonnull JDA api, long responseNumber, @Nonnull ThreadChannel channel, @Nonnull List<ForumTag> oldValue)
    {
        super(api, responseNumber, channel, ChannelField.APPLIED_TAGS, oldValue, channel.getAppliedTags());
    }

    /**
     * The newly added tags.
     *
     * @return The tags that were added to the post
     */
    @Nonnull
    public List<ForumTag> getAddedTags()
    {
        List<ForumTag> newTags = new ArrayList<>(getNewValue());
        newTags.removeAll(getOldValue());
        return newTags;
    }

    /**
     * The removed tags.
     *
     * @return The tags that were removed from the post
     */
    @Nonnull
    public List<ForumTag> getRemovedTags()
    {
        List<ForumTag> oldTags = new ArrayList<>(getOldValue());
        oldTags.removeAll(getNewValue());
        return oldTags;
    }

    @Nonnull
    @Override
    public List<ForumTag> getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public List<ForumTag> getNewValue()
    {
        return super.getNewValue();
    }
}

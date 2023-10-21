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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Metadata used to create a thread through a {@link WebhookMessageCreateAction webhook message}.
 *
 * @see WebhookMessageCreateAction#createThread(ThreadCreateMetadata)
 */
public class ThreadCreateMetadata
{
    private final String name;
    private final List<ForumTagSnowflake> appliedTags = new ArrayList<>(ForumChannel.MAX_POST_TAGS);

    /**
     * Create a new thread metadata instance.
     *
     * @param  name
     *         The title of the thread (1-{@value ThreadChannel#MAX_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the provided name is null or not between 1 and {@value ThreadChannel#MAX_NAME_LENGTH} characters long
     */
    public ThreadCreateMetadata(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notLonger(name, ThreadChannel.MAX_NAME_LENGTH, "Name");
        this.name = name;
    }

    /**
     * Apply the provided tags to the forum post.
     *
     * @param  tags
     *         The tags to apply
     *
     * @throws IllegalArgumentException
     *         If null or more than {@value ForumChannel#MAX_POST_TAGS} tags are provided
     *
     * @return The updated metadata instance
     */
    @Nonnull
    public ThreadCreateMetadata addTags(@Nonnull Collection<? extends ForumTagSnowflake> tags)
    {
        Checks.noneNull(tags, "Tags");
        Checks.check(tags.size() <= ForumChannel.MAX_POST_TAGS, "Cannot have more than %d post tags. Provided: %d", ForumChannel.MAX_POST_TAGS, tags.size());
        this.appliedTags.addAll(tags);
        return this;
    }

    /**
     * Apply the provided tags to the forum post.
     *
     * @param  tags
     *         The tags to apply
     *
     * @throws IllegalArgumentException
     *         If null or more than {@value ForumChannel#MAX_POST_TAGS} tags are provided
     *
     * @return The updated metadata instance
     */
    @Nonnull
    public ThreadCreateMetadata addTags(@Nonnull ForumTagSnowflake... tags)
    {
        Checks.noneNull(tags, "Tags");
        Checks.check(tags.length <= ForumChannel.MAX_POST_TAGS, "Cannot have more than %d post tags. Provided: %d", ForumChannel.MAX_POST_TAGS, tags.length);
        Collections.addAll(this.appliedTags, tags);
        return this;
    }

    /**
     * The thread name.
     *
     * @return The thread name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The applied tags for the thread / forum post.
     *
     * @return The applied tags
     */
    @Nonnull
    public List<ForumTagSnowflake> getAppliedTags()
    {
        return appliedTags;
    }
}

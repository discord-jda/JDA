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

package net.dv8tion.jda.api.events.channel.forum;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Indicates that a new {@link ForumTag} was added to a {@link ForumChannel}.
 *
 * <p>Use {@link net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent ChannelUpdateAppliedTagsEvent}
 * to detect when a tag is added to a forum post instead.
 *
 * <p><b>Requirements</b><br>
 * This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String, Collection) JDABuilder.createLight(...)} disables this by default.
 */
public class ForumTagAddEvent extends GenericForumTagEvent
{
    public ForumTagAddEvent(@Nonnull JDA api, long responseNumber, @Nonnull ForumChannel channel, @Nonnull ForumTag tag)
    {
        super(api, responseNumber, channel, tag);
    }
}

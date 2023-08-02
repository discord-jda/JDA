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

package net.dv8tion.jda.api.events.channel.forum.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Indicates that the {@link ForumTag#getEmoji() emoji} of a {@link ForumTag} changed.
 *
 * <p><b>Requirements</b><br>
 * This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String, Collection) JDABuilder.createLight(...)} disables this by default.
 *
 * <p>Identifier: {@code emoji}
 */
public class ForumTagUpdateEmojiEvent extends GenericForumTagUpdateEvent<EmojiUnion>
{
    public static final String IDENTIFIER = "emoji";

    public ForumTagUpdateEmojiEvent(@Nonnull JDA api, long responseNumber, @Nonnull IPostContainer channel, @Nonnull ForumTag tag, @Nullable EmojiUnion previous)
    {
        super(api, responseNumber, channel, tag, previous, tag.getEmoji(), IDENTIFIER);
    }

    /**
     * The old {@link EmojiUnion} for the {@link ForumTag}
     *
     * @return The old {@link EmojiUnion}
     */
    @Nullable
    public EmojiUnion getOldEmoji()
    {
        return getOldValue();
    }

    /**
     * The new {@link EmojiUnion} for the {@link ForumTag}
     *
     * @return The new {@link EmojiUnion}
     */
    @Nullable
    public EmojiUnion getNewEmoji()
    {
        return getNewValue();
    }
}

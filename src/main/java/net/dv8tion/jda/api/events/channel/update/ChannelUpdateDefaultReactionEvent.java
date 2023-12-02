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
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the {@link ForumChannel#getDefaultReaction() default reaction emoji} of a {@link IPostContainer} changed.
 *
 * <p>Can be used to retrieve the old default reaction and the new one.
 *
 * @see ChannelField#DEFAULT_REACTION_EMOJI
 */
public class ChannelUpdateDefaultReactionEvent extends GenericChannelUpdateEvent<EmojiUnion>
{
    public static final ChannelField FIELD = ChannelField.DEFAULT_REACTION_EMOJI;
    public static final String IDENTIFIER = FIELD.getFieldName();

    public ChannelUpdateDefaultReactionEvent(@Nonnull JDA api, long responseNumber, @Nonnull IPostContainer channel, @Nullable EmojiUnion oldValue, @Nullable EmojiUnion newValue)
    {
        super(api, responseNumber, channel, FIELD, oldValue, newValue);
    }
}

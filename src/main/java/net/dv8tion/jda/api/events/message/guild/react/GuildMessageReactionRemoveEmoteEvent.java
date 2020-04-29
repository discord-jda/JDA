/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.events.message.guild.react;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

import javax.annotation.Nonnull;

/**
 * Indicates that all reactions for a specific emoji/emote were removed by a moderator.
 *
 * <p>Can be used to detect which emoji/emote was removed.
 *
 * @since  4.2.0
 */
public class GuildMessageReactionRemoveEmoteEvent extends GenericGuildEvent
{
    private final TextChannel channel;
    private final MessageReaction reaction;
    private final long messageId;

    public GuildMessageReactionRemoveEmoteEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull TextChannel channel, @Nonnull MessageReaction reaction, long messageId)
    {
        super(api, responseNumber, guild);

        this.channel = channel;
        this.reaction = reaction;
        this.messageId = messageId;
    }

    /**
     * The {@link TextChannel} where the reaction happened
     *
     * @return The TextChannel
     */
    @Nonnull
    public TextChannel getChannel()
    {
        return channel;
    }

    /**
     * The {@link MessageReaction} that was removed.
     *
     * @return The removed MessageReaction
     */
    @Nonnull
    public MessageReaction getReaction()
    {
        return reaction;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote ReactionEmote}.
     * <br>Shortcut for {@code getReaction().getReactionEmote()}.
     *
     * @return The ReactionEmote
     */
    @Nonnull
    public MessageReaction.ReactionEmote getReactionEmote()
    {
        return reaction.getReactionEmote();
    }

    /**
     * The id of the affected message
     *
     * @return The id of the message
     */
    public long getMessageIdLong()
    {
        return messageId;
    }

    /**
     * The id of the affected message
     *
     * @return The id of the message
     */
    @Nonnull
    public String getMessageId()
    {
        return Long.toUnsignedString(messageId);
    }
}

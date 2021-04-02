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

package net.dv8tion.jda.api.events.message.guild.react;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;

import javax.annotation.Nonnull;

/**
 * Indicates that all reactions for a specific emoji/emote were removed by a moderator.
 *
 * <p>Can be used to detect which emoji/emote was removed.
 *
 * <h2>Requirements</h2>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MESSAGE_REACTIONS GUILD_MESSAGE_REACTIONS} intent to be enabled.
 *
 * @since  4.2.0
 */
public class GuildMessageReactionRemoveEmoteEvent extends GenericGuildMessageEvent
{
    private final MessageReaction reaction;

    public GuildMessageReactionRemoveEmoteEvent(@Nonnull JDA api, long responseNumber, @Nonnull TextChannel channel, @Nonnull MessageReaction reaction, long messageId)
    {
        super(api, responseNumber, messageId, channel);

        this.reaction = reaction;
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

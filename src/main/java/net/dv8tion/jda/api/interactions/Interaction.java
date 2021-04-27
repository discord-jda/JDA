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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.ReplyAction;
import net.dv8tion.jda.internal.requests.restaction.ReplyActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Interaction extends ISnowflake
{
    int getTypeRaw();

    @Nonnull
    default InteractionType getType()
    {
        return InteractionType.fromKey(getTypeRaw());
    }

    @Nonnull
    String getToken();

    @Nullable
    Guild getGuild();

    @Nonnull
    default ChannelType getChannelType()
    {
        AbstractChannel channel = getChannel();
        return channel != null ? channel.getType() : ChannelType.UNKNOWN;
    }

    @Nonnull
    User getUser();

    @Nullable
    Member getMember();

    @Nullable
    AbstractChannel getChannel();

    /**
     * Whether this interaction has already been acknowledged.
     * <br>Both {@link #defer()}, {@link #acknowledge()} and {@link #reply(String)} acknowledge an interaction.
     * Each interaction can only be acknowledged once.
     *
     * @return True, if this interaction has already been acknowledged
     */
    boolean isAcknowledged();

    @Nonnull
    @CheckReturnValue
    RestAction<InteractionHook> acknowledge(); // for slash commands this is just a defer()

    @Nonnull
    @CheckReturnValue
    default ReplyAction defer()
    {
        return ((ReplyActionImpl) acknowledge()).deferred();
    }

    @Nonnull
    @CheckReturnValue
    default ReplyAction defer(boolean ephemeral)
    {
        return defer().setEphemeral(ephemeral);
    }

    @Nonnull
    @CheckReturnValue
    default ReplyAction reply(@Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        ReplyActionImpl action = (ReplyActionImpl) defer();
        return action.applyMessage(message);
    }

    @Nonnull
    @CheckReturnValue
    default ReplyAction reply(@Nonnull String content)
    {
        Checks.notNull(content, "Content");
        return defer().setContent(content);
    }

    @Nonnull
    @CheckReturnValue
    default ReplyAction reply(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        Checks.notNull(embed, "MessageEmbed");
        Checks.noneNull(embeds, "MessageEmbed");
        return defer().addEmbeds(embed).addEmbeds(embeds);
    }

    @Nonnull
    @CheckReturnValue
    default ReplyAction replyFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return reply(String.format(format, args));
    }

    default GuildChannel getGuildChannel()
    {
        AbstractChannel channel = getChannel();
        if (channel instanceof GuildChannel)
            return (GuildChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to GuildChannel");
    }

    default MessageChannel getMessageChannel()
    {
        AbstractChannel channel = getChannel();
        if (channel instanceof MessageChannel)
            return (MessageChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to MessageChannel");
    }

    default TextChannel getTextChannel()
    {
        AbstractChannel channel = getChannel();
        if (channel instanceof TextChannel)
            return (TextChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to TextChannel");
    }

    default VoiceChannel getVoiceChannel()
    {
        AbstractChannel channel = getChannel();
        if (channel instanceof VoiceChannel)
            return (VoiceChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to VoiceChannel");
    }

    default PrivateChannel getPrivateChannel()
    {
        AbstractChannel channel = getChannel();
        if (channel instanceof PrivateChannel)
            return (PrivateChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to PrivateChannel");
    }
}

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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.callbacks.IAutoCompleteCallback;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.Command;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Abstract representation for any kind of Discord interaction.
 * <br>This includes things such as {@link net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction Slash-Commands}
 * or {@link net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction Buttons}.
 *
 * <p>To properly handle an interaction you must acknowledge it.
 * Each interaction has different callbacks which acknowledge the interaction. These are added by the individual {@code I...Callback} interfaces:
 * <ul>
 *     <li>{@link IReplyCallback}
 *     <br>Which supports direct message replies and deferred message replies via {@link IReplyCallback#reply(String)} and {@link IReplyCallback#deferReply()}</li>
 *     <li>{@link IMessageEditCallback}
 *     <br>Which supports direct message edits and deferred message edits (or no-operation) via {@link IMessageEditCallback#editMessage(String)} and {@link IMessageEditCallback#deferEdit()}</li>
 *     <li>{@link IAutoCompleteCallback}
 *     <br>Which supports choice suggestions for auto-complete interactions via {@link IAutoCompleteCallback#replyChoices(Command.Choice...)}</li>
 * </ul>
 *
 * <p>Once the interaction is acknowledged, you can not reply with these methods again. If the interaction is a {@link IDeferrableCallback deferrable},
 * you can use {@link IDeferrableCallback#getHook()} to send additional messages or update the original reply.
 * When using {@link IReplyCallback#deferReply()} the first message sent to the {@link InteractionHook} will be identical to using {@link InteractionHook#editOriginal(String)}.
 * You must decide whether your reply will be ephemeral or not before calling {@link IReplyCallback#deferReply()}. So design your code flow with that in mind!
 *
 * <p><b>You can only acknowledge an interaction once!</b> Any additional calls to reply/deferReply will result in exceptions.
 * You can use {@link #isAcknowledged()} to check whether the interaction has been acknowledged already.
 */
public interface Interaction extends ISnowflake
{
    /**
     * The raw interaction type.
     * <br>It is recommended to use {@link #getType()} instead.
     *
     * @return The raw interaction type
     */
    int getTypeRaw();

    /**
     * The {@link InteractionType} for this interaction.
     *
     * @return The {@link InteractionType} or {@link InteractionType#UNKNOWN}
     */
    @Nonnull
    default InteractionType getType()
    {
        return InteractionType.fromKey(getTypeRaw());
    }

    /**
     * The interaction token used for responding to an interaction.
     *
     * @return The interaction token
     */
    @Nonnull
    String getToken();

    /**
     * The {@link Guild} this interaction happened in.
     * <br>This is null in direct messages.
     *
     * @return The {@link Guild} or null
     */
    @Nullable
    Guild getGuild();

    /**
     * Whether this interaction came from a {@link Guild}.
     * <br>This is identical to {@code getGuild() != null}
     *
     * @return True, if this interaction happened in a guild
     */
    default boolean isFromGuild()
    {
        return getGuild() != null;
    }

    /**
     * The {@link ChannelType} for the channel this interaction came from.
     * <br>If {@link #getChannel()} is null, this returns {@link ChannelType#UNKNOWN}.
     *
     * @return The {@link ChannelType}
     */
    @Nonnull
    default ChannelType getChannelType()
    {
        Channel channel = getChannel();
        return channel != null ? channel.getType() : ChannelType.UNKNOWN;
    }

    /**
     * The {@link User} who caused this interaction.
     *
     * @return The {@link User}
     */
    @Nonnull
    User getUser();

    /**
     * The {@link Member} who caused this interaction.
     * <br>This is null if the interaction is not from a guild.
     *
     * @return The {@link Member}
     */
    @Nullable
    Member getMember();

    /**
     * The channel this interaction happened in.
     * <br>This is currently never null, but might be nullable in the future.
     *
     * @return The channel or null if this interaction is not from a channel context
     */
    @Nullable
    Channel getChannel();

    /**
     * Whether this interaction has already been acknowledged.
     * <br><b>Each interaction can only be acknowledged once.</b>
     *
     * @return True, if this interaction has already been acknowledged
     */
    boolean isAcknowledged();


    /**
     * The {@link GuildChannel} this interaction happened in.
     * <br>If {@link #getChannelType()} is not a guild type, this throws {@link IllegalStateException}!
     *
     * @throws IllegalStateException
     *         If {@link #getChannel()} is not a guild channel
     *
     * @return The {@link GuildChannel}
     */
    @Nonnull
    default GuildChannel getGuildChannel()
    {
        Channel channel = getChannel();
        if (channel instanceof GuildChannel)
            return (GuildChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to GuildChannel");
    }

    /**
     * The {@link MessageChannel} this interaction happened in.
     * <br>If {@link #getChannelType()} is not a message channel type, this throws {@link IllegalStateException}!
     *
     * @throws IllegalStateException
     *         If {@link #getChannel()} is not a message channel
     *
     * @return The {@link MessageChannel}
     */
    @Nonnull
    default MessageChannel getMessageChannel()
    {
        Channel channel = getChannel();
        if (channel instanceof MessageChannel)
            return (MessageChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to MessageChannel");
    }

    /**
     * The {@link TextChannel} this interaction happened in.
     * <br>If {@link #getChannelType()} is not {@link ChannelType#TEXT}, this throws {@link IllegalStateException}!
     *
     * @throws IllegalStateException
     *         If {@link #getChannel()} is not a text channel
     *
     * @return The {@link TextChannel}
     */
    @Nonnull
    default TextChannel getTextChannel()
    {
        Channel channel = getChannel();
        if (channel instanceof TextChannel)
            return (TextChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to TextChannel");
    }

    /**
     * The {@link NewsChannel} this interaction happened in.
     * <br>If {@link #getChannelType()} is not {@link ChannelType#NEWS}, this throws {@link IllegalStateException}!
     *
     * @throws IllegalStateException
     *         If {@link #getChannel()} is not a news channel
     *
     * @return The {@link NewsChannel}
     */
    @Nonnull
    default NewsChannel getNewsChannel()
    {
        Channel channel = getChannel();
        if (channel instanceof NewsChannel)
            return (NewsChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to NewsChannel");
    }

    /**
     * The {@link VoiceChannel} this interaction happened in.
     * <br>If {@link #getChannelType()} is not {@link ChannelType#VOICE}, this throws {@link IllegalStateException}!
     *
     * @throws IllegalStateException
     *         If {@link #getChannel()} is not a voice channel
     *
     * @return The {@link VoiceChannel}
     */
    @Nonnull
    default VoiceChannel getVoiceChannel()
    {
        Channel channel = getChannel();
        if (channel instanceof VoiceChannel)
            return (VoiceChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to VoiceChannel");
    }

    /**
     * The {@link PrivateChannel} this interaction happened in.
     * <br>If {@link #getChannelType()} is not {@link ChannelType#PRIVATE}, this throws {@link IllegalStateException}!
     *
     * @throws IllegalStateException
     *         If {@link #getChannel()} is not a private channel
     *
     * @return The {@link PrivateChannel}
     */
    @Nonnull
    default PrivateChannel getPrivateChannel()
    {
        Channel channel = getChannel();
        if (channel instanceof PrivateChannel)
            return (PrivateChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to PrivateChannel");
    }

    /**
     * The {@link ThreadChannel} this interaction happened in.
     * <br>If {@link #getChannelType()} is not {@link ChannelType#isThread()}, this throws {@link IllegalStateException}!
     *
     * @throws IllegalStateException
     *         If {@link #getChannel()} is not a thread channel
     *
     * @return The {@link ThreadChannel}
     */
    @Nonnull
    default ThreadChannel getThreadChannel()
    {
        Channel channel = getChannel();
        if (channel instanceof ThreadChannel)
            return (ThreadChannel) channel;
        throw new IllegalStateException("Cannot convert channel of type " + getChannelType() + " to ThreadChannel");
    }

    /**
     * Returns the selected language of the invoking user.
     *
     * @return The language of the invoking user
     */
    @Nonnull
    Locale getUserLocale();

    /**
     * Returns the preferred language of the Guild.
     * <br>This is identical to {@code getGuild().getLocale()}.
     *
     * @throws IllegalStateException
     *         If this interaction is not from a guild. (See {@link #isFromGuild()})
     *
     * @return The preferred language of the Guild
     */
    @Nonnull
    default Locale getGuildLocale()
    {
        if (!isFromGuild())
            throw new IllegalStateException("This interaction did not happen in a guild");
        return getGuild().getLocale();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this interaction
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

}

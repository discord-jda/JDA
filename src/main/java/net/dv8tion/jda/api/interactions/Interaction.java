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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.*;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract representation for any kind of Discord interaction.
 * <br>This includes things such as {@link net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction Slash-Commands},
 * {@link net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction Buttons} or {@link ModalInteraction Modals}.
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
 *     <li>{@link IModalCallback}
 *     <br>Which supports replying using a {@link Modal} via {@link IModalCallback#replyModal(Modal)}</li>
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
     * Whether this interaction has already been acknowledged.
     * <br><b>Each interaction can only be acknowledged once.</b>
     *
     * @return True, if this interaction has already been acknowledged
     */
    boolean isAcknowledged();

    /**
     * The channel this interaction happened in.
     * <br>This is currently never null, but might be nullable in the future.
     *
     * @return The channel or null if this interaction is not from a channel context
     */
    @Nullable
    Channel getChannel();

    /**
     * The {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel} this interaction happened in.
     * <br>If {@link #getChannelType()} is not a guild type, this throws {@link IllegalStateException}!
     *
     * @throws IllegalStateException
     *         If {@link #getChannel()} is not a guild channel
     *
     * @return The {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel}
     */
    @Nonnull
    default GuildChannel getGuildChannel()
    {
       return Helpers.safeChannelCast(getChannel(), GuildChannel.class);
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel} this interaction happened in.
     * <br>If {@link #getChannelType()} is not a message channel type, this throws {@link IllegalStateException}!
     *
     * @throws IllegalStateException
     *         If {@link #getChannel()} is not a message channel
     *
     * @return The {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel}
     */
    @Nonnull
    default MessageChannel getMessageChannel()
    {
        return Helpers.safeChannelCast(getChannel(), MessageChannel.class);
    }

    /**
     * Returns the selected language of the invoking user.
     *
     * @return The language of the invoking user
     */
    @Nonnull
    DiscordLocale getUserLocale();

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
    default DiscordLocale getGuildLocale()
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

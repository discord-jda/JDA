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
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract representation for any kind of Discord interaction.
 * <br>This includes things such as {@link net.dv8tion.jda.api.interactions.commands.CommandInteraction Slash-Commands} or {@link ComponentInteraction Buttons}.
 *
 * <p>To properly handle an interaction you must acknowledge it.
 *
 * <p><b>You can only acknowledge an interaction once!</b> Any additional acknowledgements will result in exceptions.
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
     * The {@link InteractionHook}.
     *
     * @return The interaction hook
     */
    @Nonnull
    InteractionHook getHook();

    /**
     * Whether this interaction has already been acknowledged.
     * <br> Each interaction can only be acknowledged once.
     *
     * @return True, if this interaction has already been acknowledged
     */
    boolean isAcknowledged();

    /**
     * Acknowledge this interaction and defer the reply to a later time.
     * <br>This will send a {@code <Bot> is thinking...} message in chat that will be updated later through either {@link InteractionHook#editOriginal(String)} or {@link InteractionHook#sendMessage(String)}.
     *
     * <p>You can use {@link #deferReply(boolean) deferReply(true)} to send a deferred ephemeral reply. If your initial deferred message is not ephemeral it cannot be made ephemeral later.
     * Your first message to the {@link InteractionHook} will inherit whether the message is ephemeral or not from this deferred reply.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>Use {@link #reply(String)} to reply directly.
     *
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    ReplyAction deferReply();

    /**
     * Acknowledge this interaction and defer the reply to a later time.
     * <br>This will send a {@code <Bot> is thinking...} message in chat that will be updated later through either {@link InteractionHook#editOriginal(String)} or {@link InteractionHook#sendMessage(String)}.
     *
     * <p>You can use {@code deferReply()} or {@code deferReply(false)} to send a non-ephemeral deferred reply. If your initial deferred message is ephemeral it cannot be made non-ephemeral later.
     * Your first message to the {@link InteractionHook} will inherit whether the message is ephemeral or not from this deferred reply.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>Use {@link #reply(String)} to reply directly.
     *
     * <p>Ephemeral messages have some limitations and will be removed once the user restarts their client.
     * <br>When a message is ephemeral, it will only be visible to the user that used the interaction.
     * <br>Limitations:
     * <ul>
     *     <li>Cannot be deleted by the bot</li>
     *     <li>Cannot contain any files/attachments</li>
     *     <li>Cannot be reacted to</li>
     *     <li>Cannot be retrieved</li>
     * </ul>
     *
     * @param  ephemeral
     *         True, if this message should only be visible to the interaction user
     *
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction deferReply(boolean ephemeral)
    {
        return deferReply().setEphemeral(ephemeral);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * @param  message
     *         The message to send
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction reply(@Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        ReplyActionImpl action = (ReplyActionImpl) deferReply();
        return action.applyMessage(message);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * @param  content
     *         The message content to send
     *
     * @throws IllegalArgumentException
     *         If null is provided or the content is empty or longer than {@link Message#MAX_CONTENT_LENGTH}
     *
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction reply(@Nonnull String content)
    {
        Checks.notNull(content, "Content");
        return deferReply().setContent(content);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * @param  embeds
     *         The {@link MessageEmbed MessageEmbeds} to send
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction replyEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return deferReply().addEmbeds(embeds);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * @param  embed
     *         The message embed to send
     * @param  embeds
     *         Any additional embeds to send
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction replyEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        Checks.notNull(embed, "MessageEmbed");
        Checks.noneNull(embeds, "MessageEmbed");
        return deferReply().addEmbeds(embed).addEmbeds(embeds);
    }

//    /**
//     * Reply to this interaction and acknowledge it.
//     * <br>This will send a reply message for this interaction.
//     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
//     * Replies are non-ephemeral by default.
//     *
//     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
//     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
//     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
//     *
//     * @param  components
//     *         The {@link ComponentLayout ComponentLayouts} to send, such as {@link ActionRow}
//     *
//     * @throws IllegalArgumentException
//     *         If null is provided
//     *
//     * @return {@link ReplyAction}
//     */
//    @Nonnull
//    @CheckReturnValue
//    default ReplyAction replyComponents(@Nonnull Collection<? extends ComponentLayout> components)
//    {
//        if (components.stream().anyMatch(it -> !(it instanceof ActionRow)))
//            throw new UnsupportedOperationException("Only ActionRow layouts are currently supported.");
//        List<ActionRow> rows = components.stream()
//                .map(ActionRow.class::cast)
//                .collect(Collectors.toList());
//        return deferReply().addActionRows(rows);
//    }
//
//    /**
//     * Reply to this interaction and acknowledge it.
//     * <br>This will send a reply message for this interaction.
//     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
//     * Replies are non-ephemeral by default.
//     *
//     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
//     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
//     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
//     *
//     * @param  components
//     *         The {@link ComponentLayout ComponentLayouts} to send, such as {@link ActionRow}
//     *
//     * @throws IllegalArgumentException
//     *         If null is provided
//     *
//     * @return {@link ReplyAction}
//     */
//    @Nonnull
//    @CheckReturnValue
//    default ReplyAction replyComponents(@Nonnull ComponentLayout component, @Nonnull ComponentLayout... components)
//    {
//        Checks.notNull(component, "ComponentLayouts");
//        Checks.noneNull(components, "ComponentLayouts");
//        List<ComponentLayout> layouts = new ArrayList<>();
//        layouts.add(component);
//        Collections.addAll(layouts, components);
//        return replyComponents(layouts);
//    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * @param  format
     *         Format string for the message content
     * @param  args
     *         Format arguments for the content
     *
     * @throws IllegalArgumentException
     *         If the format string is null or the resulting content is longer than {@link Message#MAX_CONTENT_LENGTH}
     *
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction replyFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return reply(String.format(format, args));
    }

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
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this interaction
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();
}

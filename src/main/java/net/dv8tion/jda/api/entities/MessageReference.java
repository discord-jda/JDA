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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An object representing a reference in a Discord message.
 * @see Message#getMessageReference()
 */
public class MessageReference
{
    private final long messageId;
    private final long channelId;
    private final long guildId;

    private final JDA api;
    private final MessageChannel channel;
    private final Guild guild;
    private Message referencedMessage;

    public MessageReference(long messageId, long channelId, long guildId, @Nullable Message referencedMessage, JDA api)
    {
        this.messageId = messageId;
        this.channelId = channelId;
        this.guildId = guildId;
        this.referencedMessage = referencedMessage;

        if (guildId == 0L)
            this.channel = api.getPrivateChannelById(channelId);
        else
            this.channel = (MessageChannel) api.getGuildChannelById(channelId);

        this.guild = api.getGuildById(guildId); // is null if guildId = 0 anyway

        this.api = api;
    }

    /**
     * Retrieves the referenced message for this message.
     * <br>If the message already exists, it will be returned immediately.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this reference refers to a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @throws java.lang.IllegalStateException
     *         If this message reference does not have a channel
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Message}
     */
    @Nonnull
    public RestAction<Message> resolve()
    {
        return resolve(true);
    }

    /**
     * Retrieves the referenced message for this message.
     * <br>If the message already exists, it will be returned immediately.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  update
     *         Whether to update the already stored message
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this reference refers to a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @throws java.lang.IllegalStateException
     *         If this message reference does not have a channel
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Message}
     */
    @Nonnull
    public RestAction<Message> resolve(boolean update)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_HISTORY);

        if (channel == null)
            throw new IllegalStateException("Cannot resolve a message without a channel present.");

        JDAImpl jda = (JDAImpl) getJDA();
        Message referenced = getMessage();

        if (referenced != null && !update)
            return new CompletedRestAction<>(jda, referenced);

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE.compile(getChannelId(), getMessageId());
        return new RestActionImpl<>(jda, route, (response, request) -> {
            Message created = jda.getEntityBuilder().createMessage(response.getObject(), getChannel(), false);
            this.referencedMessage = created;
            return created;
        });
    }

    /**
     * The resolved message, if available.
     *
     * <p>This will have different meaning depending on the {@link Message#getType() type} of message.
     * Usually, this is a {@link MessageType#INLINE_REPLY INLINE_REPLY} reference.
     * This can be null even if the type is {@link MessageType#INLINE_REPLY INLINE_REPLY}, when the message it references doesn't exist or discord wasn't able to resolve it in time.
     *
     * @return The referenced message, or null if this is not available
     *
     * @see    #resolve()
     */
    @Nullable
    public Message getMessage()
    {
        return referencedMessage;
    }

    /**
     * The channel from which this message originates.
     * <br>Messages from other guilds can be referenced, in which case JDA may not have the channel cached.
     *
     * @return The origin channel for this message reference, or null if this is not available
     *
     * @see    #getChannelId()
     */
    @Nullable
    public MessageChannel getChannel()
    {
        return channel;
    }


    /**
     * The guild for this reference.
     * <br>This will be null if the message did not come from a guild, the guild was not provided, or JDA did not have the guild cached
     *
     * @return The guild, or null if this is not available
     *
     * @see    #getGuildId()
     */
    @Nullable
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * Returns the message id for this reference, or 0 if no message id was provided.
     *
     * @return The message id, or 0.
     */
    public long getMessageIdLong()
    {
        return messageId;
    }

    /**
     * Returns the channel id for this reference, or 0 if no channel id was provided.
     *
     * @return The channel id, or 0.
     */
    public long getChannelIdLong()
    {
        return channelId;
    }

    /**
     * Returns the guild id for this reference, or 0 if no guild id was provided.
     *
     * @return The guild id, or 0.
     */
    public long getGuildIdLong()
    {
        return guildId;
    }

    /**
     * Returns the message id for this reference, or 0 if no message id was provided.
     *
     * @return The message id, or 0.
     */
    @Nonnull
    public String getMessageId()
    {
        return Long.toUnsignedString(getMessageIdLong());
    }

    /**
     * Returns the channel id for this reference, or 0 if no channel id was provided.
     *
     * @return The channel id, or 0.
     */
    @Nonnull
    public String getChannelId()
    {
        return Long.toUnsignedString(getChannelIdLong());
    }

    /**
     * Returns the guild id for this reference, or 0 if no guild id was provided.
     *
     * @return The guild id, or 0.
     */
    @Nonnull
    public String getGuildId()
    {
        return Long.toUnsignedString(getGuildIdLong());
    }

    /**
     * Returns the JDA instance related to this message reference.
     *
     * @return The corresponding JDA instance
     */
    @Nonnull
    public JDA getJDA()
    {
        return api;
    }

    private void checkPermission(Permission permission)
    {
        if (guild == null || !(channel instanceof IPermissionContainer)) return;

        Member selfMember = guild.getSelfMember();


        IPermissionContainer permChannel = (IPermissionContainer) channel;

        if (!selfMember.hasAccess(permChannel))
            throw new MissingAccessException(permChannel, Permission.VIEW_CHANNEL);
        if (!selfMember.hasPermission(permChannel, permission))
            throw new InsufficientPermissionException(permChannel, permission);
    }
}

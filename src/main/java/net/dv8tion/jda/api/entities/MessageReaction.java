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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.ReactionPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * An object representing a single MessageReaction from Discord.
 * This is an immutable object and is not updated by method calls or changes in Discord. A new snapshot instance
 * built from Discord is needed to see changes.
 *
 * @see    Message#getReactions()
 * @see    Message#getReaction(Emoji)
 */
public class MessageReaction
{
    private final MessageChannel channel;
    private final EmojiUnion emoji;
    private final long messageId;
    private final boolean self;
    private final int count;

    /**
     * Creates a new MessageReaction instance
     *
     * @param  channel
     *         The {@link MessageChannel} this Reaction was used in
     * @param  emoji
     *         The {@link Emoji} that was used
     * @param  messageId
     *         The message id this reaction is attached to
     * @param  self
     *         Whether we already reacted with this Reaction
     * @param  count
     *         The amount of people that reacted with this Reaction
     */
    public MessageReaction(@Nonnull MessageChannel channel, @Nonnull EmojiUnion emoji, long messageId, boolean self, int count)
    {
        this.channel = channel;
        this.emoji = emoji;
        this.messageId = messageId;
        this.self = self;
        this.count = count;
    }

    /**
     * The JDA instance of this Reaction
     *
     * @return The JDA instance of this Reaction
     */
    @Nonnull
    public JDA getJDA()
    {
        return channel.getJDA();
    }

    /**
     * Whether the currently logged in account has reacted with this reaction
     *
     * <p><b>This will always be false for events. Discord does not provide this information for reaction events.</b>
     * You can use {@link MessageChannel#retrieveMessageById(String)} to get this information on a complete message.
     *
     * @return True, if we reacted with this reaction
     */
    public boolean isSelf()
    {
        return self;
    }

    /**
     * Whether this reaction can provide a count via {@link #getCount()}.
     * <br>This is usually not provided for reactions coming from {@link net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent}
     * or similar.
     *
     * @return True, if a count is available
     *
     * @see    #getCount()
     */
    public boolean hasCount()
    {
        return count >= 0;
    }

    /**
     * The amount of users that already reacted with this Reaction
     * <br><b>This is not updated, it is a {@code final int} per Reaction instance</b>
     *
     * <p>This value is not available in events such as {@link net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent MessageReactionAddEvent}
     * and {@link net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent MessageReactionRemoveEvent} in which case an
     * {@link java.lang.IllegalStateException IllegalStateException} is thrown!
     *
     * @throws java.lang.IllegalStateException
     *         If this MessageReaction is from an event which does not provide a count
     *
     * @return The amount of users that reacted with this Reaction
     */
    public int getCount()
    {
        if (!hasCount())
            throw new IllegalStateException("Cannot retrieve count for this MessageReaction!");
        return count;
    }

    /**
     * The {@link ChannelType ChannelType}
     * this Reaction was used in.
     *
     * @return The ChannelType
     */
    @Nonnull
    public ChannelType getChannelType()
    {
        return channel.getType();
    }

    /**
     * Whether this Reaction was used in a {@link MessageChannel}
     * of the specified {@link ChannelType ChannelType}.
     *
     * @param  type
     *         The ChannelType to compare
     *
     * @return True, if this Reaction was used in a MessageChannel from the specified ChannelType
     */
    public boolean isFromType(@Nonnull ChannelType type)
    {
        return getChannelType() == type;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} this Reaction was used in.
     *
     * @throws IllegalStateException
     *         If {@link #getChannel()} is not a guild channel
     *
     * @return {@link net.dv8tion.jda.api.entities.Guild Guild} this Reaction was used in
     */
    @Nonnull
    public Guild getGuild()
    {
        return getGuildChannel().getGuild();
    }

    /**
     * The {@link MessageChannel MessageChannel}
     * this Reaction was used in.
     *
     * @return The channel this Reaction was used in
     */
    @Nonnull
    public MessageChannelUnion getChannel()
    {
        return (MessageChannelUnion) channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel channel} this Reaction was used in.
     *
     * @throws IllegalStateException
     *          If {@link #getChannel()} is not a guild channel
     *
     * @return The guild channel this Reaction was used in
     */
    @Nonnull
    public GuildMessageChannelUnion getGuildChannel()
    {
        return (GuildMessageChannelUnion) getChannel().asGuildMessageChannel();
    }

    /**
     * The {@link Emoji} of this Reaction.
     * <br>This includes both {@link Emoji.Type#CUSTOM custom emojis} and {@link Emoji.Type#UNICODE unicode emoji}.
     *
     * @return The final instance of this Reaction's Emoji
     */
    @Nonnull
    public EmojiUnion getEmoji()
    {
        return emoji;
    }

    /**
     * The message id this reaction is attached to
     *
     * @return The message id this reaction is attached to
     */
    @Nonnull
    public String getMessageId()
    {
        return Long.toUnsignedString(messageId);
    }

    /**
     * The message id this reaction is attached to
     *
     * @return The message id this reaction is attached to
     */
    public long getMessageIdLong()
    {
        return messageId;
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.api.entities.User Users} that
     * already reacted with this MessageReaction.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>If the message this reaction was attached to got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel this reaction was used in got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the channel/guild</li>
     * </ul>
     *
     * @return {@link ReactionPaginationAction ReactionPaginationAction}
     */
    @Nonnull
    @CheckReturnValue
    public ReactionPaginationAction retrieveUsers()
    {
        return new ReactionPaginationActionImpl(this);
    }

    /**
     * Removes this Reaction from the Message.
     * <br>This will remove our own reaction as an overload
     * of {@link #removeReaction(User)}.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>If the message this reaction was attached to got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel this reaction was used in got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the channel/guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Void
     *         Nothing is returned on success
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Void> removeReaction()
    {
        return removeReaction(getJDA().getSelfUser());
    }

    /**
     * Removes this Reaction from the Message.
     * <br>This will remove the reaction of the {@link net.dv8tion.jda.api.entities.User User}
     * provided.
     *
     * <p>If the provided User did not react with this Reaction this does nothing.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>If the message this reaction was attached to got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel this reaction was used in got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the channel/guild</li>
     * </ul>
     *
     * @param  user
     *         The User of which to remove the reaction
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code user} is null.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the provided User is not us and we do not have permission to
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE manage messages}
     *         in the channel this reaction was used in
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If the message is from another user in a {@link PrivateChannel PrivateChannel}
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     *         Nothing is returned on success
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Void> removeReaction(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        boolean self = user.equals(getJDA().getSelfUser());
        if (!self)
        {
            if (!channel.getType().isGuild())
                throw new PermissionException("Unable to remove Reaction of other user in non-guild channels!");

            GuildChannel guildChannel = (GuildChannel) channel;
            if (!guildChannel.getGuild().getSelfMember().hasPermission(guildChannel, Permission.MESSAGE_MANAGE))
                throw new InsufficientPermissionException(guildChannel, Permission.MESSAGE_MANAGE);
        }

        String code = emoji.getAsReactionCode();
        String target = self ? "@me" : user.getId();
        Route.CompiledRoute route = Route.Messages.REMOVE_REACTION.compile(channel.getId(), getMessageId(), code, target);
        return new RestActionImpl<>(getJDA(), route);
    }

    /**
     * Removes this entire reaction from the message.
     * <br>Unlike {@link #removeReaction(User)}, which removes the reaction of a single user, this will remove the reaction
     * completely.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account lost access to the channel by either being removed from the guild
     *         or losing the {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided unicode emoji doesn't exist. Try using one of the example formats.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message was deleted.</li>
     * </ul>
     *
     * @throws UnsupportedOperationException
     *         If this reaction happened in a private channel
     * @throws InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MESSAGE_MANAGE} in the channel
     *
     * @return {@link RestAction}
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Void> clearReactions()
    {
        // Requires permission, only works in guilds
        if (!getChannelType().isGuild())
            throw new UnsupportedOperationException("Cannot clear reactions on a message sent from a private channel");
        GuildMessageChannel guildChannel = Objects.requireNonNull(getGuildChannel());
        return guildChannel.clearReactionsById(getMessageId(), emoji);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof MessageReaction))
            return false;
        MessageReaction r = (MessageReaction) obj;
        return r.emoji.equals(emoji)
            && r.self == self
            && r.messageId == messageId;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("messageId", messageId)
                .addMetadata("emoji", emoji)
                .toString();
    }
}

/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.core.utils.MiscUtil;

import javax.annotation.CheckReturnValue;
import java.util.Objects;

/**
 * An object representing a single MessageReaction from Discord.
 * This is an immutable object and is not updated by method calls or changes in Discord. A new snapshot instance
 * built from Discord is needed to see changes.
 *
 * @since  3.0
 */
public class MessageReaction
{

    private final MessageChannel channel;
    private final ReactionEmote emote;
    private final long messageId;
    private final boolean self;
    private final int count;

    /**
     * Creates a new MessageReaction instance
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.core.entities.MessageChannel} this Reaction was used in
     * @param  emote
     *         The {@link net.dv8tion.jda.core.entities.MessageReaction.ReactionEmote ReactionEmote} that was used
     * @param  messageId
     *         The message id this reaction is attached to
     * @param  self
     *         Whether we already reacted with this Reaction
     * @param  count
     *         The amount of people that reacted with this Reaction
     */
    public MessageReaction(MessageChannel channel, ReactionEmote emote, long messageId, boolean self, int count)
    {
        this.channel = channel;
        this.emote = emote;
        this.messageId = messageId;
        this.self = self;
        this.count = count;
    }

    /**
     * The JDA instance of this Reaction
     *
     * @return The JDA instance of this Reaction
     */
    public JDA getJDA()
    {
        return channel.getJDA();
    }

    /**
     * Whether the currently logged in account has reacted with this reaction
     *
     * @return True, if we reacted with this reaction
     */
    public boolean isSelf()
    {
        return self;
    }

    /**
     * The amount of users that already reacted with this Reaction
     * <br><b>This is not updated, it is a {@code final int} per Reaction instance</b>
     *
     * <p>This value is not available in events such as {@link net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent MessageReactionAddEvent}
     * and {@link net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent MessageReactionRemoveEvent} in which case an
     * {@link java.lang.IllegalStateException IllegalStateException} is thrown!
     *
     * @throws java.lang.IllegalStateException
     *         If this MessageReaction is from an event which does not provide a count
     *
     * @return The amount of users that reacted with this Reaction
     */
    public int getCount()
    {
        if (count < 0)
            throw new IllegalStateException("Cannot retrieve count for this MessageReaction!");
        return count;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     * this Reaction was used in.
     *
     * @return The ChannelType
     */
    public ChannelType getChannelType()
    {
        return channel.getType();
    }

    /**
     * Whether this Reaction was used in a {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * of the specified {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}.
     *
     * @param  type
     *         The ChannelType to compare
     *
     * @return True, if this Reaction was used in a MessageChannel from the specified ChannelType
     */
    public boolean isFromType(ChannelType type)
    {
        return getChannelType() == type;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this Reaction was used in,
     * this might return {@code null} when this Reaction was not used in a MessageChannel
     * from the ChannelType {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT}!
     *
     * @return {@link net.dv8tion.jda.core.entities.Guild Guild} this Reaction was used in, or {@code null}
     */
    public Guild getGuild()
    {
        TextChannel channel = getTextChannel();
        return channel != null ? channel.getGuild() : null;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} this Reaction was used in
     * or {@code null} if this is not from type {@link net.dv8tion.jda.core.entities.ChannelType#TEXT ChannelType.TEXT}!
     *
     * @return The {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} or {@code null}
     */
    public TextChannel getTextChannel()
    {
        return getChannel() instanceof TextChannel ? (TextChannel) getChannel() : null;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} this Reaction was used in
     * or {@code null} if this is not from type {@link net.dv8tion.jda.core.entities.ChannelType#PRIVATE ChannelType.PRIVATE}!
     *
     * @return The {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} or {@code null}
     */
    public PrivateChannel getPrivateChannel()
    {
        return getChannel() instanceof PrivateChannel ? (PrivateChannel) getChannel() : null;
    }

    /**
     * The {@link net.dv8tion.jda.client.entities.Group Group} this Reaction was used in
     * or {@code null} if this is not from type {@link net.dv8tion.jda.core.entities.ChannelType#GROUP ChannelType.GROUP}!
     *
     * @return The {@link net.dv8tion.jda.client.entities.Group Group} or {@code null}
     */
    public Group getGroup()
    {
        return getChannel() instanceof Group ? (Group) getChannel() : null;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * this Reaction was used in.
     *
     * @return The channel this Reaction was used in
     */
    public MessageChannel getChannel()
    {
        return channel;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.MessageReaction.ReactionEmote ReactionEmote}
     * of this Reaction
     *
     * @return The final instance of this Reaction's Emote/Emoji
     */
    public ReactionEmote getReactionEmote()
    {
        return emote;
    }

    /**
     * The message id this reaction is attached to
     *
     * @return The message id this reaction is attached to
     */
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
     * Retrieves the {@link net.dv8tion.jda.core.entities.User Users} that
     * already reacted with this MessageReaction.
     * <br>This is an overload of {@link #getUsers(int)} with {@code 100}.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>If the message this reaction was attached to got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel this reaction was used in got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the channel/guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.pagination.ReactionPaginationAction ReactionPaginationAction}
     *         <br>Retrieves an immutable list of users that reacted with this Reaction.
     */
    @CheckReturnValue
    public ReactionPaginationAction getUsers()
    {
        return getUsers(100);
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.core.entities.User Users} that
     * already reacted with this MessageReaction. The maximum amount of users
     * that can be retrieved is 100.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>If the message this reaction was attached to got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel this reaction was used in got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the channel/guild</li>
     * </ul>
     *
     * @param  amount
     *         the amount of users to retrieve
     *
     * @throws IllegalArgumentException
     *         if the provided amount is not between 1-100
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.pagination.ReactionPaginationAction ReactionPaginationAction}
     *         <br>Retrieves an immutable list of users that reacted with this Reaction.
     */
    @CheckReturnValue
    public ReactionPaginationAction getUsers(int amount)
    {
        return new ReactionPaginationAction(this).limit(amount);
    }

    /**
     * Removes this Reaction from the Message.
     * <br>This will remove our own reaction as an overload
     * of {@link #removeReaction(User)}.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>If the message this reaction was attached to got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel this reaction was used in got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the channel/guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     *         Nothing is returned on success
     */
    @CheckReturnValue
    public RestAction<Void> removeReaction()
    {
        return removeReaction(getJDA().getSelfUser());
    }

    /**
     * Removes this Reaction from the Message.
     * <br>This will remove the reaction of the {@link net.dv8tion.jda.core.entities.User User}
     * provided.
     *
     * <p>If the provided User did not react with this Reaction this does nothing.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>If the message this reaction was attached to got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel this reaction was used in got deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the channel/guild</li>
     * </ul>
     *
     * @param  user
     *         The User of which to remove the reaction
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code user} is null.
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if the provided User is not us and we do not have permission to
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE manage messages}
     *         in the channel this reaction was used in
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     *         Nothing is returned on success
     */
    @CheckReturnValue
    public RestAction<Void> removeReaction(User user)
    {
        if (user == null)
            throw new IllegalArgumentException("Provided User was null!");
        if (!user.equals(getJDA().getSelfUser()))
        {
            if (channel.getType() == ChannelType.TEXT)
            {
                Channel channel = (Channel) this.channel;
                if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
                    throw new InsufficientPermissionException(Permission.MESSAGE_MANAGE);
            }
            else
            {
                throw new PermissionException("Unable to remove Reaction of other user in non-text channel!");
            }
        }

        String code = emote.isEmote()
                    ? emote.getName() + ":" + emote.getId()
                    : MiscUtil.encodeUTF8(emote.getName());
        Route.CompiledRoute route;
        if (user.equals(getJDA().getSelfUser()))
            route = Route.Messages.REMOVE_OWN_REACTION.compile(channel.getId(), getMessageId(), code);
        else
            route = Route.Messages.REMOVE_REACTION.compile(channel.getId(), getMessageId(), code, user.getId());
        return new RestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof MessageReaction))
            return false;
        MessageReaction r = (MessageReaction) obj;
        return r.emote.equals(emote)
            && r.self == self
            && r.messageId == messageId;
    }

    @Override
    public String toString()
    {
        return "MR:(M:(" + messageId + ") / " + emote + ")";
    }

    /**
     * Represents an Emoji/Emote of a MessageReaction
     * <br>This is used to wrap both emojis and emotes
     */
    public static class ReactionEmote implements ISnowflake
    {

        private final JDA api;
        private final String name;
        private final Long id;
        private Emote emote = null;

        public ReactionEmote(String name, Long id, JDA api)
        {
            this.name = name;
            this.id = id;
            this.api = api;
        }

        public ReactionEmote(Emote emote)
        {
            this(emote.getName(), emote.getIdLong(), emote.getJDA());
            this.emote = emote;
        }

        /**
         * Whether this is an {@link net.dv8tion.jda.core.entities.Emote Emote}
         * wrapper.
         *
         * @return True, if {@link #getId()} is not null
         */
        public boolean isEmote()
        {
            return emote != null;
        }

        @Override
        public String getId()
        {
            return id != null ? String.valueOf(id) : null;
        }

        @Override
        public long getIdLong()
        {
            if (id == null)
                throw new IllegalStateException("No id available");
            return id;
        }

        /**
         * The name for this emote/emoji
         * <br>For unicode emojis this will be the unicode of said emoji.
         *
         * @return The name for this emote/emoji
         */
        public String getName()
        {
            return name;
        }

        /**
         * The instance of {@link net.dv8tion.jda.core.entities.Emote Emote}
         * for the Reaction instance.
         * <br>Might be null if {@link #getId()} returns null.
         *
         * @return The possibly-null Emote for the Reaction instance
         */
        public Emote getEmote()
        {
            return emote;
        }

        /**
         * The current JDA instance for the Reaction
         *
         * @return The JDA instance of the Reaction
         */
        public JDA getJDA()
        {
            return api;
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof ReactionEmote
                    && Objects.equals(((ReactionEmote) obj).id, id)
                    && ((ReactionEmote) obj).getName().equals(name);
        }

        @Override
        public String toString()
        {
            return "RE:" + (isEmote() ? getEmote() : getName() + "(" + id + ")");
        }
    }

}

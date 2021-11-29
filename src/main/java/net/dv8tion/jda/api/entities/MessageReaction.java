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
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.pagination.ReactionPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * An object representing a single MessageReaction from Discord.
 * This is an immutable object and is not updated by method calls or changes in Discord. A new snapshot instance
 * built from Discord is needed to see changes.
 *
 * @since  3.0
 *
 * @see    Message#getReactions()
 * @see    Message#getReactionByUnicode(String)
 * @see    Message#getReactionById(long)
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
     *         The {@link net.dv8tion.jda.api.entities.MessageChannel} this Reaction was used in
     * @param  emote
     *         The {@link net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote ReactionEmote} that was used
     * @param  messageId
     *         The message id this reaction is attached to
     * @param  self
     *         Whether we already reacted with this Reaction
     * @param  count
     *         The amount of people that reacted with this Reaction
     */
    public MessageReaction(@Nonnull MessageChannel channel, @Nonnull ReactionEmote emote, long messageId, boolean self, int count)
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
     * The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}
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
     * Whether this Reaction was used in a {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}
     * of the specified {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}.
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
     * This will return null if the channel this Reaction was used in is not part of a Guild.
     *
     * @return {@link net.dv8tion.jda.api.entities.Guild Guild} this Reaction was used in, or {@code null}
     */
    @Nullable
    public Guild getGuild()
    {
        GuildMessageChannel channel = getGuildChannel();
        return channel != null ? channel.getGuild() : null;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} this Reaction was used in
     * or {@code null} if this is not from type {@link net.dv8tion.jda.api.entities.ChannelType#TEXT ChannelType.TEXT}!
     *
     * @return The {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} or {@code null}
     */
    @Nullable
    public TextChannel getTextChannel()
    {
        return getChannel() instanceof TextChannel ? (TextChannel) getChannel() : null;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} this Reaction was used in
     * or {@code null} if this is not from type {@link net.dv8tion.jda.api.entities.ChannelType#PRIVATE ChannelType.PRIVATE}!
     *
     * @return The {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} or {@code null}
     */
    @Nullable
    public PrivateChannel getPrivateChannel()
    {
        return getChannel() instanceof PrivateChannel ? (PrivateChannel) getChannel() : null;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}
     * this Reaction was used in.
     *
     * @return The channel this Reaction was used in
     */
    @Nonnull
    public MessageChannel getChannel()
    {
        return channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.GuildMessageChannel GuildMessageChannel}
     * this Reaction was used in.
     *
     * @return The channel this Reaction was used in or null if it wasn't used in a Guild
     */
    @Nullable
    public GuildMessageChannel getGuildChannel()
    {
        return getChannel() instanceof GuildMessageChannel ? (GuildMessageChannel) getChannel() : null;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote ReactionEmote}
     * of this Reaction
     *
     * @return The final instance of this Reaction's Emote/Emoji
     */
    @Nonnull
    public ReactionEmote getReactionEmote()
    {
        return emote;
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
     *         If the message is from another user in a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
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
            if (!channel.getType().isGuild()) {
                throw new PermissionException("Unable to remove Reaction of other user in non-guild channels!");
            }

            IPermissionContainer permChannel = (IPermissionContainer) this.channel;
            if (!permChannel.getGuild().getSelfMember().hasPermission(permChannel, Permission.MESSAGE_MANAGE))
                throw new InsufficientPermissionException(permChannel, Permission.MESSAGE_MANAGE);
        }

        String code = getReactionCode();
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
        return guildChannel.clearReactionsById(getMessageId(), getReactionCode());
    }

    private String getReactionCode()
    {
        return emote.isEmote()
                ? emote.getName() + ":" + emote.getId()
                : EncodingUtil.encodeUTF8(emote.getName());
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
        private final long id;
        private final Emote emote;

        private ReactionEmote(@Nonnull String name, @Nonnull JDA api)
        {
            this.name = name;
            this.api = api;
            this.id = 0;
            this.emote = null;
        }

        private ReactionEmote(@Nonnull Emote emote)
        {
            this.api = emote.getJDA();
            this.name = emote.getName();
            this.id = emote.getIdLong();
            this.emote = emote;
        }

        @Nonnull
        public static ReactionEmote fromUnicode(@Nonnull String name, @Nonnull JDA api)
        {
            return new ReactionEmote(name, api);
        }

        @Nonnull
        public static ReactionEmote fromCustom(@Nonnull Emote emote)
        {
            return new ReactionEmote(emote);
        }

        /**
         * Whether this is an {@link net.dv8tion.jda.api.entities.Emote Emote} wrapper.
         * <br>This means {@link #getEmoji()} will throw {@link java.lang.IllegalStateException}.
         *
         * @return True, if {@link #getEmote()} can be used
         *
         * @see    #getEmote()
         */
        public boolean isEmote()
        {
            return emote != null;
        }

        /**
         * Whether this represents a unicode emoji.
         * <br>This means {@link #getEmote()}, {@link #getId()}, and {@link #getIdLong()} will not be available.
         *
         * @return True, if this represents a unicode emoji
         *
         * @see    #getEmoji()
         */
        public boolean isEmoji()
        {
            return emote == null;
        }

        /**
         * The name for this emote/emoji
         * <br>For unicode emojis this will be the unicode of said emoji rather than an alias like {@code :smiley:}.
         *
         * <p>For better use in consoles that do not support unicode emoji use {@link #getAsCodepoints()} for a more
         * readable representation of the emoji.
         *
         * <p>Custom emotes may return an empty string for this if the emote was deleted.
         *
         * @return The name for this emote/emoji
         */
        @Nonnull
        public String getName()
        {
            return name;
        }

        /**
         * Converts the unicode name into codepoint notation like {@code U+1F602}.
         *
         * @throws java.lang.IllegalStateException
         *         If this is not an emoji reaction, see {@link #isEmoji()}
         *
         * @return String containing the codepoint representation of the reaction emoji
         */
        @Nonnull
        public String getAsCodepoints()
        {
            if (!isEmoji())
                throw new IllegalStateException("Cannot get codepoint for custom emote reaction");
            return EncodingUtil.encodeCodepoints(name);
        }

        @Override
        public long getIdLong()
        {
            if (!isEmote())
                throw new IllegalStateException("Cannot get id for emoji reaction");
            return id;
        }
        
        /**
         * The code for this Reaction.
         * <br>For unicode emojis this will be the unicode of said emoji rather than an alias like {@code :smiley:}.
         * <br>For custom emotes this will be the name and id of said emote in the format {@code <name>:<id>}.
         *
         * @return The unicode if it is an emoji, or the name and id in the format {@code <name>:<id>}
         */
        @Nonnull
        public String getAsReactionCode()
        {
            return emote != null
                    ? name + ":" + id
                    : name;
        }

        /**
         * The unicode representing the emoji used for reacting.
         *
         * @throws java.lang.IllegalStateException
         *         If this is not an emoji reaction, see {@link #isEmoji()}
         *
         * @return The unicode for the emoji
         */
        @Nonnull
        public String getEmoji()
        {
            if (!isEmoji())
                throw new IllegalStateException("Cannot get emoji code for custom emote reaction");
            return getName();
        }

        /**
         * The instance of {@link net.dv8tion.jda.api.entities.Emote Emote}
         * for the Reaction instance.
         *
         * @throws java.lang.IllegalStateException
         *         If this is not a custom emote reaction, see {@link #isEmote()}
         *
         * @return The Emote for the Reaction instance
         */
        @Nonnull
        public Emote getEmote()
        {
            if (!isEmote())
                throw new IllegalStateException("Cannot get custom emote for emoji reaction");
            return emote;
        }

        /**
         * The current JDA instance for the Reaction
         *
         * @return The JDA instance of the Reaction
         */
        @Nonnull
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
            if (isEmoji())
                return "RE:" + getAsCodepoints();
            return "RE:" + getName() + "(" + getId() + ")";
        }
    }
}

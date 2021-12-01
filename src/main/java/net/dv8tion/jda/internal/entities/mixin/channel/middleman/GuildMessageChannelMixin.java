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

package net.dv8tion.jda.internal.entities.mixin.channel.middleman;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;

public interface GuildMessageChannelMixin<T extends GuildMessageChannelMixin<T>> extends GuildMessageChannel, GuildChannelMixin<T>, MessageChannelMixin<T>
{
    // ---- Default implementations of interface ----
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> deleteMessagesByIds(@Nonnull Collection<String> messageIds)
    {
        checkPermission(Permission.MESSAGE_MANAGE, "Must have MESSAGE_MANAGE in order to bulk delete messages in this channel regardless of author.");

        if (messageIds.size() < 2 || messageIds.size() > 100)
            throw new IllegalArgumentException("Must provide at least 2 or at most 100 messages to be deleted.");

        long twoWeeksAgo = TimeUtil.getDiscordTimestamp((System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)));
        for (String id : messageIds)
            Checks.check(MiscUtil.parseSnowflake(id) > twoWeeksAgo, "Message Id provided was older than 2 weeks. Id: " + id);

        return bulkDeleteMessages(messageIds);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(@Nonnull String messageId, @Nonnull String unicode, @Nonnull User user)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(unicode, "Provided Unicode");
        unicode = unicode.trim();
        Checks.notEmpty(unicode, "Provided Unicode");
        Checks.notNull(user, "User");

        if (!getJDA().getSelfUser().equals(user))
            checkPermission(Permission.MESSAGE_MANAGE);

        final String encoded = EncodingUtil.encodeReaction(unicode);

        String targetUser;
        if (user.equals(getJDA().getSelfUser()))
            targetUser = "@me";
        else
            targetUser = user.getId();

        final Route.CompiledRoute route = Route.Messages.REMOVE_REACTION.compile(getId(), messageId, encoded, targetUser);
        return new RestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    default RestAction<Void> clearReactionsById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");

        checkPermission(Permission.MESSAGE_MANAGE);

        final Route.CompiledRoute route = Route.Messages.REMOVE_ALL_REACTIONS.compile(getId(), messageId);
        return new RestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    default RestAction<Void> clearReactionsById(@Nonnull String messageId, @Nonnull String unicode)
    {
        Checks.notNull(messageId, "Message ID");
        Checks.notNull(unicode, "Emote Name");

        checkPermission(Permission.MESSAGE_MANAGE);

        String code = EncodingUtil.encodeReaction(unicode);
        Route.CompiledRoute route = Route.Messages.CLEAR_EMOTE_REACTIONS.compile(getId(), messageId, code);
        return new RestActionImpl<>(getJDA(), route);
    }
    
    // ---- Default implementation of parent mixins hooks ----
    default void checkCanAccessChannel()
    {
        checkPermission(Permission.VIEW_CHANNEL);
    }

    default void checkCanSendMessage()
    {
        if (getType().isThread())
            checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        else
            checkPermission(Permission.MESSAGE_SEND);
    }

    default void checkCanSendMessageEmbeds()
    {
        checkPermission(Permission.MESSAGE_EMBED_LINKS);
    }

    default void checkCanSendFiles()
    {
        checkPermission(Permission.MESSAGE_ATTACH_FILES);
    }

    default void checkCanViewHistory()
    {
        checkPermission(Permission.MESSAGE_HISTORY);
    }

    default void checkCanAddReactions()
    {
        checkPermission(Permission.MESSAGE_ADD_REACTION);
        checkPermission(Permission.MESSAGE_HISTORY, "You need MESSAGE_HISTORY to add reactions to a message");
    }

    default void checkCanRemoveReactions()
    {
        checkPermission(Permission.MESSAGE_HISTORY, "You need MESSAGE_HISTORY to remove reactions from a message");
    }

    default void checkCanControlMessagePins()
    {
        checkPermission(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");
    }

    default boolean canDeleteOtherUsersMessages()
    {
        return hasPermission(Permission.MESSAGE_MANAGE);
    }
}

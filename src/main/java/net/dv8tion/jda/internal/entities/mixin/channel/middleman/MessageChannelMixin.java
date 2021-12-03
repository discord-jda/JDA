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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.components.ComponentLayout;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface MessageChannelMixin<T extends MessageChannelMixin<T>> extends MessageChannel
{
    // ---- Default implementations of interface ----
    @Nonnull
    default List<CompletableFuture<Void>> purgeMessages(@Nonnull List<? extends Message> messages)
    {
        if (messages == null || messages.isEmpty())
            return Collections.emptyList();

        if (!canDeleteOtherUsersMessages())
        {
            for (Message m : messages)
            {
                if (m.getAuthor().equals(getJDA().getSelfUser()))
                    continue;

                if (getType() == ChannelType.PRIVATE)
                    throw new IllegalStateException("Cannot delete messages of other users in a private channel");
                else
                    throw new InsufficientPermissionException((GuildChannel) this, Permission.MESSAGE_MANAGE, "Cannot delete messages of other users");
            }
        }

        return MessageChannel.super.purgeMessages(messages);
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@Nonnull long... messageIds)
    {
        if (messageIds == null || messageIds.length == 0)
            return Collections.emptyList();

        //If we can't use the bulk delete system, then use the standard purge defined in MessageChannel
        if (!canDeleteOtherUsersMessages())
            return MessageChannel.super.purgeMessagesById(messageIds);

        // remove duplicates and sort messages
        List<CompletableFuture<Void>> list = new LinkedList<>();
        TreeSet<Long> bulk = new TreeSet<>(Comparator.reverseOrder());
        TreeSet<Long> norm = new TreeSet<>(Comparator.reverseOrder());
        long twoWeeksAgo = TimeUtil.getDiscordTimestamp(System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000) + 10000);
        for (long messageId : messageIds)
        {
            if (messageId > twoWeeksAgo) //Bulk delete cannot delete messages older than 2 weeks.
                bulk.add(messageId);
            else
                norm.add(messageId);
        }

        // delete chunks of 100 messages each
        if (!bulk.isEmpty())
        {
            List<String> toDelete = new ArrayList<>(100);
            while (!bulk.isEmpty())
            {
                toDelete.clear();
                for (int i = 0; i < 100 && !bulk.isEmpty(); i++)
                    toDelete.add(Long.toUnsignedString(bulk.pollLast()));

                //If we only had 1 in the bulk collection then use the standard deleteMessageById request
                // as you cannot bulk delete a single message
                if (toDelete.size() == 1)
                    list.add(deleteMessageById(toDelete.get(0)).submit());
                else if (!toDelete.isEmpty())
                    list.add(bulkDeleteMessages(toDelete).submit());
            }
        }

        // delete messages too old for bulk delete
        if (!norm.isEmpty())
        {
            for (long message : norm)
                list.add(deleteMessageById(message).submit());
        }
        return list;
    }

    @Nonnull
    @CheckReturnValue
    default MessageAction sendMessage(@Nonnull CharSequence text)
    {
        checkCanAccessChannel();
        checkCanSendMessage();
        return MessageChannel.super.sendMessage(text);
    }

    @Nonnull
    @CheckReturnValue
    default MessageAction sendMessageEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other)
    {
        checkCanAccessChannel();
        checkCanSendMessage();
        checkCanSendMessageEmbeds();
        return MessageChannel.super.sendMessageEmbeds(embed, other);
    }

    @Nonnull
    @CheckReturnValue
    default MessageAction sendMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        checkCanAccessChannel();
        checkCanSendMessage();
        checkCanSendMessageEmbeds();
        return MessageChannel.super.sendMessageEmbeds(embeds);
    }

    @Nonnull
    @CheckReturnValue
    default MessageAction sendMessage(@Nonnull Message msg)
    {
        checkCanAccessChannel();
        checkCanSendMessage();
        return MessageChannel.super.sendMessage(msg);
    }

    @Nonnull
    @CheckReturnValue
    default MessageAction sendFile(@Nonnull InputStream data, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        checkCanAccessChannel();
        checkCanSendMessage();
        checkCanSendFiles();
        return MessageChannel.super.sendFile(data, fileName, options);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Message> retrieveMessageById(@Nonnull String messageId)
    {
        checkCanAccessChannel();
        checkCanViewHistory();
        return MessageChannel.super.retrieveMessageById(messageId);
    }

    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> deleteMessageById(@Nonnull String messageId)
    {
       checkCanAccessChannel();
       //We don't know if this is a Message sent by us or another user, so we can't run checks for Permission.MESSAGE_MANAGE
       return MessageChannel.super.deleteMessageById(messageId);
    }

    @Nonnull
    @Override
    default MessageHistory getHistory()
    {
        checkCanAccessChannel();
        checkCanViewHistory();
        return MessageChannel.super.getHistory();
    }

    @Nonnull
    @CheckReturnValue
    default MessagePaginationAction getIterableHistory()
    {
        checkCanAccessChannel();
        checkCanViewHistory();
        return MessageChannel.super.getIterableHistory();
    }

    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAround(@Nonnull String messageId, int limit)
    {
        checkCanAccessChannel();
        checkCanViewHistory();
        return MessageChannel.super.getHistoryAround(messageId, limit);
    }

    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAfter(@Nonnull String messageId, int limit)
    {
        checkCanAccessChannel();
        checkCanViewHistory();
        return MessageChannel.super.getHistoryAfter(messageId, limit);
    }

    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryBefore(@Nonnull String messageId, int limit)
    {
        checkCanAccessChannel();
        checkCanViewHistory();
        return MessageChannel.super.getHistoryBefore(messageId, limit);
    }

    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryFromBeginning(int limit)
    {
        checkCanAccessChannel();
        checkCanViewHistory();
        return MessageHistory.getHistoryFromBeginning(this).limit(limit);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> sendTyping()
    {
        checkCanAccessChannel();
        return MessageChannel.super.sendTyping();
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addReactionById(@Nonnull String messageId, @Nonnull String unicode)
    {
        checkCanAccessChannel();
        checkCanAddReactions();
        return MessageChannel.super.addReactionById(messageId, unicode);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(@Nonnull String messageId, @Nonnull String unicode)
    {
        checkCanAccessChannel();
        checkCanRemoveReactions();
        return MessageChannel.super.removeReactionById(messageId, unicode);
    }

    @Nonnull
    @CheckReturnValue
    default ReactionPaginationAction retrieveReactionUsersById(@Nonnull String messageId, @Nonnull String unicode)
    {
        checkCanAccessChannel();
        checkCanRemoveReactions();
        return MessageChannel.super.retrieveReactionUsersById(messageId, unicode);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> pinMessageById(@Nonnull String messageId)
    {
        checkCanAccessChannel();
        checkCanControlMessagePins();
        return MessageChannel.super.pinMessageById(messageId);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> unpinMessageById(@Nonnull String messageId)
    {
        checkCanAccessChannel();
        checkCanControlMessagePins();
        return MessageChannel.super.unpinMessageById(messageId);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<List<Message>> retrievePinnedMessages()
    {
        checkCanAccessChannel();
        return MessageChannel.super.retrievePinnedMessages();
    }

    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageById(@Nonnull String messageId, @Nonnull CharSequence newContent)
    {
        checkCanAccessChannel();
        checkCanSendMessage();
        return MessageChannel.super.editMessageById(messageId, newContent);
    }

    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageById(@Nonnull String messageId, @Nonnull Message newContent)
    {
       checkCanAccessChannel();
       checkCanSendMessage();
       return MessageChannel.super.editMessageById(messageId, newContent);
    }


    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageEmbedsById(@Nonnull String messageId, @Nonnull Collection<? extends MessageEmbed> newEmbeds)
    {
        checkCanAccessChannel();
        checkCanSendMessage();
        checkCanSendMessageEmbeds();
        return MessageChannel.super.editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageComponentsById(@Nonnull String messageId, @Nonnull Collection<? extends ComponentLayout> components)
    {
        checkCanAccessChannel();
        checkCanSendMessage();
        return MessageChannel.super.editMessageComponentsById(messageId, components);
    }

    // ---- State Accessors ----
    T setLatestMessageIdLong(long latestMessageId);

    // ---- Mixin Hooks ----
    void checkCanAccessChannel();
    void checkCanSendMessage();
    void checkCanSendMessageEmbeds();
    void checkCanSendFiles();
    void checkCanViewHistory();
    void checkCanAddReactions();
    void checkCanRemoveReactions();
    void checkCanControlMessagePins();

    boolean canDeleteOtherUsersMessages();

    // ---- Helpers -----
    default RestActionImpl<Void> bulkDeleteMessages(Collection<String> messageIds)
    {
        DataObject body = DataObject.empty().put("messages", messageIds);
        Route.CompiledRoute route = Route.Messages.DELETE_MESSAGES.compile(getId());
        return new RestActionImpl<>(getJDA(), route, body);
    }
}

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

package net.dv8tion.jda.internal.entities.channel.mixin.middleman;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import net.dv8tion.jda.internal.entities.channel.mixin.ChannelMixin;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface MessageChannelMixin<T extends MessageChannelMixin<T>> extends
        MessageChannel,
        MessageChannelUnion,
        ChannelMixin<T>
{
    // ---- Default implementations of interface ----
    @Nonnull
    default List<CompletableFuture<Void>> purgeMessages(@Nonnull List<? extends Message> messages)
    {
        checkCanAccess();
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

        return MessageChannelUnion.super.purgeMessages(messages);
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@Nonnull long... messageIds)
    {
        checkCanAccess();
        if (messageIds == null || messageIds.length == 0)
            return Collections.emptyList();

        //If we can't use the bulk delete system, then use the standard purge defined in MessageChannel
        if (!canDeleteOtherUsersMessages())
            return MessageChannelUnion.super.purgeMessagesById(messageIds);

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
    default MessageCreateAction sendMessage(@Nonnull CharSequence text)
    {
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessage(text);
    }

    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessageEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other)
    {
        checkCanSendMessage();
        checkCanSendMessageEmbeds();
        return MessageChannelUnion.super.sendMessageEmbeds(embed, other);
    }

    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        checkCanSendMessage();
        checkCanSendMessageEmbeds();
        return MessageChannelUnion.super.sendMessageEmbeds(embeds);
    }

    @NotNull
    @Override
    default MessageCreateAction sendMessageComponents(@NotNull LayoutComponent component, @NotNull LayoutComponent... other)
    {
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessageComponents(component, other);
    }

    @Nonnull
    @Override
    default MessageCreateAction sendMessageComponents(@Nonnull Collection<? extends LayoutComponent> components)
    {
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessageComponents(components);
    }

    @Nonnull
    @Override
    default MessageCreateAction sendMessagePoll(@Nonnull MessagePollData poll)
    {
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessagePoll(poll);
    }

    @Nonnull
    @Override
    default MessageCreateAction sendMessageComponents(@Nonnull Collection<? extends LayoutComponent> components)
    {
        checkCanAccessChannel();
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessageComponents(components);
    }

    @Nonnull
    @Override
    default MessageCreateAction sendMessageComponents(@Nonnull LayoutComponent component, @Nonnull LayoutComponent... other)
    {
        checkCanAccessChannel();
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessageComponents(component, other);
    }

    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessage(@Nonnull MessageCreateData msg)
    {
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessage(msg);
    }

    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendFiles(@Nonnull Collection<? extends FileUpload> files)
    {
        checkCanSendMessage();
        checkCanSendFiles();
        return MessageChannelUnion.super.sendFiles(files);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Message> retrieveMessageById(@Nonnull String messageId)
    {
        checkCanViewHistory();
        return MessageChannelUnion.super.retrieveMessageById(messageId);
    }

    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> deleteMessageById(@Nonnull String messageId)
    {
       checkCanAccess();
       //We don't know if this is a Message sent by us or another user, so we can't run checks for Permission.MESSAGE_MANAGE
       return MessageChannelUnion.super.deleteMessageById(messageId);
    }

    @Nonnull
    @Override
    default MessageHistory getHistory()
    {
        checkCanViewHistory();
        return MessageChannelUnion.super.getHistory();
    }

    @Nonnull
    @CheckReturnValue
    default MessagePaginationAction getIterableHistory()
    {
        checkCanViewHistory();
        return MessageChannelUnion.super.getIterableHistory();
    }

    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAround(@Nonnull String messageId, int limit)
    {
        checkCanViewHistory();
        return MessageChannelUnion.super.getHistoryAround(messageId, limit);
    }

    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAfter(@Nonnull String messageId, int limit)
    {
        checkCanViewHistory();
        return MessageChannelUnion.super.getHistoryAfter(messageId, limit);
    }

    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryBefore(@Nonnull String messageId, int limit)
    {
        checkCanViewHistory();
        return MessageChannelUnion.super.getHistoryBefore(messageId, limit);
    }

    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryFromBeginning(int limit)
    {
        checkCanViewHistory();
        return MessageHistory.getHistoryFromBeginning(this).limit(limit);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> sendTyping()
    {
        checkCanAccess();
        return MessageChannelUnion.super.sendTyping();
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addReactionById(@Nonnull String messageId, @Nonnull Emoji emoji)
    {
        checkCanAddReactions();
        return MessageChannelUnion.super.addReactionById(messageId, emoji);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(@Nonnull String messageId, @Nonnull Emoji emoji)
    {
        checkCanRemoveReactions();
        return MessageChannelUnion.super.removeReactionById(messageId, emoji);
    }

    @Nonnull
    @CheckReturnValue
    default ReactionPaginationAction retrieveReactionUsersById(@Nonnull String messageId, @Nonnull Emoji emoji)
    {
        checkCanRemoveReactions();
        return MessageChannelUnion.super.retrieveReactionUsersById(messageId, emoji);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> pinMessageById(@Nonnull String messageId)
    {
        checkCanControlMessagePins();
        return MessageChannelUnion.super.pinMessageById(messageId);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> unpinMessageById(@Nonnull String messageId)
    {
        checkCanControlMessagePins();
        return MessageChannelUnion.super.unpinMessageById(messageId);
    }

    @Nonnull
    @CheckReturnValue
    default RestAction<List<Message>> retrievePinnedMessages()
    {
        checkCanAccess();
        return MessageChannelUnion.super.retrievePinnedMessages();
    }

    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageById(@Nonnull String messageId, @Nonnull CharSequence newContent)
    {
        checkCanSendMessage();
        return MessageChannelUnion.super.editMessageById(messageId, newContent);
    }

    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageById(@Nonnull String messageId, @Nonnull MessageEditData data)
    {
       checkCanSendMessage();
       return MessageChannelUnion.super.editMessageById(messageId, data);
    }


    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageEmbedsById(@Nonnull String messageId, @Nonnull Collection<? extends MessageEmbed> newEmbeds)
    {
        checkCanSendMessage();
        checkCanSendMessageEmbeds();
        return MessageChannelUnion.super.editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageComponentsById(@Nonnull String messageId, @Nonnull Collection<? extends LayoutComponent> components)
    {
        checkCanSendMessage();
        return MessageChannelUnion.super.editMessageComponentsById(messageId, components);
    }

    @Nonnull
    @Override
    default MessageEditAction editMessageAttachmentsById(@Nonnull String messageId, @Nonnull Collection<? extends AttachedFile> attachments)
    {
        checkCanSendMessage();
        return MessageChannelUnion.super.editMessageAttachmentsById(messageId, attachments);
    }

    // ---- State Accessors ----
    T setLatestMessageIdLong(long latestMessageId);

    // ---- Mixin Hooks ----
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

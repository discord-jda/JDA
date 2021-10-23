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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;
import net.dv8tion.jda.api.requests.restaction.pagination.GuildThreadPaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.WebhookActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.GuildThreadPaginationActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.ReactionPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class BaseGuildMessageChannelImpl<T extends BaseGuildMessageChannel, M extends BaseGuildMessageChannelImpl<T, M>> extends AbstractChannelImpl<T, M> implements BaseGuildMessageChannel
{
    protected String topic;
    protected long lastMessageId;
    protected boolean nsfw;

    public BaseGuildMessageChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public boolean isNSFW()
    {
        return nsfw;
    }

    @Nullable
    @Override
    public String getTopic()
    {
        return topic;
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return lastMessageId;
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(getGuild().getMembersView().stream()
            .filter(m -> m.hasPermission(this, Permission.VIEW_CHANNEL))
            .collect(Collectors.toList()));
    }

    @Override
    public boolean canTalk(@Nonnull Member member)
    {
        if (!getGuild().equals(member.getGuild()))
            throw new IllegalArgumentException("Provided Member is not from the Guild that this TextChannel is part of.");

        return member.hasPermission(this, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
    }

    @Nonnull
    @Override
    public RestAction<List<Webhook>> retrieveWebhooks()
    {
        checkPermission(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Channels.GET_WEBHOOKS.compile(getId());
        JDAImpl jda = (JDAImpl) getJDA();
        return new RestActionImpl<>(jda, route, (response, request) ->
        {
            DataArray array = response.getArray();
            List<Webhook> webhooks = new ArrayList<>(array.length());
            EntityBuilder builder = jda.getEntityBuilder();

            for (int i = 0; i < array.length(); i++)
            {
                try
                {
                    webhooks.add(builder.createWebhook(array.getObject(i)));
                }
                catch (UncheckedIOException | NullPointerException e)
                {
                    JDAImpl.LOG.error("Error while creating websocket from json", e);
                }
            }

            return Collections.unmodifiableList(webhooks);
        });
    }

    @Nonnull
    @Override
    public WebhookAction createWebhook(@Nonnull String name)
    {
        Checks.notBlank(name, "Webhook name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");
        checkPermission(Permission.MANAGE_WEBHOOKS);

        return new WebhookActionImpl(getJDA(), this, name);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> deleteWebhookById(@Nonnull String id)
    {
        Checks.isSnowflake(id, "Webhook ID");

        checkPermission(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Webhooks.DELETE_WEBHOOK.compile(id);
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public RestAction<Void> deleteMessagesByIds(@Nonnull Collection<String> messageIds)
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
    @Override
    public RestAction<Void> clearReactionsById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");

        checkPermission(Permission.MESSAGE_MANAGE);
        final Route.CompiledRoute route = Route.Messages.REMOVE_ALL_REACTIONS.compile(getId(), messageId);
        return new RestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public RestAction<Void> clearReactionsById(@Nonnull String messageId, @Nonnull String unicode)
    {
        Checks.notNull(messageId, "Message ID");
        Checks.notNull(unicode, "Emote Name");
        checkPermission(Permission.MESSAGE_MANAGE);

        String code = EncodingUtil.encodeReaction(unicode);
        Route.CompiledRoute route = Route.Messages.CLEAR_EMOTE_REACTIONS.compile(getId(), messageId, code);
        return new RestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public RestActionImpl<Void> removeReactionById(@Nonnull String messageId, @Nonnull String unicode, @Nonnull User user)
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

    @Override
    public RestAction<GuildThread> createThread(String name, boolean isPrivate)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        if (isPrivate)
            checkPermission(Permission.CREATE_PRIVATE_THREADS);
        else
            checkPermission(Permission.CREATE_PUBLIC_THREADS);

        ChannelType threadType = isPrivate
            ? ChannelType.GUILD_PRIVATE_THREAD
            : getType() == ChannelType.TEXT
                ? ChannelType.GUILD_PUBLIC_THREAD
                : ChannelType.GUILD_NEWS_THREAD;

        //TODO: need to include "invitable" which is only valid for private threads
        //TODO: this needs to be a ThreadAction
        DataObject data = DataObject.empty()
            .put("name", name)
            .put("type", threadType.getId())
            .put("auto_archive_duration", GuildThread.AutoArchiveDuration.TIME_24_HOURS.getMinutes());

        Route.CompiledRoute route = Route.Channels.CREATE_THREAD_WITHOUT_MESSAGE.compile(getId());
        return new RestActionImpl<>(api, route, data, (response, request) -> {
            DataObject threadObj = response.getObject();
            return api.getEntityBuilder().createGuildThread(threadObj, getGuild().getIdLong());
        });
    }

    @Override
    public RestAction<GuildThread> createThread(String name, long messageId)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.CREATE_PUBLIC_THREADS);

        //TODO-threads: This needs to be a ThreadAction
        DataObject data = DataObject.empty()
            .put("name", name)
            .put("auto_archive_duration", GuildThread.AutoArchiveDuration.TIME_24_HOURS.getMinutes());

        Route.CompiledRoute route = Route.Channels.CREATE_THREAD_WITH_MESSAGE.compile(getId(), Long.toUnsignedString(messageId));
        return new RestActionImpl<>(api, route, data, (response, request) -> {
            DataObject threadObj = response.getObject();
            return api.getEntityBuilder().createGuildThread(threadObj, getGuild().getIdLong());
        });
    }

    @Override
    public GuildThreadPaginationActionImpl retrieveArchivedPublicThreads()
    {
        checkPermission(Permission.MESSAGE_HISTORY);

        Route.CompiledRoute route = Route.Channels.LIST_PUBLIC_ARCHIVED_THREADS.compile(getId());
        return new GuildThreadPaginationActionImpl(api, route, this);
    }

    @Override
    public GuildThreadPaginationActionImpl retrieveArchivedPrivateThreads()
    {
        checkPermission(Permission.MESSAGE_HISTORY);
        checkPermission(Permission.MANAGE_THREADS);

        Route.CompiledRoute route = Route.Channels.LIST_PRIVATE_ARCHIVED_THREADS.compile(getId());
        return new GuildThreadPaginationActionImpl(api, route, this);
    }

    @Override
    public GuildThreadPaginationAction retrieveArchivedPrivateJoinedThreads()
    {
        checkPermission(Permission.MESSAGE_HISTORY);

        Route.CompiledRoute route = Route.Channels.LIST_JOINED_PRIVATE_ARCHIVED_THREADS.compile(getId());
        return new GuildThreadPaginationActionImpl(api, route, this);
    }

    // ---- Overrides for Permission Injection ----

    @Nonnull
    @Override
    public List<CompletableFuture<Void>> purgeMessages(@Nonnull List<? extends Message> messages)
    {
        if (messages == null || messages.isEmpty())
            return Collections.emptyList();
        boolean hasPerms = getGuild().getSelfMember().hasPermission(this, Permission.MESSAGE_MANAGE);
        if (!hasPerms)
        {
            for (Message m : messages)
            {
                if (m.getAuthor().equals(getJDA().getSelfUser()))
                    continue;
                throw new InsufficientPermissionException(this, Permission.MESSAGE_MANAGE, "Cannot delete messages of other users");
            }
        }
        return BaseGuildMessageChannel.super.purgeMessages(messages);
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public List<CompletableFuture<Void>> purgeMessagesById(@Nonnull long... messageIds)
    {
        if (messageIds == null || messageIds.length == 0)
            return Collections.emptyList();

        //If we can't use the bulk delete system, then use the standard purge defined in MessageChannel
        if (!getGuild().getSelfMember().hasPermission(this, Permission.MESSAGE_MANAGE))
            return BaseGuildMessageChannel.super.purgeMessagesById(messageIds);

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
    @Override
    public MessageAction sendMessage(@Nonnull CharSequence text)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND);
        return BaseGuildMessageChannel.super.sendMessage(text);
    }

    @Nonnull
    @Override
    public MessageAction sendMessage(@Nonnull MessageEmbed embed)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND);
        // this is checked because you cannot send an empty message
        checkPermission(Permission.MESSAGE_EMBED_LINKS);
        return BaseGuildMessageChannel.super.sendMessage(embed);
    }

    @Nonnull
    @Override
    public MessageAction sendMessage(@Nonnull Message msg)
    {
        Checks.notNull(msg, "Message");

        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND);
        if (msg.getContentRaw().isEmpty() && !msg.getEmbeds().isEmpty())
            checkPermission(Permission.MESSAGE_EMBED_LINKS);

        //Call MessageChannel's default
        return BaseGuildMessageChannel.super.sendMessage(msg);
    }

    @Nonnull
    @Override
    public MessageAction sendFile(@Nonnull File file, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND);
        checkPermission(Permission.MESSAGE_ATTACH_FILES);

        final long maxSize = getGuild().getMaxFileSize();
        Checks.check(file == null || file.length() <= maxSize,
                    "File may not exceed the maximum file length of %d bytes!", maxSize);

        //Call MessageChannel's default method
        return BaseGuildMessageChannel.super.sendFile(file, fileName, options);
    }

    @Nonnull
    @Override
    public MessageAction sendFile(@Nonnull InputStream data, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND);
        checkPermission(Permission.MESSAGE_ATTACH_FILES);

        //Call MessageChannel's default method
        return BaseGuildMessageChannel.super.sendFile(data, fileName, options);
    }

    @Nonnull
    @Override
    public MessageAction sendFile(@Nonnull byte[] data, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND);
        checkPermission(Permission.MESSAGE_ATTACH_FILES);

        final long maxSize = getGuild().getMaxFileSize();
        Checks.check(data == null || data.length <= maxSize, "File is too big! Max file-size is %d bytes", maxSize);

        //Call MessageChannel's default method
        return BaseGuildMessageChannel.super.sendFile(data, fileName, options);
    }

    @Nonnull
    @Override
    public RestAction<Message> retrieveMessageById(@Nonnull String messageId)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return BaseGuildMessageChannel.super.retrieveMessageById(messageId);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> deleteMessageById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");
        checkPermission(Permission.VIEW_CHANNEL);

        //Call MessageChannel's default method
        return BaseGuildMessageChannel.super.deleteMessageById(messageId);
    }

    @Nonnull
    @Override
    public RestAction<Void> pinMessageById(@Nonnull String messageId)
    {
        checkPermission(Permission.VIEW_CHANNEL, "You cannot pin a message in a channel you can't access. (VIEW_CHANNEL)");
        checkPermission(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");

        //Call MessageChannel's default method
        return BaseGuildMessageChannel.super.pinMessageById(messageId);
    }

    @Nonnull
    @Override
    public RestAction<Void> unpinMessageById(@Nonnull String messageId)
    {
        checkPermission(Permission.VIEW_CHANNEL, "You cannot unpin a message in a channel you can't access. (VIEW_CHANNEL)");
        checkPermission(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");

        //Call MessageChannel's default method
        return BaseGuildMessageChannel.super.unpinMessageById(messageId);
    }

    @Nonnull
    @Override
    public RestAction<List<Message>> retrievePinnedMessages()
    {
        checkPermission(Permission.VIEW_CHANNEL, "Cannot get the pinned message of a channel without VIEW_CHANNEL access.");

        //Call MessageChannel's default method
        return BaseGuildMessageChannel.super.retrievePinnedMessages();
    }

    @Nonnull
    @Override
    public RestAction<Void> addReactionById(@Nonnull String messageId, @Nonnull String unicode)
    {
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return BaseGuildMessageChannel.super.addReactionById(messageId, unicode);
    }

    @Nonnull
    @Override
    public RestAction<Void> addReactionById(@Nonnull String messageId, @Nonnull Emote emote)
    {
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return BaseGuildMessageChannel.super.addReactionById(messageId, emote);
    }

    @Nonnull
    @Override
    public ReactionPaginationAction retrieveReactionUsersById(@Nonnull String messageId, @Nonnull String unicode)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notEmpty(unicode, "Emoji");
        Checks.noWhitespace(unicode, "Emoji");

        checkPermission(Permission.MESSAGE_HISTORY);

        return new ReactionPaginationActionImpl(this, messageId, EncodingUtil.encodeUTF8(unicode));
    }

    @Nonnull
    @Override
    public ReactionPaginationAction retrieveReactionUsersById(@Nonnull String messageId, @Nonnull Emote emote)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(emote, "Emote");

        checkPermission(Permission.MESSAGE_HISTORY);

        return new ReactionPaginationActionImpl(this, messageId, String.format("%s:%s", emote, emote.getId()));
    }

    @Nonnull
    @Override
    public MessageAction editMessageById(@Nonnull String messageId, @Nonnull CharSequence newContent)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND);
        return BaseGuildMessageChannel.super.editMessageById(messageId, newContent);
    }

    @Nonnull
    @Override
    @Deprecated
    public MessageAction editMessageById(@Nonnull String messageId, @Nonnull MessageEmbed newEmbed)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND);
        checkPermission(Permission.MESSAGE_EMBED_LINKS);
        return BaseGuildMessageChannel.super.editMessageById(messageId, newEmbed);
    }

    @Nonnull
    @Override
    public MessageAction editMessageEmbedsById(@Nonnull String messageId, @Nonnull Collection<? extends MessageEmbed> newEmbeds)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND);
        checkPermission(Permission.MESSAGE_EMBED_LINKS);
        return BaseGuildMessageChannel.super.editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    @Override
    public MessageAction editMessageById(@Nonnull String id, @Nonnull Message newContent)
    {
        Checks.notNull(newContent, "Message");

        //checkVerification(); no verification needed to edit a message
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND);
        if (newContent.getContentRaw().isEmpty() && !newContent.getEmbeds().isEmpty())
            checkPermission(Permission.MESSAGE_EMBED_LINKS);

        //Call MessageChannel's default
        return BaseGuildMessageChannel.super.editMessageById(id, newContent);
    }

    // -- Setters --

    public M setTopic(String topic)
    {
        this.topic = topic;
        return (M) this;
    }

    public M setLastMessageId(long id)
    {
        this.lastMessageId = id;
        return (M) this;
    }

    public M setNSFW(boolean nsfw)
    {
        this.nsfw = nsfw;
        return (M) this;
    }

    // -- internal --
    private RestActionImpl<Void> bulkDeleteMessages(Collection<String> messageIds)
    {
        DataObject body = DataObject.empty().put("messages", messageIds);
        Route.CompiledRoute route = Route.Messages.DELETE_MESSAGES.compile(getId());
        return new RestActionImpl<>(getJDA(), route, body);
    }
}

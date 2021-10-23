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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.ReactionPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GuildThreadImpl implements GuildThread
{
    private final long id;
    private final ChannelType type;
    private final JDAImpl api;
    private final CacheView.SimpleCacheView<GuildThreadMember> threadMembers = new CacheView.SimpleCacheView<>(GuildThreadMember.class, null);

    private GuildImpl guild;
    private String name;
    private AutoArchiveDuration autoArchiveDuration;
    private boolean locked;
    private boolean archived;
    private long parentChannelId;
    private long archiveTimestamp;
    private long ownerId;
    private long lastMessageId;
    private int messageCount;
    private int memberCount;
    private int slowmode;

    public GuildThreadImpl(long id, ChannelType type, GuildImpl guild)
    {
        this.id = id;
        this.type = type;
        this.api = guild.getJDA();
        this.guild = guild;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @NotNull
    @Override
    public String getName()
    {
        return name;
    }

    @NotNull
    @Override
    public ChannelType getType()
    {
        return type;
    }

    @NotNull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @NotNull
    @Override
    public Guild getGuild()
    {
        GuildImpl realGuild = (GuildImpl) api.getGuildById(guild.getIdLong());
        if (realGuild != null)
            guild = realGuild;
        return guild;
    }

    @NotNull
    @Override
    public ChannelManager<? extends GuildChannel> getManager()
    {
        return null;
    }

    @Override
    @Nonnull
    public BaseGuildMessageChannel getParentChannel()
    {
        //noinspection ConstantConditions
        return (BaseGuildMessageChannel) guild.getGuildChannelById(parentChannelId);
    }

    @Override
    public int getMessageCount()
    {
        return messageCount;
    }

    @Override
    public int getMemberCount()
    {
        return memberCount;
    }

    @Override
    @Nullable
    public List<GuildThreadMember> getThreadMembers()
    {
        return threadMembers.asList();
    }

    @Override
    public GuildThreadMember getThreadMemberById(long id)
    {
        return threadMembers.get(id);
    }

    @Override
    public long getOwnerIdLong()
    {
        return ownerId;
    }

    @Override
    public boolean isLocked()
    {
        return locked;
    }

    @Override
    public boolean isArchived()
    {
        return archived;
    }

    @Override
    public OffsetDateTime getTimeArchive()
    {
        return Helpers.toOffset(archiveTimestamp);
    }

    @Override
    @Nonnull
    public AutoArchiveDuration getAutoArchiveDuration()
    {
        return autoArchiveDuration;
    }

    @Override
    public int getSlowmode()
    {
        return slowmode;
    }

    @Override
    public RestAction<Void> join()
    {
        checkUnarchived();

        Route.CompiledRoute route = Route.Channels.JOIN_THREAD.compile(getId());
        return new RestActionImpl<>(api, route);
    }

    @Override
    public RestAction<Void> leave()
    {
        checkUnarchived();

        Route.CompiledRoute route = Route.Channels.LEAVE_THREAD.compile(getId());
        return new RestActionImpl<>(api, route);
    }

    @Override
    public RestAction<Void> addThreadMemberById(long userId)
    {
        checkUnarchived();

        Route.CompiledRoute route = Route.Channels.ADD_THREAD_MEMBER.compile(getId(), Long.toUnsignedString(userId));
        return new RestActionImpl<>(api, route);
    }

    @Override
    public RestAction<Void> removeThreadMemberById(long userId)
    {
        checkUnarchived();

        boolean privateThreadOwner = type == ChannelType.GUILD_PRIVATE_THREAD && ownerId == api.getSelfUser().getIdLong();
        if (!privateThreadOwner) {
            checkPermission(Permission.MANAGE_THREADS);
        }

        Route.CompiledRoute route = Route.Channels.REMOVE_THREAD_MEMBER.compile(getId(), Long.toUnsignedString(userId));
        return new RestActionImpl<>(api, route);

    }

    @Override
    public RestAction<List<GuildThreadMember>> retrieveThreadMembers()
    {
        //TODO-threads: This interacts with GUILD_MEMBERS in some way. Need to test and document how.

        Route.CompiledRoute route = Route.Channels.LIST_THREAD_MEMBERS.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            EntityBuilder builder = api.getEntityBuilder();
            List<GuildThreadMember> threadMembers = new LinkedList<>();
            DataArray memberArr = response.getArray();

            for (int i = 0; i < memberArr.length(); i++)
            {
                final DataObject object = memberArr.getObject(i);
                //Very possible this is gonna break because we don't get user/member info with threadmembers
                //TODO revisit the @Nonnull annotations in GuildThreadMember due to the lack of user/member info. Might be a time to introduce RestX entities?
                threadMembers.add(builder.createGuildThreadMember((GuildImpl) this.getGuild(), this, object));
            }
            return Collections.unmodifiableList(threadMembers);
        });
    }

    @NotNull
    @Override
    public List<Member> getMembers()
    {
        return threadMembers.stream().map(GuildThreadMember::getMember).collect(Collectors.toList());
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return lastMessageId;
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> delete()
    {
        checkPermission(Permission.MANAGE_THREADS);

        Route.CompiledRoute route = Route.Channels.DELETE_CHANNEL.compile(getId());
        return new AuditableRestActionImpl<>(api, route);
    }

    @Override
    public boolean canTalk(@NotNull Member member)
    {
        Checks.notNull(member, "Member");
        return member.hasPermission(getParentChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND_IN_THREADS);
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

    //TODO-Threads: CompareTo doesn't really make sense here given that there is no sorting of threads. What do?
    @Override
    public int compareTo(@NotNull GuildChannel o)
    {
        return 0;
    }
    
    // =========== Inject Permissions ================

    @Nonnull
    @Override
    public List<CompletableFuture<Void>> purgeMessages(@Nonnull List<? extends Message> messages)
    {
        if (messages == null || messages.isEmpty())
            return Collections.emptyList();
        boolean hasPerms = getGuild().getSelfMember().hasPermission(this.getParentChannel(), Permission.MESSAGE_MANAGE);
        if (!hasPerms)
        {
            for (Message m : messages)
            {
                if (m.getAuthor().equals(getJDA().getSelfUser()))
                    continue;
                throw new InsufficientPermissionException(this, Permission.MESSAGE_MANAGE, "Cannot delete messages of other users");
            }
        }
        return GuildThread.super.purgeMessages(messages);
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public List<CompletableFuture<Void>> purgeMessagesById(@Nonnull long... messageIds)
    {
        if (messageIds == null || messageIds.length == 0)
            return Collections.emptyList();

        //If we can't use the bulk delete system, then use the standard purge defined in MessageChannel
        if (!getGuild().getSelfMember().hasPermission(this.getParentChannel(), Permission.MESSAGE_MANAGE))
            return GuildThread.super.purgeMessagesById(messageIds);

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
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        return GuildThread.super.sendMessage(text);
    }

    @Nonnull
    @Override
    public MessageAction sendMessage(@Nonnull MessageEmbed embed)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        // this is checked because you cannot send an empty message
        checkPermission(Permission.MESSAGE_EMBED_LINKS);
        return GuildThread.super.sendMessage(embed);
    }

    @Nonnull
    @Override
    public MessageAction sendMessage(@Nonnull Message msg)
    {
        Checks.notNull(msg, "Message");

        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        if (msg.getContentRaw().isEmpty() && !msg.getEmbeds().isEmpty())
            checkPermission(Permission.MESSAGE_EMBED_LINKS);

        //Call MessageChannel's default
        return GuildThread.super.sendMessage(msg);
    }

    @Nonnull
    @Override
    public MessageAction sendFile(@Nonnull File file, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        checkPermission(Permission.MESSAGE_ATTACH_FILES);

        final long maxSize = getGuild().getMaxFileSize();
        Checks.check(file == null || file.length() <= maxSize,
                "File may not exceed the maximum file length of %d bytes!", maxSize);

        //Call MessageChannel's default method
        return GuildThread.super.sendFile(file, fileName, options);
    }

    @Nonnull
    @Override
    public MessageAction sendFile(@Nonnull InputStream data, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        checkPermission(Permission.MESSAGE_ATTACH_FILES);

        //Call MessageChannel's default method
        return GuildThread.super.sendFile(data, fileName, options);
    }

    @Nonnull
    @Override
    public MessageAction sendFile(@Nonnull byte[] data, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        checkPermission(Permission.MESSAGE_ATTACH_FILES);

        final long maxSize = getGuild().getMaxFileSize();
        Checks.check(data == null || data.length <= maxSize, "File is too big! Max file-size is %d bytes", maxSize);

        //Call MessageChannel's default method
        return GuildThread.super.sendFile(data, fileName, options);
    }

    @Nonnull
    @Override
    public RestAction<Message> retrieveMessageById(@Nonnull String messageId)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return GuildThread.super.retrieveMessageById(messageId);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> deleteMessageById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");
        checkPermission(Permission.VIEW_CHANNEL);

        //Call MessageChannel's default method
        return GuildThread.super.deleteMessageById(messageId);
    }

    @Nonnull
    @Override
    public RestAction<Void> pinMessageById(@Nonnull String messageId)
    {
        checkPermission(Permission.VIEW_CHANNEL, "You cannot pin a message in a channel you can't access. (VIEW_CHANNEL)");
        checkPermission(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");

        //Call MessageChannel's default method
        return GuildThread.super.pinMessageById(messageId);
    }

    @Nonnull
    @Override
    public RestAction<Void> unpinMessageById(@Nonnull String messageId)
    {
        checkPermission(Permission.VIEW_CHANNEL, "You cannot unpin a message in a channel you can't access. (VIEW_CHANNEL)");
        checkPermission(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");

        //Call MessageChannel's default method
        return GuildThread.super.unpinMessageById(messageId);
    }

    @Nonnull
    @Override
    public RestAction<List<Message>> retrievePinnedMessages()
    {
        checkPermission(Permission.VIEW_CHANNEL, "Cannot get the pinned message of a channel without VIEW_CHANNEL access.");

        //Call MessageChannel's default method
        return GuildThread.super.retrievePinnedMessages();
    }

    @Nonnull
    @Override
    public RestAction<Void> addReactionById(@Nonnull String messageId, @Nonnull String unicode)
    {
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return GuildThread.super.addReactionById(messageId, unicode);
    }

    @Nonnull
    @Override
    public RestAction<Void> addReactionById(@Nonnull String messageId, @Nonnull Emote emote)
    {
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return GuildThread.super.addReactionById(messageId, emote);
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
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        return GuildThread.super.editMessageById(messageId, newContent);
    }

    @Nonnull
    @Override
    @Deprecated
    public MessageAction editMessageById(@Nonnull String messageId, @Nonnull MessageEmbed newEmbed)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        checkPermission(Permission.MESSAGE_EMBED_LINKS);
        return GuildThread.super.editMessageById(messageId, newEmbed);
    }

    @Nonnull
    @Override
    public MessageAction editMessageEmbedsById(@Nonnull String messageId, @Nonnull Collection<? extends MessageEmbed> newEmbeds)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        checkPermission(Permission.MESSAGE_EMBED_LINKS);
        return GuildThread.super.editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    @Override
    public MessageAction editMessageById(@Nonnull String id, @Nonnull Message newContent)
    {
        Checks.notNull(newContent, "Message");

        //checkVerification(); no verification needed to edit a message
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        if (newContent.getContentRaw().isEmpty() && !newContent.getEmbeds().isEmpty())
            checkPermission(Permission.MESSAGE_EMBED_LINKS);

        //Call MessageChannel's default
        return GuildThread.super.editMessageById(id, newContent);
    }

    // ====== Non-API getters =====

    public long getArchiveTimestamp()
    {
        return archiveTimestamp;
    }

    public CacheView.SimpleCacheView<GuildThreadMember> getThreadMemberView()
    {
        return threadMembers;
    }

    // ====== Setters ======

    public GuildThreadImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public GuildThreadImpl setAutoArchiveDuration(AutoArchiveDuration autoArchiveDuration)
    {
        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }

    public GuildThreadImpl setParentChannelId(long parentChannelId)
    {
        this.parentChannelId = parentChannelId;
        return this;
    }

    public GuildThreadImpl setLocked(boolean locked)
    {
        this.locked = locked;
        return this;
    }

    public GuildThreadImpl setArchived(boolean archived)
    {
        this.archived = archived;
        return this;
    }

    public GuildThreadImpl setArchiveTimestamp(long archiveTimestamp)
    {
        this.archiveTimestamp = archiveTimestamp;
        return this;
    }

    public GuildThreadImpl setOwnerId(long ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    public GuildThreadImpl setMessageCount(int messageCount)
    {
        this.messageCount = messageCount;
        return this;
    }

    public GuildThreadImpl setMemberCount(int memberCount)
    {
        this.memberCount = memberCount;
        return this;
    }

    public GuildThreadImpl setLastMessageId(long lastMessageId)
    {
        this.lastMessageId = lastMessageId;
        return this;
    }

    public GuildThreadImpl setSlowmode(int slowmode)
    {
        this.slowmode = slowmode;
        return this;
    }
    
    // ===== Internal =====

    // -- internal --
    private RestActionImpl<Void> bulkDeleteMessages(Collection<String> messageIds)
    {
        DataObject body = DataObject.empty().put("messages", messageIds);
        Route.CompiledRoute route = Route.Messages.DELETE_MESSAGES.compile(getId());
        return new RestActionImpl<>(getJDA(), route, body);
    }

    private void checkUnarchived()
    {
        if (archived) {
            throw new IllegalStateException("Cannot modify a GuildThread while it is archived!");
        }
    }

    private void checkPermission(Permission permission) {checkPermission(permission, null);}
    private void checkPermission(Permission permission, String message)
    {
        if (!getGuild().getSelfMember().hasPermission(this.getParentChannel(), permission))
        {
            if (message != null)
                throw new InsufficientPermissionException(this, permission, message);
            else
                throw new InsufficientPermissionException(this, permission);
        }
    }
}

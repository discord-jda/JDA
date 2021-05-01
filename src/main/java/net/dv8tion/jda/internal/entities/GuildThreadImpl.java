package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.GuildThread;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class GuildThreadImpl extends AbstractChannelImpl<GuildThread, GuildThreadImpl> implements GuildThread
{
    //TODO this same pattern is used in MemberImpl. Might want to consider centralizing? dunno.
    private static final ZoneOffset OFFSET = ZoneOffset.of("+00:00");

    //TODO | If / When we introduce PublicThread/PrivateThread/NewsThread then we wont need this field
    private final ChannelType channelType;

    private GuildThreadMember selfThreadMember;
    private AutoArchiveDuration autoArchiveDuration;
    private boolean isArchived;
    private long parentChannelId;
    private long archivingMemberId;
    private long archiveTimestamp;
    private long ownerId;
    private long lastMessageId;
    private int messageCount;
    private int memberCount;

    public GuildThreadImpl(long id, GuildImpl guild, ChannelType channelType)
    {
        super(id, guild);
        this.channelType = channelType;
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return lastMessageId;
    }

    @Override
    public boolean hasLatestMessage()
    {
        return lastMessageId != 0;
    }

    @NotNull
    @Override
    public ChannelType getType()
    {
        return channelType;
    }

    @NotNull
    @Override
    public List<Member> getMembers()
    {
        throw new NotImplementedException();
    }

    @Override
    public int getPosition()
    {
        throw new UnsupportedOperationException("Thread do not have a concept of position");
    }

    @Override
    public GuildChannel getParentChannel()
    {
        return guild.getGuildChannelById(parentChannelId);
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
    public boolean isSubscribedToThread()
    {
        return selfThreadMember != null;
    }

    @Override
    public GuildThreadMember getSelfThreadMember()
    {
        return selfThreadMember;
    }

    @Override
    public List<GuildThreadMember> getThreadMembers()
    {
        //TODO | Implement
        throw new NotImplementedException();
    }

    @Override
    public GuildThreadMember getThreadMemberById(long id)
    {
        //TODO | Implement
        throw new NotImplementedException();
    }

    @Override
    public Member getOwner()
    {
        return guild.getMemberById(ownerId);
    }

    @Override
    public String getOwnerId()
    {
        return Long.toUnsignedString(ownerId);
    }

    @Override
    public long getOwnerIdLong()
    {
        return ownerId;
    }

    @Override
    public boolean isArchived()
    {
        return isArchived;
    }

    @Override
    public OffsetDateTime getTimeArchive()
    {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(archiveTimestamp), OFFSET);
    }

    @Override
    public Member getArchivingMember()
    {
        if (archivingMemberId == 0)
            return null;

        return guild.getMemberById(archivingMemberId);
    }

    @Override
    public String getArchivingMemberId()
    {
        if (archivingMemberId == 0)
            return null;

        return Long.toUnsignedString(archivingMemberId);
    }

    @Override
    public long getArchivingMemberIdLong()
    {
        return archivingMemberId;
    }

    @Override
    public AutoArchiveDuration getAutoArchiveDuration()
    {
        return autoArchiveDuration;
    }

    @NotNull
    @Override
    public ChannelAction<GuildThread> createCopy(@NotNull Guild guild)
    {
        throw new UnsupportedOperationException("Cannot copy a thread");
    }

    public GuildThreadImpl setParentChannelId(long parentChannelId)
    {
        this.parentChannelId = parentChannelId;
        return this;
    }

    public GuildThreadImpl setSelfThreadMember(GuildThreadMember selfThreadMember)
    {
        this.selfThreadMember = selfThreadMember;
        return this;
    }

    public GuildThreadImpl setAutoArchiveDuration(AutoArchiveDuration autoArchiveDuration)
    {
        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }

    public GuildThreadImpl setIsArchived(boolean isArchived)
    {
        this.isArchived = isArchived;
        return this;
    }

    public GuildThreadImpl setArchivingMemberId(long archivingMemberId)
    {
        this.archivingMemberId = archivingMemberId;
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

    public GuildThreadImpl setLastMessageId(long lastMessageId)
    {
        this.lastMessageId = lastMessageId;
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
}

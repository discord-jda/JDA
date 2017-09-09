package net.dv8tion.jda.core.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.InviteAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryImpl extends AbstractChannelImpl<CategoryImpl> implements Category
{
    protected final TLongObjectMap<Channel> channels = MiscUtil.newLongMap();

    public CategoryImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public Category getParent()
    {
        return null;
    }

    @Override
    public int compareTo(Category other)
    {
        Checks.notNull(other, "Other Category");
        if (other.equals(this))
            return 0;
        Checks.check(getGuild().equals(other.getGuild()), "Cannot compare categories from different guilds!");
        if (rawPosition == other.getPositionRaw())
            return Long.compare(id, other.getIdLong());
        return Integer.compare(rawPosition, other.getPositionRaw());
    }

    @Override
    public ChannelType getType()
    {
        return ChannelType.CATEGORY;
    }

    @Override
    public List<Member> getMembers()
    {
        //TODO
        return null;
    }

    @Override
    public int getPosition()
    {
        //TODO
        return 0;
    }

    @Override
    public InviteAction createInvite()
    {
        throw new UnsupportedOperationException("Cannot create invites for category!");
    }

    @Override
    public RestAction<List<Invite>> getInvites()
    {
        return new AuditableRestAction.EmptyRestAction<>(getJDA(), Collections.emptyList());
    }

    @Override
    public List<Channel> getChannels()
    {
        List<Channel> channels = new ArrayList<>();
        channels.addAll(getTextChannels());
        channels.addAll(getVoiceChannels());
        return Collections.unmodifiableList(channels);
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        return Collections.unmodifiableList(getGuild().getTextChannels().stream()
                    .filter(channel -> channel.getParent().equals(this))
                    .collect(Collectors.toList()));
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        return Collections.unmodifiableList(getGuild().getVoiceChannels().stream()
                    .filter(channel -> channel.getParent().equals(this))
                    .collect(Collectors.toList()));
    }

    @Override
    public String toString()
    {
        return "GC:" + getName() + '(' + id + ')';
    }
}

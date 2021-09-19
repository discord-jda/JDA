package net.dv8tion.jda.internal.entities;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MiscUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractGuildAudioChannelImpl<T extends AudioChannel, M extends AbstractGuildAudioChannelImpl<T, M>> extends AbstractChannelImpl<T, M> implements AudioChannel
{
    protected final TLongObjectMap<Member> connectedMembers = MiscUtil.newLongMap();
    protected int bitrate;
    protected String region;

    public AbstractGuildAudioChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public int getBitrate()
    {
        return bitrate;
    }

    @Nullable
    @Override
    public String getRegionRaw()
    {
        return region;
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(new ArrayList<>(getConnectedMembersMap().valueCollection()));
    }

    public M setBitrate(int bitrate)
    {
        this.bitrate = bitrate;
        return (M) this;
    }

    public M setRegion(String region)
    {
        this.region = region;
        return (M) this;
    }

    // -- Map Getters --

    public TLongObjectMap<Member> getConnectedMembersMap()
    {
        connectedMembers.transformValues((member) -> {
            // Load real member instance from cache to provided up-to-date cache information
            Member real = getGuild().getMemberById(member.getIdLong());
            return real != null ? real : member;
        });

        return connectedMembers;
    }
}

package net.dv8tion.jda.core.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.entities.CategoryChannel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CategoryChannelImpl extends AbstractChannelImpl<CategoryChannelImpl> implements CategoryChannel
{
    private final TLongObjectMap<TextChannel> textChannels = MiscUtil.newLongMap();

    public CategoryChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public int compareTo(CategoryChannel chan)
    {
        if (this == chan)
            return 0;

        if (!this.getGuild().equals(chan.getGuild()))
            throw new IllegalArgumentException("Cannot compare CategoryChannels that aren't from the same guild!");

        if (this.getPositionRaw() != chan.getPositionRaw())
            return chan.getPositionRaw() - this.getPositionRaw();

        OffsetDateTime thisTime = this.getCreationTime();
        OffsetDateTime chanTime = chan.getCreationTime();

        //We compare the provided channel's time to this's time instead of the reverse as one would expect due to how
        // discord deals with hierarchy. The more recent a channel was created, the lower its hierarchy ranking when
        // it shares the same position as another channel.
        return chanTime.compareTo(thisTime);
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        ArrayList<TextChannel> channels = new ArrayList<>(textChannels.valueCollection());
        channels.sort(Comparator.reverseOrder());
        return Collections.unmodifiableList(channels);
    }

    @Override
    public ChannelType getType()
    {
        return ChannelType.CATEGORY;
    }

    @Override
    public List<Member> getMembers()
    {
        return Collections.emptyList();
    }

    @Override
    public int getPosition()
    {
        return 0;
    }

    @Override
    public String toString()
    {
        return "CC:" + getName() + '(' + id + ')';
    }
}

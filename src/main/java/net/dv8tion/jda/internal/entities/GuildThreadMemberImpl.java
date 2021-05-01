package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.GuildThread;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class GuildThreadMemberImpl implements GuildThreadMember
{
    //TODO this same pattern is used in MemberImpl. Might want to consider centralizing? dunno.
    private static final ZoneOffset OFFSET = ZoneOffset.of("+00:00");

    private final GuildThread guildThread;
    private final long userId;

    private long joinDate;
    private long flags;

    public GuildThreadMemberImpl(GuildThread guildThread, long userId)
    {
        this.guildThread = guildThread;
        this.userId = userId;
    }

    @NotNull
    @Override
    public JDA getJDA()
    {
        return guildThread.getJDA();
    }

    @NotNull
    @Override
    public GuildThread getThread()
    {
        return guildThread;
    }

    @Override
    public Guild getGuild()
    {
        return guildThread.getGuild();
    }

    @Override
    public Member getMember()
    {
        //TODO evaluate whether this is problematic
        return guildThread.getGuild().getMember(getUser());
    }

    @Override
    public User getUser()
    {
        //TODO | Revisit this lookup. Is this for the best?
        return getJDA().getUserById(userId);
    }

    @NotNull
    @Override
    public OffsetDateTime getTimeJoined()
    {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(joinDate), OFFSET);
    }

    @Override
    public long getFlagsRaw()
    {
        return flags;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getThread(), getUser());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof GuildThreadMember))
            return false;

        GuildThreadMemberImpl oThreadMember = (GuildThreadMemberImpl) obj;
        return this.getThread().getId().equals(oThreadMember.getThread().getId())
            && this.userId == oThreadMember.userId;
    }

    GuildThreadMemberImpl setJoinDate(long joinDate)
    {
        this.joinDate = joinDate;
        return this;
    }

    GuildThreadMemberImpl setFlags(long flags)
    {
        this.flags = flags;
        return this;
    }
}

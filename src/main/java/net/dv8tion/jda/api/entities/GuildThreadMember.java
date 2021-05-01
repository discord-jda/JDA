package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

public interface GuildThreadMember
{
    @Nonnull
    JDA getJDA();

    @Nonnull
    GuildThread getThread();

    Guild getGuild();

    Member getMember();

    User getUser();

    @Nonnull
    OffsetDateTime getTimeJoined();

    //TODO | Set<ThreadMemberFlags> getFlags();

    long getFlagsRaw();
}

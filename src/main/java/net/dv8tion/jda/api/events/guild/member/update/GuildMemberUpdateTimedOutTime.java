package net.dv8tion.jda.api.events.guild.member.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class GuildMemberUpdateTimedOutTime extends GenericGuildMemberUpdateEvent<OffsetDateTime>
{
    public static final String IDENTIFIER = "timedout_time";

    public GuildMemberUpdateTimedOutTime(@Nonnull JDA api, long responseNumber, @Nonnull Member member, @Nullable OffsetDateTime previous)
    {
        super(api, responseNumber, member, previous, member.getTimeUntilTimedOut(), IDENTIFIER);
    }

    /**
     * The old time until in time out
     *
     * @return The old time until in time out
     */
    @Nullable
    public OffsetDateTime getOldTimeUntilTimedOut()
    {
        return getOldValue();
    }

    /**
     * The mew time until in time out
     *
     * @return The new time until in time out
     */
    @Nullable
    public OffsetDateTime getNewTimeUntilTimedOut()
    {
        return getNewValue();
    }
}

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.InviteImpl;
import net.dv8tion.jda.core.requests.RestAction;

import org.apache.http.annotation.Immutable;

import java.time.OffsetDateTime;

@Immutable
public interface Invite
{
    static interface Channel extends ISnowflake
    {

        String getName();

        ChannelType getType();

    }

    static interface Guild extends ISnowflake
    {

        String getIconId();

        String getName();

        String getSplashId();
    }

    public static RestAction<Invite> resolve(final JDA api, final String code)
    {
        return InviteImpl.resolve(api, code);
    }

    public RestAction<Invite> expand();

    Channel getChannel();

    String getCode();

    Guild getGuild();

    User getInviter();

    int getMaxAge();

    int getMaxUses();

    OffsetDateTime getTimeCreated();

    int getUses();

    public boolean isExpanded();

    boolean isTemporary();

}

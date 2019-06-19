package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.TeamMember;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

public class TeamMemberImpl implements TeamMember
{
    private final User user;
    private final MembershipState state;
    private final long teamId;

    public TeamMemberImpl(User user, MembershipState state, long teamId)
    {
        this.user = user;
        this.state = state;
        this.teamId = teamId;
    }

    @Nonnull
    @Override
    public User getUser()
    {
        return user;
    }

    @Nonnull
    @Override
    public MembershipState getMembershipState()
    {
        return state;
    }

    @Override
    public long getTeamIdLong()
    {
        return teamId;
    }

    @Override
    public String toString()
    {
        return "TeamMember(" + getTeamId() + ", " + user + ")";
    }
}

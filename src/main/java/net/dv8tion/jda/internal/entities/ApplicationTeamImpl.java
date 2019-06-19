package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.ApplicationTeam;
import net.dv8tion.jda.api.entities.TeamMember;

import javax.annotation.Nonnull;
import java.util.List;

public class ApplicationTeamImpl implements ApplicationTeam
{
    private final String iconId;
    private final List<TeamMember> members;
    private final long id;

    public ApplicationTeamImpl(String iconId, List<TeamMember> members, long id)
    {
        this.iconId = iconId;
        this.members = members;
        this.id = id;
    }

    @Override
    public String getIconId()
    {
        return iconId;
    }

    @Nonnull
    @Override
    public List<TeamMember> getMembers()
    {
        return members;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return "ApplicationTeam(" + getId() + ')';
    }
}

package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ApplicationTeam extends ISnowflake
{
    @Nullable
    String getIconId();

    @Nonnull
    List<? extends TeamMember> getMembers();
}

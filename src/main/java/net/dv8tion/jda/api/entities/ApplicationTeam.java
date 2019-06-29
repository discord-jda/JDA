/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Meta-data for the team of an application.
 */
public interface ApplicationTeam extends ISnowflake
{
    /** Template for {@link #getIconUrl()} */
    String ICON_URL = "https://cdn.discordapp.com/team-icons/%s/%s.png";

    /**
     * Searches for the {@link net.dv8tion.jda.api.entities.TeamMember TeamMember}
     * in {@link #getMembers()} that has the same user id as {@link #getOwnerIdLong()}.
     * <br>Its possible although unlikely that the owner of the team is not a member, in that case this will be null.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.TeamMember TeamMember} who owns the team
     */
    @Nullable
    default TeamMember getOwner()
    {
        long ownerId = getOwnerIdLong();
        for (TeamMember m : getMembers())
        {
            if (m.getUser().getIdLong() == ownerId)
                return m;
        }
        return null;
    }

    /**
     * The id for the user who owns this team.
     *
     * @return The owner id
     */
    @Nonnull
    default String getOwnerId()
    {
        return Long.toUnsignedString(getOwnerIdLong());
    }

    /**
     * The id for the user who owns this team.
     *
     * @return The owner id
     */
    long getOwnerIdLong();

    /**
     * The id hash for the icon of this team.
     *
     * @return The icon id, or null if no icon is applied
     *
     * @see    #getIconUrl()
     */
    @Nullable
    String getIconId();

    /**
     * The url for the icon of this team.
     *
     * @return The icon url, or null if no icon is applied
     */
    @Nullable
    default String getIconUrl()
    {
        String iconId = getIconId();
        return iconId == null ? null : String.format(ICON_URL, getId(), iconId);
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.TeamMember Team Members}.
     *
     * @return Immutable list of team members
     */
    @Nonnull
    List<TeamMember> getMembers();
}

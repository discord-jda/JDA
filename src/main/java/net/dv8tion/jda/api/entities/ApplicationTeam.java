/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Meta-data for the team of an application.
 *
 * @see ApplicationInfo#getTeam()
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
        return getMemberById(getOwnerIdLong());
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
     * Returns an {@link ImageProxy} for this application team's icon.
     *
     * @return The {@link ImageProxy} of this application team's icon, or null if no icon is applied
     *
     * @see    #getIconUrl()
     */
    @Nullable
    default ImageProxy getIcon()
    {
        final String iconUrl = getIconUrl();
        return iconUrl == null ? null : new ImageProxy(iconUrl);
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.TeamMember Team Members}.
     *
     * @return Immutable list of team members
     */
    @Nonnull
    List<TeamMember> getMembers();

    /**
     * Check whether {@link #getMember(User)} returns null for the provided user.
     *
     * @param  user
     *         The user to check
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return True, if the provided user is a member of this team
     */
    default boolean isMember(@Nonnull User user)
    {
        return getMember(user) != null;
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.api.entities.TeamMember TeamMember} instance
     * for the provided user. If the user is not a member of this team, null is returned.
     *
     * @param  user
     *         The user for the team member
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return The {@link net.dv8tion.jda.api.entities.TeamMember TeamMember} for the user or null
     */
    @Nullable
    default TeamMember getMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return getMemberById(user.getIdLong());
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.api.entities.TeamMember TeamMember} instance
     * for the provided user id. If the user is not a member of this team, null is returned.
     *
     * @param  userId
     *         The user id for the team member
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return The {@link net.dv8tion.jda.api.entities.TeamMember TeamMember} for the user or null
     */
    @Nullable
    default TeamMember getMemberById(@Nonnull String userId)
    {
        return getMemberById(MiscUtil.parseSnowflake(userId));
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.api.entities.TeamMember TeamMember} instance
     * for the provided user id. If the user is not a member of this team, null is returned.
     *
     * @param  userId
     *         The user id for the team member
     *
     * @return The {@link net.dv8tion.jda.api.entities.TeamMember TeamMember} for the user or null
     */
    @Nullable
    default TeamMember getMemberById(long userId)
    {
        for (TeamMember member : getMembers())
        {
            if (member.getUser().getIdLong() == userId)
                return member;
        }
        return null;
    }
}

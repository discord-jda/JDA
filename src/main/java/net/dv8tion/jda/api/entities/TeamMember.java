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

import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

/**
 * Member of a {@link net.dv8tion.jda.api.entities.ApplicationTeam}.
 *
 * @see ApplicationTeam#getMembers()
 * @see ApplicationTeam#getMember(User)
 * @see ApplicationTeam#getMemberById(long)
 */
public interface TeamMember
{
    /**
     * User for the team member.
     *
     * @return The user
     */
    @Nonnull
    User getUser();

    /**
     * The state of this member.
     * <br>Note: the API does not seem to provide members with {@link net.dv8tion.jda.api.entities.TeamMember.MembershipState#INVITED}
     * to bots.
     *
     * @return The {@link net.dv8tion.jda.api.entities.TeamMember.MembershipState}, or {@link net.dv8tion.jda.api.entities.TeamMember.MembershipState#UNKNOWN UNKNOWN}
     */
    @Nonnull
    MembershipState getMembershipState();

    /**
     * The role of this member.
     *
     * @return The {@link RoleType}, or {@link RoleType#UNKNOWN UNKNOWN}
     */
    @Nonnull
    RoleType getRoleType();

    /**
     * The id for the team this member belongs to.
     *
     * @return The team id.
     */
    @Nonnull
    default String getTeamId()
    {
        return Long.toUnsignedString(getTeamIdLong());
    }

    /**
     * The id for the team this member belongs to.
     *
     * @return The team id.
     */
    long getTeamIdLong();

    /**
     * The membership state on the team.
     */
    enum MembershipState
    {
        /** The user has a pending invite */
        INVITED(1),
        /** The user has accepted an invite as is a member of this team */
        ACCEPTED(2),
        /** Placeholder for future states */
        UNKNOWN(-1);

        private final int key;

        MembershipState(int key)
        {
            this.key = key;
        }

        /**
         * The key for this state that is used in the API.
         *
         * @return The key for this state
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Resolves the provided key to the correct MembershipState.
         *
         * @param  key
         *         The key to resolve
         *
         * @return The MembershipState, or {@link #UNKNOWN}
         */
        @Nonnull
        public static MembershipState fromKey(int key)
        {
            for (MembershipState state : values())
            {
                if (state.key == key)
                    return state;
            }
            return UNKNOWN;
        }
    }

    /**
     * The role in the team.
     */
    enum RoleType
    {
        /**
         * Owners are the most permissible role, and can take destructive,
         * irreversible actions like deleting team-owned apps or the team itself.
         *
         * <p>Teams are limited to one owner.
         */
        OWNER(""),
        /**
         * Admins have similar access as owners,
         * except they cannot take destructive actions on the team or team-owned apps.
         */
        ADMIN("admin"),
        /**
         * Members which can access information about team-owned apps, like the client secret or public key.
         * <br>They can also take limited actions on team-owned apps, like configuring interaction endpoints or resetting the bot token.
         * <br>Members with the Developer role cannot manage the team or its members, or take destructive actions on team-owned apps.
         */
        DEVELOPER("developer"),
        /**
         * Members which can access information about a team and any team-owned apps.
         * <br>Some examples include getting the IDs of applications and exporting payout records.
         * <br>Members can also invite bots associated with team-owned apps that are marked private.
         */
        READ_ONLY("read_only"),
        /**
         * Placeholder for future types
         */
        UNKNOWN("");

        private final String key;

        RoleType(String key)
        {
            this.key = key;
        }

        /**
         * The key for this role that is used in the API.
         *
         * @return The key for this role
         */
        @Nonnull
        public String getKey()
        {
            return key;
        }

        /**
         * Resolves the provided key to the correct RoleType.
         *
         * <p><b>Note:</b> {@link #OWNER} will never be returned, check the team owner ID instead.
         *
         * @param  key
         *         The key to resolve
         *
         * @return The RoleType, or {@link #UNKNOWN}
         */
        @Nonnull
        public static RoleType fromKey(@Nonnull String key)
        {
            Checks.notNull(key, "Key");
            if (key.isEmpty()) return UNKNOWN;

            for (RoleType state : values())
            {
                if (state.key.equals(key))
                    return state;
            }
            return UNKNOWN;
        }
    }
}

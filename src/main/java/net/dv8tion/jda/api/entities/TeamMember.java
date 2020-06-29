/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
     * Possibly-fake user for the team member.
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
}

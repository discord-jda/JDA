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
package net.dv8tion.jda.api.entities

import javax.annotation.Nonnull

/**
 * Member of a [net.dv8tion.jda.api.entities.ApplicationTeam].
 *
 * @see ApplicationTeam.getMembers
 * @see ApplicationTeam.getMember
 * @see ApplicationTeam.getMemberById
 */
interface TeamMember {
    @get:Nonnull
    val user: User

    @get:Nonnull
    val membershipState: MembershipState?

    @get:Nonnull
    val teamId: String?
        /**
         * The id for the team this member belongs to.
         *
         * @return The team id.
         */
        get() = java.lang.Long.toUnsignedString(teamIdLong)

    /**
     * The id for the team this member belongs to.
     *
     * @return The team id.
     */
    val teamIdLong: Long

    /**
     * The membership state on the team.
     */
    enum class MembershipState(
        /**
         * The key for this state that is used in the API.
         *
         * @return The key for this state
         */
        val key: Int
    ) {
        /** The user has a pending invite  */
        INVITED(1),

        /** The user has accepted an invite as is a member of this team  */
        ACCEPTED(2),

        /** Placeholder for future states  */
        UNKNOWN(-1);

        companion object {
            /**
             * Resolves the provided key to the correct MembershipState.
             *
             * @param  key
             * The key to resolve
             *
             * @return The MembershipState, or [.UNKNOWN]
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): MembershipState {
                for (state in entries) {
                    if (state.key == key) return state
                }
                return UNKNOWN
            }
        }
    }
}

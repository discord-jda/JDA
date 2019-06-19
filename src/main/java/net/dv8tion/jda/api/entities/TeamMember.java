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

public interface TeamMember
{
    @Nonnull
    User getUser();

    @Nonnull
    MembershipState getMembershipState();

    @Nonnull
    default String getTeamId()
    {
        return Long.toUnsignedString(getTeamIdLong());
    }

    long getTeamIdLong();

    enum MembershipState
    {
        INVITED(1),
        ACCEPTED(2),
        UNKNOWN(-1);

        private final int key;

        MembershipState(int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return key;
        }

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

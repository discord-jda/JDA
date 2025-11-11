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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.TeamMember;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.EntityString;

import java.util.Objects;

import javax.annotation.Nonnull;

public class TeamMemberImpl implements TeamMember {
    private final User user;
    private final MembershipState state;
    private final RoleType roleType;
    private final long teamId;

    public TeamMemberImpl(User user, MembershipState state, RoleType roleType, long teamId) {
        this.user = user;
        this.state = state;
        this.roleType = roleType;
        this.teamId = teamId;
    }

    @Nonnull
    @Override
    public User getUser() {
        return user;
    }

    @Nonnull
    @Override
    public MembershipState getMembershipState() {
        return state;
    }

    @Nonnull
    @Override
    public RoleType getRoleType() {
        return roleType;
    }

    @Override
    public long getTeamIdLong() {
        return teamId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, teamId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TeamMemberImpl)) {
            return false;
        }
        TeamMemberImpl member = (TeamMemberImpl) obj;
        return member.teamId == this.teamId && member.user.equals(this.user);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("teamId", getTeamId())
                .addMetadata("user", user)
                .toString();
    }
}

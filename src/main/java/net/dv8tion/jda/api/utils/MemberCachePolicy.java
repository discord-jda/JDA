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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;

public interface MemberCachePolicy
{
    MemberCachePolicy NONE = (member) -> false;
    MemberCachePolicy ALL = (member) -> true;
    MemberCachePolicy OWNER = Member::isOwner;
    MemberCachePolicy ONLINE = (member) -> member.getOnlineStatus() != OnlineStatus.OFFLINE && member.getOnlineStatus() != OnlineStatus.UNKNOWN;
    MemberCachePolicy VOICE = (member) -> {
        GuildVoiceState voiceState = member.getVoiceState();
        return voiceState != null && voiceState.getChannel() != null;
    };

    boolean cacheMember(Member member);

    @Nonnull
    default MemberCachePolicy or(@Nonnull MemberCachePolicy policy)
    {
        return (member) -> cacheMember(member) || policy.cacheMember(member);
    }

    @Nonnull
    default MemberCachePolicy and(@Nonnull MemberCachePolicy policy)
    {
        return (member) -> cacheMember(member) && policy.cacheMember(member);
    }

    @Nonnull
    static MemberCachePolicy any(@Nonnull MemberCachePolicy policy, @Nonnull MemberCachePolicy... policies)
    {
        for (MemberCachePolicy p : policies)
            policy = policy.or(p);
        return policy;
    }

    @Nonnull
    static MemberCachePolicy all(@Nonnull MemberCachePolicy policy, @Nonnull MemberCachePolicy... policies)
    {
        for (MemberCachePolicy p : policies)
            policy = policy.and(p);
        return policy;
    }
}

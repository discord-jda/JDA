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

public interface MemberCachePolicy
{
    MemberCachePolicy NONE = (member) -> false;
    MemberCachePolicy ALL = (member) -> true;
    MemberCachePolicy ONLINE = (member) -> member.getOnlineStatus() != OnlineStatus.OFFLINE && member.getOnlineStatus() != OnlineStatus.UNKNOWN;
    MemberCachePolicy VOICE = (member) -> {
        GuildVoiceState voiceState = member.getVoiceState();
        return voiceState != null && voiceState.getChannel() != null;
    };

    boolean cacheMember(Member member);
}

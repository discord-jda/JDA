/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.events.guild.voice;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.Member Member} was (un-)muted by a moderator.
 *
 * <p>Can be used to detect when a member is muted or un-muted by a moderator.
 */
public class GuildVoiceGuildMuteEvent extends GenericGuildVoiceEvent
{
    protected final boolean guildMuted;

    public GuildVoiceGuildMuteEvent(JDA api, long responseNumber, Member member)
    {
        super(api, responseNumber, member);
        this.guildMuted = member.getVoiceState().isGuildMuted();
    }

    /**
     * Whether the member was muted by a moderator in this event
     *
     * @return True, if a moderator muted this member,
     *         <br>False, if a moderator un-muted this member
     */
    public boolean isGuildMuted()
    {
        return guildMuted;
    }
}

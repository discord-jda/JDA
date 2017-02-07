/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.core.events.guild.member;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;

/**
 * <b><u>GenericGuildMemberEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.core.entities.Guild Guild} member causes an event.<br>
 * Every GuildMemberEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any GuildMemberEvent.
 */
public abstract class GenericGuildMemberEvent extends GenericGuildEvent
{
    private final Member member;

    public GenericGuildMemberEvent(JDA api, long responseNumber, Guild guild, Member member)
    {
        super(api, responseNumber, guild);
        this.member = member;
    }

    public Member getMember()
    {
        return member;
    }
}

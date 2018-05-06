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
package net.dv8tion.jda.core.events.guild.member;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.Guild Guild} member event is fired.
 * <br>Every GuildMemberEvent is an instance of this event and can be casted.
 *
 * <p>Can be used to detect any GuildMemberEvent.
 */
public abstract class GenericGuildMemberEvent extends GenericGuildEvent
{
    private final Member member;

    public GenericGuildMemberEvent(JDA api, long responseNumber, Member member)
    {
        super(api, responseNumber, member.getGuild());
        this.member = member;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.User User} instance
     * <br>Shortcut for {@code getMember().getUser()}
     *
     * @return The User instance
     */
    public User getUser()
    {
        return getMember().getUser();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Member Member} instance
     *
     * @return The Member instance
     */
    public Member getMember()
    {
        return member;
    }
}

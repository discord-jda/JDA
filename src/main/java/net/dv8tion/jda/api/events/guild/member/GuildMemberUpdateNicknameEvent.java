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

package net.dv8tion.jda.api.events.guild.member;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.UpdateEvent;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Member Member} updated their {@link net.dv8tion.jda.api.entities.Guild Guild} nickname.
 *
 * <p>Can be used to retrieve members who change their nickname, triggering guild, the old nick and the new nick.
 */
public class GuildMemberUpdateNicknameEvent extends GenericGuildMemberEvent implements UpdateEvent<Member, String>
{
    private final String oldNick, newNick;

    public GuildMemberUpdateNicknameEvent(JDA api, long responseNumber, Member member, String oldNick, String newNick)
    {
        super(api, responseNumber, member);
        this.oldNick = oldNick;
        this.newNick = newNick;
    }

    /**
     * The old nickname
     *
     * @return The old nickname
     */
    public String getOldNickname()
    {
        return getOldValue();
    }

    /**
     * The new nickname
     *
     * @return The new nickname
     */
    public String getNewNickname()
    {
        return getNewValue();
    }

    @Override
    public String getPropertyIdentifier()
    {
        return "nick";
    }

    @Override
    public Member getEntity()
    {
        return getMember();
    }

    @Override
    public String getOldValue()
    {
        return oldNick;
    }

    @Override
    public String getNewValue()
    {
        return newNick;
    }

    @Override
    public String toString()
    {
        return "MemberUpdate[" + getPropertyIdentifier() + "](" + getOldValue() + "->" + getNewValue() + ')';
    }
}

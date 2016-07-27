/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.events.guild.member;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;

/**
 * <b><u>GuildMemberNickChangeEvent</u></b><br/>
 * Fired if a {@link net.dv8tion.jda.entities.User User} updates their {@link net.dv8tion.jda.entities.Guild Guild} nickname.<br/>
 * <br/>
 * Use: Retrieve user who changed their nickname, triggering guild, the old nick and the new nick.
 */
public class GuildMemberNickChangeEvent extends GenericGuildMemberEvent
{
    private final String prevNick, newNick;

    public GuildMemberNickChangeEvent(JDA api, int responseNumber, Guild guild, User user, String prevNick, String newNick)
    {
        super(api, responseNumber, guild, user);
        this.prevNick = prevNick;
        this.newNick = newNick;
    }

    public String getPrevNick()
    {
        return prevNick;
    }

    public String getNewNick()
    {
        return newNick;
    }
}

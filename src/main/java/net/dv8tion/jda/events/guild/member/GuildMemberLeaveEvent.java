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
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;

import java.util.List;

/**
 * <b><u>GuildMemberLeaveEvent</u></b><br>
 * Fired if a {@link net.dv8tion.jda.entities.User User} leaves a {@link net.dv8tion.jda.entities.Guild Guild}.<br>
 * <br>
 * Use: Retrieve user who left (if available) and triggering guild.
 */
public class GuildMemberLeaveEvent extends GenericGuildMemberEvent
{

    private List<Role> oldRoles;
    private String oldNick;

    public GuildMemberLeaveEvent(JDA api, int responseNumber, Guild guild, User user, List<Role> oldRoles, String oldNick)
    {
        super(api, responseNumber, guild, user);
        this.oldRoles = oldRoles;
        this.oldNick = oldNick;
    }

    public List<Role> getOldRoles()
    {
        return oldRoles;
    }

    /**
     * Possibly null Nickname for {@link net.dv8tion.jda.entities.User User} who left.
     * @return Old nickname or null if none existed.
     */
    public String getOldNick()
    {
        return oldNick;
    }
}

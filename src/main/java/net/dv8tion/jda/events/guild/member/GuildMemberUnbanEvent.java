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
 * <b><u>GuildMemberBanEvent</u></b><br/>
 * Fired if a {@link net.dv8tion.jda.entities.User User} is unbanned from a {@link net.dv8tion.jda.entities.Guild Guild}.<br/>
 * <br/>
 * Use: Retrieve user who was unbanned (if available) and the guild which they were unbanned from.
 */
public class GuildMemberUnbanEvent extends GenericGuildMemberEvent
{

    public GuildMemberUnbanEvent(JDA api, int responseNumber, Guild guild, User user)
    {
        super(api, responseNumber, guild, user);
    }
}

/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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

public class GuildMemberUnbanEvent extends GenericGuildMemberEvent
{
    private final String userId;
    private final String userName;
    private final String userDiscriminator;

    public GuildMemberUnbanEvent(JDA api, int responseNumber, Guild guild, String userId, String userName, String userDiscriminator)
    {
        super(api, responseNumber, guild, null);
        this.userId = userId;
        this.userName = userName;
        this.userDiscriminator = userDiscriminator;
    }

    public String getUserId()
    {
        return userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getUserDiscriminator()
    {
        return userDiscriminator;
    }
}

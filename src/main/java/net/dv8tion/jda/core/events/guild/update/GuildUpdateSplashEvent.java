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

package net.dv8tion.jda.core.events.guild.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;

public class GuildUpdateSplashEvent extends GenericGuildUpdateEvent
{
    private final String oldSplashId;

    public GuildUpdateSplashEvent(JDA api, long responseNumber, Guild guild, String oldSplashId)
    {
        super(api, responseNumber, guild);
        this.oldSplashId = oldSplashId;
    }

    public String getOldSplashId()
    {
        return oldSplashId;
    }

    public String getOldSplashUrl()
    {
        return oldSplashId == null ? null : "https://cdn.discordapp.com/splashes/" + guild.getId() + "/" + oldSplashId + ".jpg";
    }
}

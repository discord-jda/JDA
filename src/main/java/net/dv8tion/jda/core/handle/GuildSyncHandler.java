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

package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.json.JSONArray;
import org.json.JSONObject;

public class GuildSyncHandler extends SocketHandler
{
    public GuildSyncHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long guildId = content.getLong("id");
        if (!api.getGuildMap().containsKey(guildId))
        {
            JDAImpl.LOG.error("Received a GUILD_SYNC for a Guild that does not yet exist in JDA's guild cache. This is a BAD ERROR FOR CLIENTS!");
            return null;
        }

        GuildImpl guild = (GuildImpl) api.getGuildMap().get(guildId);
        JSONArray members = content.getJSONArray("members");
        JSONArray presences = content.getJSONArray("presences");
        api.getEntityBuilder().handleGuildSync(guild, members, presences);

        return null;
    }
}

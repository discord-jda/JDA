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

import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.core.utils.JDALogger;
import org.json.JSONObject;

public class GuildBanHandler extends SocketHandler
{
    private final boolean banned;

    public GuildBanHandler(JDAImpl api, boolean banned)
    {
        super(api);
        this.banned = banned;
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long id = content.getLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(id))
            return id;

        JSONObject userJson = content.getJSONObject("user");
        GuildImpl guild = (GuildImpl) getJDA().getGuildMap().get(id);
        if (guild == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.GUILD, id, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received Guild Member {} event for a Guild not yet cached.", JDALogger.getLazyString(() -> banned ? "Ban" : "Unban"));
            return null;
        }

        User user = getJDA().getEntityBuilder().createFakeUser(userJson, false);

        if (banned)
        {
            getJDA().getEventManager().handle(
                    new GuildBanEvent(
                            getJDA(), responseNumber,
                            guild, user));
        }
        else
        {
            getJDA().getEventManager().handle(
                    new GuildUnbanEvent(
                            getJDA(), responseNumber,
                            guild, user));
        }
        return null;
    }
}

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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONObject;

public abstract class SocketHandler
{
    protected final JDAImpl api;
    protected final int responseNumber;
    protected JSONObject allContent;

    public SocketHandler(JDAImpl api, int responseNumber)
    {
        this.api = api;
        this.responseNumber = responseNumber;
    }


    public final void handle(JSONObject o)
    {
        this.allContent = o;
        String guildId = handleInternally(o.getJSONObject("d"));
        if (guildId != null)
        {
            GuildLock.get(api).queue(guildId, o);
        }
    }

    /**
     * Handles a given data-json of the Event handled by this Handler.
     * @param content
     *      the content of the event to handle
     * @return
     *      Guild-id if that guild has a lock, or null if successful
     */
    protected abstract String handleInternally(JSONObject content);
}
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

import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.utils.cache.UpstreamReference;
import org.json.JSONObject;

public abstract class SocketHandler
{
    protected final UpstreamReference<JDAImpl> api;
    protected long responseNumber;
    protected JSONObject allContent;

    public SocketHandler(JDAImpl api)
    {
        this.api = new UpstreamReference<>(api);
    }

    public final synchronized void handle(long responseTotal, JSONObject o)
    {
        this.allContent = o;
        this.responseNumber = responseTotal;
        final Long guildId = handleInternally(o.getJSONObject("d"));
        if (guildId != null)
            getJDA().getGuildSetupController().cacheEvent(guildId, o);
        this.allContent = null;
    }

    protected JDAImpl getJDA()
    {
        return api.get();
    }

    /**
     * Handles a given data-json of the Event handled by this Handler.
     * @param content
     *      the content of the event to handle
     * @return
     *      Guild-id if that guild has a lock, or null if successful
     */
    protected abstract Long handleInternally(JSONObject content);

    public static class NOPHandler extends SocketHandler
    {
        public NOPHandler(JDAImpl api)
        {
            super(api);
        }

        @Override
        protected Long handleInternally(JSONObject content)
        {
            return null;
        }
    }
}

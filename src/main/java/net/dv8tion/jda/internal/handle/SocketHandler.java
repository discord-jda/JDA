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
package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public abstract class SocketHandler
{
    protected final JDAImpl api;
    protected long responseNumber;
    protected DataObject allContent;

    public SocketHandler(JDAImpl api)
    {
        this.api = api;
    }

    public final synchronized void handle(long responseTotal, DataObject o)
    {
        this.allContent = o;
        this.responseNumber = responseTotal;
        final Long guildId = handleInternally(o.getObject("d"));
        if (guildId != null)
            getJDA().getGuildSetupController().cacheEvent(guildId, o);
        this.allContent = null;
    }

    protected JDAImpl getJDA()
    {
        return api;
    }

    /**
     * Handles a given data-json of the Event handled by this Handler.
     * @param content
     *      the content of the event to handle
     * @return
     *      Guild-id if that guild has a lock, or null if successful
     */
    protected abstract Long handleInternally(DataObject content);

    public static class NOPHandler extends SocketHandler
    {
        public NOPHandler(JDAImpl api)
        {
            super(api);
        }

        @Override
        protected Long handleInternally(DataObject content)
        {
            return null;
        }
    }
}

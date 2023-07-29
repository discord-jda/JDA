/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;

public class ThreadCreateHandler extends SocketHandler
{
    public ThreadCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        long guildId = content.getLong("guild_id");
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;

        try
        {
            //Prevent possible duplicate events or unexpected situations of threads being revealed
            // after already being known due to permissions
            if (api.getThreadChannelById(content.getUnsignedLong("id")) != null)
                return null;
            ThreadChannel thread = api.getEntityBuilder().createThreadChannel(content, guildId);

            if (content.getBoolean("newly_created"))
                api.handleEvent(new ChannelCreateEvent(api, responseNumber, thread));
            else
                api.handleEvent(new ThreadRevealedEvent(api, responseNumber, thread));
        }
        catch (IllegalArgumentException ex)
        {
            if (!EntityBuilder.MISSING_CHANNEL.equals(ex.getMessage()))
                throw ex;

            long parentId = content.getUnsignedLong("parent_id", 0L);
            EventCache.LOG.debug("Caching THREAD_CREATE_EVENT for channel with uncached parent. Parent ID: {}", parentId);
            api.getEventCache().cache(EventCache.Type.CHANNEL, parentId, responseNumber, allContent, this::handle);
        }

        return null;
    }
}

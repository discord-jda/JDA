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

import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

public class ThreadDeleteHandler extends SocketHandler
{
    public ThreadDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        long guildId = content.getLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;

        GuildImpl guild = (GuildImpl) getJDA().getGuildById(guildId);
        final long threadId = content.getLong("id");

        ThreadChannel thread = getJDA().getThreadChannelsView().remove(threadId);
        if (thread == null || guild == null)
        {
            WebSocketClient.LOG.debug("THREAD_DELETE attempted to delete a thread that is not yet cached. JSON: {}", content);
            return null;
        }

        guild.getThreadChannelsView().remove(threadId);

        getJDA().handleEvent(
            new ChannelDeleteEvent(
                getJDA(), responseNumber,
                thread));

        getJDA().getEventCache().clear(EventCache.Type.CHANNEL, threadId);
        return null;
    }
}

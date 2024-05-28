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

import net.dv8tion.jda.api.events.channel.update.ChannelUpdateVoiceStatusEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.VoiceChannelImpl;

public class VoiceChannelStatusUpdateHandler extends SocketHandler
{
    public VoiceChannelStatusUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        long guildId = content.getUnsignedLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;

        long id = content.getUnsignedLong("id");
        VoiceChannelImpl channel = (VoiceChannelImpl) getJDA().getVoiceChannelById(id);

        if (channel == null)
        {
            EventCache.LOG.debug("Caching VOICE_CHANNEL_STATUS_UPDATE for uncached channel. ID: {}", id);
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, id, responseNumber, allContent, this::handle);
            return null;
        }

        String newStatus = content.getString("status", "");
        if (!newStatus.equals(channel.getStatus()))
        {
            String oldStatus = channel.getStatus();
            channel.setStatus(newStatus);
            api.handleEvent(
                new ChannelUpdateVoiceStatusEvent(
                    api, responseNumber,
                    channel, oldStatus, newStatus));
        }
        return null;
    }
}

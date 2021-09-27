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

import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdatePrivacyLevelEvent;
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdateTopicEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;

import java.util.Objects;

public class StageInstanceUpdateHandler extends SocketHandler
{
    public StageInstanceUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        long guildId = content.getUnsignedLong("guild_id", 0L);
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;

        GuildImpl guild = (GuildImpl) getJDA().getGuildById(guildId);
        if (guild == null)
        {
            EventCache.LOG.debug("Caching STAGE_INSTANCE_UPDATE for uncached guild with id {}", guildId);
            getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            return null;
        }

        StageChannel channel = getJDA().getStageChannelById(content.getUnsignedLong("channel_id"));
        if (channel == null)
            return null;
        StageInstance oldInstance = channel.getStageInstance();
        if (oldInstance == null)
            return null;

        String oldTopic = oldInstance.getTopic();
        StageInstance.PrivacyLevel oldLevel = oldInstance.getPrivacyLevel();
        StageInstance newInstance = getJDA().getEntityBuilder().createStageInstance(guild, content);
        if (newInstance == null)
            return null;

        if (!Objects.equals(oldTopic, newInstance.getTopic()))
            getJDA().handleEvent(new StageInstanceUpdateTopicEvent(getJDA(), responseNumber, newInstance, oldTopic));
        if (oldLevel != newInstance.getPrivacyLevel())
            getJDA().handleEvent(new StageInstanceUpdatePrivacyLevelEvent(getJDA(), responseNumber, newInstance, oldLevel));
        return null;
    }
}

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

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.*;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.GuildScheduledEventImpl;

import java.time.OffsetDateTime;
import java.util.*;

public class GuildScheduledEventUpdateHandler extends SocketHandler
{
    public GuildScheduledEventUpdateHandler(JDAImpl api)
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
            EventCache.LOG.debug("Caching SCHEDULED_EVENT_UPDATE for uncached guild with id {}", guildId);
            getJDA().getEventCache().cache(EventCache.Type.GUILD_SCHEDULED_EVENT, guildId, responseNumber, allContent, this::handle);
            return null;
        }

        GuildScheduledEventImpl event = (GuildScheduledEventImpl) guild.getScheduledEventById(content.getUnsignedLong("id"));
        if (event == null)
            event = (GuildScheduledEventImpl) api.getEntityBuilder().createGuildScheduledEvent(guild, content, guildId);

        final String name = content.getString("name");
        final String description = content.getString("description", null);
        final OffsetDateTime startTime = content.getOffsetDateTime("scheduled_start_time");
        final OffsetDateTime endTime = content.getOffsetDateTime("scheduled_end_time", null);
        final GuildScheduledEvent.Status status = GuildScheduledEvent.Status.fromKey(content.getInt("status", -1));
        final String imageUrl = content.getString("image", null);
        String location = content.getString("channel_id", null);
        GuildChannel channel = null;
        if (location == null)
            location = content.getObject("entity_metadata").getString("location");
        else
            channel = guild.getGuildChannelById(location);


        if (!Objects.equals(name, event.getName()))
        {
            String oldName = event.getName();
            event.setName(name);
            getJDA().handleEvent(new GuildScheduledEventUpdateNameEvent(getJDA(), responseNumber, event, oldName));
        }
        if (!Objects.equals(description, event.getDescription()))
        {
            String oldDescription = event.getDescription();
            event.setDescription(description);
            getJDA().handleEvent(new GuildScheduledEventUpdateDescriptionEvent(getJDA(), responseNumber, event, oldDescription));
        }
        if (!Objects.equals(startTime, event.getStartTime()))
        {
            OffsetDateTime oldStartTime = event.getStartTime();
            event.setStartTime(startTime);
            getJDA().handleEvent(new GuildScheduledEventUpdateStartTimeEvent(getJDA(), responseNumber, event, oldStartTime));
        }
        if (!Objects.equals(endTime, event.getEndTime()))
        {
            OffsetDateTime oldEndTime = event.getEndTime();
            event.setEndTime(endTime);
            getJDA().handleEvent(new GuildScheduledEventUpdateEndTimeEvent(getJDA(), responseNumber, event, oldEndTime));
        }
        if (!Objects.equals(status, event.getStatus()))
        {
            GuildScheduledEvent.Status oldStatus = event.getStatus();
            event.setStatus(status);
            getJDA().handleEvent(new GuildScheduledEventUpdateStatusEvent(getJDA(), responseNumber, event, oldStatus));
        }
        if (channel == null && !Objects.equals(location, event.getExternalLocation()))
        {
            GuildScheduledEventUpdateLocationEvent.Location oldLocation = new GuildScheduledEventUpdateLocationEvent.Location(event);
            event.setStageChannel(null);
            event.setVoiceChannel(null);
            event.setExternalLocation(location);
            getJDA().handleEvent(new GuildScheduledEventUpdateLocationEvent(getJDA(), responseNumber, event, oldLocation));
        }
        if (channel instanceof StageChannel && !Objects.equals(channel, event.getStageChannel()))
        {
            GuildScheduledEventUpdateLocationEvent.Location oldLocation = new GuildScheduledEventUpdateLocationEvent.Location(event);
            event.setVoiceChannel(null);
            event.setExternalLocation(null);
            event.setStageChannel((StageChannel) channel);
            getJDA().handleEvent(new GuildScheduledEventUpdateLocationEvent(getJDA(), responseNumber, event, oldLocation));
        }
        if (channel instanceof VoiceChannel && !Objects.equals(channel, event.getVoiceChannel()))
        {
            GuildScheduledEventUpdateLocationEvent.Location oldLocation = new GuildScheduledEventUpdateLocationEvent.Location(event);
            event.setStageChannel(null);
            event.setExternalLocation(null);
            event.setVoiceChannel((VoiceChannel) channel);
            getJDA().handleEvent(new GuildScheduledEventUpdateLocationEvent(getJDA(), responseNumber, event, oldLocation));
        }
        if (!Objects.equals(imageUrl, event.getImageUrl()))
        {
            String oldImageUrl = event.getImageUrl();
            event.setImage(imageUrl);
            getJDA().handleEvent(new GuildScheduledEventUpdateDescriptionEvent(getJDA(), responseNumber, event, oldImageUrl));
        }
        return null;
    }
}

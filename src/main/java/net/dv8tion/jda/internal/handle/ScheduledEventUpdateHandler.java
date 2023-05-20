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

import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.*;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.ScheduledEventImpl;

import java.time.OffsetDateTime;
import java.util.Objects;

public class ScheduledEventUpdateHandler extends SocketHandler
{
    public ScheduledEventUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        if (!getJDA().isCacheFlagSet(CacheFlag.SCHEDULED_EVENTS))
            return null;
        long guildId = content.getUnsignedLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;

        GuildImpl guild = (GuildImpl) getJDA().getGuildById(guildId);
        if (guild == null)
        {
            EventCache.LOG.debug("Caching SCHEDULED_EVENT_UPDATE for uncached guild with id {}", guildId);
            getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            return null;
        }

        ScheduledEventImpl event = (ScheduledEventImpl) guild.getScheduledEventById(content.getUnsignedLong("id"));
        if (event == null)
        {
            api.getEntityBuilder().createScheduledEvent(guild, content);
            return null;
        }

        final String name = content.getString("name");
        final String description = content.getString("description", null);
        final OffsetDateTime startTime = content.getOffsetDateTime("scheduled_start_time");
        final OffsetDateTime endTime = content.getOffsetDateTime("scheduled_end_time", null);
        final ScheduledEvent.Status status = ScheduledEvent.Status.fromKey(content.getInt("status", -1));
        final String imageUrl = content.getString("image", null);
        String location = content.getString("channel_id", null);
        GuildChannel channel = null;
        String oldLocation = event.getLocation();

        if (location != null)
            channel = guild.getGuildChannelById(location);
        else // null in some cases due to discord validation bugs
            location = content.optObject("entity_metadata").map(o -> o.getString("location", "")).orElse("");

        if (!Objects.equals(name, event.getName()))
        {
            String oldName = event.getName();
            event.setName(name);
            getJDA().handleEvent(
                new ScheduledEventUpdateNameEvent(getJDA(), responseNumber,
                    event, oldName));
        }
        if (!Objects.equals(description, event.getDescription()))
        {
            String oldDescription = event.getDescription();
            event.setDescription(description);
            getJDA().handleEvent(new ScheduledEventUpdateDescriptionEvent(getJDA(), responseNumber, event, oldDescription));
        }
        if (!Objects.equals(startTime, event.getStartTime()))
        {
            OffsetDateTime oldStartTime = event.getStartTime();
            event.setStartTime(startTime);
            getJDA().handleEvent(new ScheduledEventUpdateStartTimeEvent(getJDA(), responseNumber, event, oldStartTime));
        }
        if (!Objects.equals(endTime, event.getEndTime()))
        {
            OffsetDateTime oldEndTime = event.getEndTime();
            event.setEndTime(endTime);
            getJDA().handleEvent(new ScheduledEventUpdateEndTimeEvent(getJDA(), responseNumber, event, oldEndTime));
        }
        if (!Objects.equals(status, event.getStatus()))
        {
            ScheduledEvent.Status oldStatus = event.getStatus();
            event.setStatus(status);
            getJDA().handleEvent(new ScheduledEventUpdateStatusEvent(getJDA(), responseNumber, event, oldStatus));
        }
        if (channel == null && !location.equals(event.getLocation()))
        {
            event.setLocation(location);
            event.setType(ScheduledEvent.Type.EXTERNAL);
            getJDA().handleEvent(new ScheduledEventUpdateLocationEvent(getJDA(), responseNumber, event, oldLocation));
        }
        if (channel instanceof StageChannel && !location.equals(event.getLocation()))
        {
            event.setLocation(channel.getId());
            event.setType(ScheduledEvent.Type.STAGE_INSTANCE);
            getJDA().handleEvent(new ScheduledEventUpdateLocationEvent(getJDA(), responseNumber, event, oldLocation));
        }
        if (channel instanceof VoiceChannel && !location.equals(event.getLocation()))
        {
            event.setLocation(channel.getId());
            event.setType(ScheduledEvent.Type.VOICE);
            getJDA().handleEvent(new ScheduledEventUpdateLocationEvent(getJDA(), responseNumber, event, oldLocation));
        }
        if (!Objects.equals(imageUrl, event.getImageUrl()))
        {
            String oldImageUrl = event.getImageUrl();
            event.setImage(imageUrl);
            getJDA().handleEvent(new ScheduledEventUpdateImageEvent(getJDA(), responseNumber, event, oldImageUrl));
        }
        return null;
    }
}

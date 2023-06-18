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

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.ScheduledEventManager;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class ScheduledEventManagerImpl extends ManagerBase<ScheduledEventManager> implements ScheduledEventManager
{
    protected ScheduledEvent event;
    protected String name, description;
    protected long channelId;
    protected String location;
    protected Icon image;
    protected OffsetDateTime startTime, endTime;
    protected ScheduledEvent.Type entityType;
    protected ScheduledEvent.Status status;

    public ScheduledEventManagerImpl(ScheduledEvent event)
    {
        super(event.getJDA(), Route.Guilds.MODIFY_SCHEDULED_EVENT.compile(event.getGuild().getId(), event.getId()));
        this.event = event;
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    @Nonnull
    @Override
    public ScheduledEvent getScheduledEvent()
    {
        ScheduledEvent realEvent = event.getGuild().getScheduledEventById(event.getIdLong());
        if (realEvent != null)
            event = realEvent;
        return event;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ScheduledEventManagerImpl setName(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        Checks.notLonger(name, ScheduledEvent.MAX_NAME_LENGTH, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Nonnull
    @Override
    public ScheduledEventManager setDescription(@Nullable String description)
    {
        Checks.notLonger(description, ScheduledEvent.MAX_DESCRIPTION_LENGTH, "Description");
        this.description = description;
        set |= DESCRIPTION;
        return this;
    }

    @Nonnull
    @Override
    public ScheduledEventManager setImage(@Nullable Icon icon)
    {
        this.image = icon;
        set |= IMAGE;
        return this;
    }

    @Nonnull
    @Override
    public ScheduledEventManager setLocation(@Nonnull GuildChannel channel)
    {
        Checks.notNull(channel, "Channel");
        if (!channel.getGuild().equals(event.getGuild()))
        {
            throw new IllegalArgumentException("Invalid parameter: Channel has to be from the same guild as the scheduled event!");
        }
        else if (channel instanceof StageChannel)
        {
            this.channelId = channel.getIdLong();
            this.entityType = ScheduledEvent.Type.STAGE_INSTANCE;
        }
        else if (channel instanceof VoiceChannel)
        {
            this.channelId = channel.getIdLong();
            this.entityType = ScheduledEvent.Type.VOICE;
        }
        else
        {
            throw new IllegalArgumentException("Invalid parameter: Can only set location to Voice and Stage Channels!");
        }

        set |= LOCATION;
        return this;
    }

    @Nonnull
    @Override
    public ScheduledEventManager setLocation(@Nonnull String location)
    {
        Checks.notBlank(location, "Location");
        Checks.notLonger(location, ScheduledEvent.MAX_LOCATION_LENGTH, "Location");
        this.location = location;
        this.entityType = ScheduledEvent.Type.EXTERNAL;
        set |= LOCATION;
        return this;
    }

    @Nonnull
    @Override
    public ScheduledEventManager setStartTime(@Nonnull TemporalAccessor startTime)
    {
        Checks.notNull(startTime, "Start Time");
        OffsetDateTime offsetStartTime = Helpers.toOffsetDateTime(startTime);
        Checks.check(offsetStartTime.isAfter(OffsetDateTime.now()), "Cannot schedule event in the past!");
        Checks.check(offsetStartTime.isBefore(OffsetDateTime.now().plusYears(5)), "Scheduled start and end times must be within five years.");
        this.startTime = offsetStartTime;
        set |= START_TIME;
        return this;
    }

    @Nonnull
    @Override
    public ScheduledEventManager setEndTime(@Nonnull TemporalAccessor endTime)
    {
        Checks.notNull(endTime, "End Time");
        OffsetDateTime offsetEndTime = Helpers.toOffsetDateTime(endTime);
        Checks.check(offsetEndTime.isBefore(OffsetDateTime.now().plusYears(5)), "Scheduled start and end times must be within five years.");
        this.endTime = offsetEndTime;
        set |= END_TIME;
        return this;
    }

    @Nonnull
    @Override
    public ScheduledEventManager setStatus(@Nonnull ScheduledEvent.Status status)
    {
        Checks.notNull(status, "Status");
        Checks.check(status != ScheduledEvent.Status.UNKNOWN, "Cannot set the event status to an unknown status!");
        Checks.check(status != ScheduledEvent.Status.SCHEDULED && getScheduledEvent().getStatus() != ScheduledEvent.Status.ACTIVE, "Cannot perform status update!");
        this.status = status;
        set |= STATUS;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        preChecks();
        DataObject object = DataObject.empty();
        if (shouldUpdate(NAME))
            object.put("name", name);
        if (shouldUpdate(DESCRIPTION))
            object.put("description", description);
        if (shouldUpdate(LOCATION))
        {
            object.put("entity_type", entityType.getKey());
            switch (entityType)
            {
            case STAGE_INSTANCE:
            case VOICE:
                object.putNull("entity_metadata");
                object.put("channel_id", channelId);
                break;
            case EXTERNAL:
                object.put("entity_metadata", DataObject.empty().put("location", location));
                object.put("channel_id", null);
                break;
            default:
                throw new IllegalStateException("ScheduledEventType " + entityType + " is not supported!");
            }
        }
        if (shouldUpdate(START_TIME))
            object.put("scheduled_start_time", startTime.format(DateTimeFormatter.ISO_DATE_TIME));
        if (shouldUpdate(END_TIME))
            object.put("scheduled_end_time", endTime.format(DateTimeFormatter.ISO_DATE_TIME));
        if (shouldUpdate(IMAGE))
            object.put("image", image != null ? image.getEncoding() : null);
        if (shouldUpdate(STATUS))
            object.put("status", status.getKey());

        return getRequestBody(object);
    }

    private void preChecks()
    {
        if (shouldUpdate(LOCATION))
        {
            Checks.check(getScheduledEvent().getStatus() == ScheduledEvent.Status.SCHEDULED || (entityType == ScheduledEvent.Type.EXTERNAL && getScheduledEvent().getType() == ScheduledEvent.Type.EXTERNAL), "Cannot update location type or location channel of non-scheduled event.");
            if (entityType == ScheduledEvent.Type.EXTERNAL && endTime == null && getScheduledEvent().getEndTime() == null)
                throw new IllegalStateException("Missing required parameter: End Time");
        }

        if (shouldUpdate(START_TIME))
        {
            Checks.check(getScheduledEvent().getStatus() == ScheduledEvent.Status.SCHEDULED, "Cannot update start time of non-scheduled event!");
            Checks.check((endTime == null && getScheduledEvent().getEndTime() == null) || (endTime == null ? getScheduledEvent().getEndTime() : endTime).isAfter(startTime), "Cannot schedule event to end before starting!");
        }

        if (shouldUpdate(END_TIME))
            Checks.check((startTime == null ? getScheduledEvent().getStartTime() : startTime).isBefore(endTime), "Cannot schedule event to end before starting!");
    }
}

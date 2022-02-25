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

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.GuildScheduledEventManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


public class GuildScheduledEventManagerImpl extends ManagerBase<GuildScheduledEventManager> implements GuildScheduledEventManager
{
    protected GuildScheduledEvent event;
    protected String name, description;
    protected long channelId;
    protected String location;
    protected Icon image;
    protected OffsetDateTime startTime, endTime;
    protected int entityType;
    protected GuildScheduledEvent.Status status;

    public GuildScheduledEventManagerImpl(GuildScheduledEvent event)
    {
        super(event.getJDA(), Route.Guilds.MODIFY_SCHEDULED_EVENT.compile(event.getGuild().getId(), event.getId()));
        this.event = event;
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    @Nonnull
    @Override
    public GuildScheduledEvent getGuildScheduledEvent()
    {
        GuildScheduledEvent realEvent = event.getGuild().getScheduledEventById(event.getIdLong());
        if (realEvent != null)
            event = realEvent;
        return event;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildScheduledEventManagerImpl setName(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, GuildScheduledEvent.MAX_NAME_LENGTH, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @NotNull
    @Override
    public GuildScheduledEventManager setDescription(@NotNull String description)
    {
        Checks.notBlank(description, "Description");
        description = description.trim();
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, GuildScheduledEvent.MAX_DESCRIPTION_LENGTH, "Description");
        this.description = description;
        set |= DESCRIPTION;
        return this;
    }

    @NotNull
    @Override
    public GuildScheduledEventManager setImage(@NotNull Icon icon)
    {
        this.image = icon;
        set |= IMAGE;
        return this;
    }

    @NotNull
    @Override
    public GuildScheduledEventManager setLocation(@NotNull StageChannel stageChannel)
    {
        this.channelId = stageChannel.getIdLong();
        this.entityType = 1;
        set |= LOCATION;
        return this;
    }

    @NotNull
    @Override
    public GuildScheduledEventManager setLocation(@NotNull VoiceChannel voiceChannel)
    {
        this.channelId = voiceChannel.getIdLong();
        this.entityType = 2;
        set |= LOCATION;
        return this;
    }

    @NotNull
    @Override
    public GuildScheduledEventManager setLocation(@NotNull String externalLocation)
    {
        this.location = externalLocation;
        this.entityType = 3;
        set |= LOCATION;
        return this;
    }

    @NotNull
    @Override
    public GuildScheduledEventManager setStartTime(@NotNull OffsetDateTime startTime)
    {
        this.startTime = startTime;
        set |= START_TIME;
        return this;
    }

    @NotNull
    @Override
    public GuildScheduledEventManager setEndTime(@Nullable OffsetDateTime endTime)
    {
        this.endTime = endTime;
        set |= END_TIME;
        return this;
    }

    @NotNull
    @Override
    public GuildScheduledEventManager setStatus(@NotNull GuildScheduledEvent.Status status)
    {
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
            object.put("entity_type", entityType);
            if (this.entityType == 1 || this.entityType == 2)
                object.put("channel_id", channelId);
            else if (this.entityType == 3)
            {
                object.put("entity_metadata", DataObject.empty().put("location", location));
                object.put("channel_id", null);
            }
        }
        if (shouldUpdate(START_TIME))
            object.put("scheduled_start_time", startTime.format(DateTimeFormatter.ISO_DATE_TIME));
        if (shouldUpdate(END_TIME))
            object.put("scheduled_end_time", endTime.format(DateTimeFormatter.ISO_DATE_TIME));
        if (shouldUpdate(IMAGE))
            object.put("image", image.getEncoding());
        if (shouldUpdate(STATUS))
            object.put("status", status.getKey());

        return getRequestBody(object);
    }

    void preChecks()
    {
        if (shouldUpdate(LOCATION))
        {
            Checks.check(getGuildScheduledEvent().getStatus() == GuildScheduledEvent.Status.SCHEDULED, "Cannot update location of non-scheduled event.");
            Checks.check(this.entityType != 3 || (location != null && location.length() != 0), "Missing required parameter: Location");
            Checks.check(this.entityType != 3 || endTime != null || getGuildScheduledEvent().getEndTime() != null, "Missing required parameter: End Time");
        }

        if (shouldUpdate(START_TIME))
        {
            Checks.check(getGuildScheduledEvent().getStatus() == GuildScheduledEvent.Status.SCHEDULED, "Cannot update start time of non-scheduled event!");
            Checks.check((this.endTime == null && getGuildScheduledEvent().getEndTime() == null) || (this.endTime == null ? getGuildScheduledEvent().getEndTime() : this.endTime).isAfter(startTime), "Cannot schedule event to end before starting!");
        }

        if (shouldUpdate(END_TIME))
            Checks.check((this.startTime == null ? getGuildScheduledEvent().getStartTime() : this.startTime).isBefore(endTime), "Cannot schedule event to end before starting!");

        if (shouldUpdate(STATUS))
        {
            Checks.check(this.status != GuildScheduledEvent.Status.UNKNOWN, "Cannot set the event status to an unknown status!");
            Checks.check(this.status != GuildScheduledEvent.Status.SCHEDULED && getGuildScheduledEvent().getStatus() != GuildScheduledEvent.Status.ACTIVE, "Cannot perform status update!");
        }
    }
}

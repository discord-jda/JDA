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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.ScheduledEventAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class ScheduledEventActionImpl extends AuditableRestActionImpl<ScheduledEvent> implements ScheduledEventAction
{
    protected final Guild guild;
    protected String name, description;
    protected Icon image;
    protected long channelId;
    protected String location;
    protected OffsetDateTime startTime, endTime;
    protected final ScheduledEvent.Type entityType;

    public ScheduledEventActionImpl(String name, String location, TemporalAccessor startTime, TemporalAccessor endTime, Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.CREATE_SCHEDULED_EVENT.compile(guild.getId()));
        this.guild = guild;
        setName(name);
        setStartTime(startTime);
        setEndTime(endTime);
        Checks.notNull(location, "Location");
        Checks.notBlank(location, "Location");
        Checks.notEmpty(location, "Location");
        Checks.notLonger(location, ScheduledEvent.MAX_LOCATION_LENGTH, "Location");
        this.location = location;
        this.entityType = ScheduledEvent.Type.EXTERNAL;
    }

    public ScheduledEventActionImpl(String name, GuildChannel channel, TemporalAccessor startTime, Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.CREATE_SCHEDULED_EVENT.compile(guild.getId()));
        this.guild = guild;
        setName(name);
        setStartTime(startTime);
        Checks.notNull(channel, "Channel");
        if (!channel.getGuild().equals(guild))
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
    }

    @Nonnull
    @Override
    public ScheduledEventActionImpl setCheck(BooleanSupplier checks)
    {
        return (ScheduledEventActionImpl) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public ScheduledEventActionImpl timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (ScheduledEventActionImpl) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public ScheduledEventActionImpl deadline(long timestamp)
    {
        return (ScheduledEventActionImpl) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public ScheduledEventActionImpl reason(@Nullable String reason)
    {
        return (ScheduledEventActionImpl) super.reason(reason);
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nonnull
    @Override
    public ScheduledEventActionImpl setName(@Nullable String name)
    {
        Checks.notBlank(name, "Name");
        Checks.notLonger(name, ScheduledEvent.MAX_NAME_LENGTH, "Name");
        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ScheduledEventActionImpl setDescription(@Nullable String description)
    {
        if (description != null)
            Checks.notLonger(description, ScheduledEvent.MAX_DESCRIPTION_LENGTH, "Description");
        this.description = description;
        return this;
    }

    @Nonnull
    @Override
    public ScheduledEventAction setStartTime(@Nonnull TemporalAccessor startTime)
    {
        Checks.notNull(startTime, "Start Time");
        OffsetDateTime offsetStartTime = Helpers.toOffsetDateTime(startTime);
        Checks.check(offsetStartTime.isAfter(OffsetDateTime.now()), "Cannot schedule event in the past!");
        Checks.check(offsetStartTime.isBefore(OffsetDateTime.now().plusYears(5)), "Scheduled start and end times must be within five years.");
        this.startTime = offsetStartTime;
        return this;
    }

    @Nonnull
    @Override
    public ScheduledEventAction setEndTime(@Nullable TemporalAccessor endTime)
    {
        Checks.notNull(endTime, "End Time");
        OffsetDateTime offsetEndTime = Helpers.toOffsetDateTime(endTime);
        Checks.check(offsetEndTime.isAfter(startTime), "Cannot schedule event to end before its starting!");
        Checks.check(offsetEndTime.isBefore(OffsetDateTime.now().plusYears(5)), "Scheduled start and end times must be within five years.");
        this.endTime = offsetEndTime;
        return this;
    }

    @Nonnull
    @Override
    public ScheduledEventAction setImage(@Nullable Icon icon)
    {
        this.image = icon;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject object = DataObject.empty();
        object.put("entity_type", entityType.getKey());
        object.put("privacy_level", 2);
        object.put("name", name);
        object.put("scheduled_start_time", startTime.format(DateTimeFormatter.ISO_DATE_TIME));

        switch (entityType)
        {
        case STAGE_INSTANCE:
        case VOICE:
            object.put("channel_id", channelId);
            break;
        case EXTERNAL:
            object.put("entity_metadata", DataObject.empty().put("location", location));
            break;
        default:
            throw new IllegalStateException("ScheduledEventType " + entityType + " is not supported!");
        }

        if (description != null)
            object.put("description", description);
        if (image != null)
            object.put("image", image.getEncoding());
        if (endTime != null)
            object.put("scheduled_end_time", endTime.format(DateTimeFormatter.ISO_DATE_TIME));

        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<ScheduledEvent> request)
    {
        request.onSuccess(api.getEntityBuilder().createScheduledEvent((GuildImpl) guild, response.getObject()));
    }
}

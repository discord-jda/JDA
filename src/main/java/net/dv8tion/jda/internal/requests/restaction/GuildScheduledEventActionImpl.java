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

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.GuildScheduledEventAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.Route;
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

public class GuildScheduledEventActionImpl extends AuditableRestActionImpl<GuildScheduledEvent> implements GuildScheduledEventAction
{
    protected final Guild guild;
    protected String name, description;
    protected Icon image;
    protected long channelId;
    protected String location;
    protected OffsetDateTime startTime, endTime;
    protected int entityType;

    public GuildScheduledEventActionImpl(String name, String location, TemporalAccessor startTime, TemporalAccessor endTime, Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.CREATE_SCHEDULED_EVENT.compile(guild.getId()));
        this.guild = guild;
        this.name = name;
        this.startTime = Helpers.toOffsetDateTime(startTime);
        this.endTime = Helpers.toOffsetDateTime(endTime);
        this.location = location;
        this.entityType = 3;
    }

    public GuildScheduledEventActionImpl(String name, GuildChannel channel, TemporalAccessor startTime, Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.CREATE_SCHEDULED_EVENT.compile(guild.getId()));
        this.guild = guild;
        this.name = name;
        this.startTime = Helpers.toOffsetDateTime(startTime);
        Checks.notNull(channel, "Channel");
        if (!channel.getGuild().equals(guild)) {
            throw new IllegalArgumentException("Invalid parameter: Channel has to be from the same guild as the scheduled event!");
        } else if (channel instanceof StageChannel) {
            this.channelId = channel.getIdLong();
            this.entityType = 1;
        } else if (channel instanceof VoiceChannel) {
            this.channelId = channel.getIdLong();
            this.entityType = 2;
        } else {
            throw new IllegalArgumentException("Invalid parameter: Can only set location to Voice and Stage Channels!");
        }
    }

    @Nonnull
    @Override
    public GuildScheduledEventActionImpl setCheck(BooleanSupplier checks)
    {
        return (GuildScheduledEventActionImpl) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public GuildScheduledEventActionImpl timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (GuildScheduledEventActionImpl) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public GuildScheduledEventActionImpl deadline(long timestamp)
    {
        return (GuildScheduledEventActionImpl) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }


    @Nonnull
    @Override
    @CheckReturnValue
    public GuildScheduledEventActionImpl setDescription(@Nullable String description)
    {
        if (description != null)
            Checks.notLonger(description, GuildScheduledEvent.MAX_DESCRIPTION_LENGTH, "Description");
        this.description = description;
        return this;
    }

    @Nonnull
    @Override
    public GuildScheduledEventAction setImage(@Nullable Icon icon)
    {
        this.image = icon;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        preChecks();
        DataObject object = DataObject.empty();
        object.put("entity_type", entityType);
        object.put("privacy_level", 2);
        object.put("name", name);
        object.put("scheduled_start_time", startTime.format(DateTimeFormatter.ISO_DATE_TIME));
        if (entityType == 1 || entityType == 2)
            object.put("channel_id", channelId);
        if (entityType == 3 && location != null && location.length() > 0)
            object.put("entity_metadata", DataObject.empty().put("location", location));
        if (description != null)
            object.put("description", description);
        if (image != null)
            object.put("image", image.getEncoding());
        if (endTime != null)
            object.put("scheduled_end_time", endTime.format(DateTimeFormatter.ISO_DATE_TIME));

        return getRequestBody(object);
    }

    private void preChecks()
    {
        Checks.notNull(name, "Name");
        Checks.notBlank(name, "Name");
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, GuildScheduledEvent.MAX_NAME_LENGTH, "Name");
        Checks.notNull(startTime, "Start Time");
        Checks.check(startTime.isAfter(OffsetDateTime.now()), "Cannot schedule event in the past!");

        if (entityType == 3) {
            Checks.notNull(location, "Location");
            Checks.notBlank(location, "Location");
            Checks.notEmpty(location, "Location");
            Checks.notLonger(location, GuildScheduledEvent.MAX_LOCATION_LENGTH, "Location");
            Checks.notNull(endTime, "End Time");
            Checks.check((endTime).isAfter(startTime), "Cannot schedule event to end before starting!");

        }

    }


    @Override
    protected void handleSuccess(Response response, Request<GuildScheduledEvent> request)
    {
        request.onSuccess(api.getEntityBuilder().createGuildScheduledEvent((GuildImpl) guild, response.getObject()));
    }
}

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
package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.ScheduledEventManager;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ScheduledEventMembersPaginationAction;
import net.dv8tion.jda.internal.managers.ScheduledEventManagerImpl;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.ScheduledEventMembersPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class ScheduledEventImpl implements ScheduledEvent
{
    private final long id;
    private final Guild guild;

    private String name, description;
    private OffsetDateTime startTime, endTime;
    private String image;
    private Status status;
    private Type type;
    private User creator;
    private long creatorId;
    private int interestedUserCount;
    private String location;

    public ScheduledEventImpl(long id, Guild guild)
    {
        this.id = id;
        this.guild = guild;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return description;
    }

    @Nullable
    @Override
    public String getImageUrl()
    {
        return image == null ? null : String.format(IMAGE_URL, getId(), image, image.startsWith("a_") ? "gif" : "png");
    }

    @Nullable
    @Override
    public User getCreator()
    {
        return creator;
    }

    @Override
    public long getCreatorIdLong()
    {
        return creatorId;
    }

    @Nonnull
    @Override
    public Status getStatus()
    {
        return status;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return type;
    }

    @Nonnull
    @Override
    public OffsetDateTime getStartTime()
    {
        return startTime;
    }

    @Nullable
    @Override
    public OffsetDateTime getEndTime()
    {
        return endTime;
    }

    @Nullable
    @Override
    public GuildChannelUnion getChannel()
    {
        if (type.isChannel())
            return (GuildChannelUnion) guild.getGuildChannelById(location);
        return null;
    }

    @Nonnull
    @Override
    public String getLocation()
    {
        return location;
    }

    @Override
    public int getInterestedUserCount()
    {
        return interestedUserCount;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public ScheduledEventManager getManager()
    {
        return new ScheduledEventManagerImpl(this);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        Guild guild = getGuild();
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_EVENTS))
            throw new InsufficientPermissionException(guild, Permission.MANAGE_EVENTS);

        Route.CompiledRoute route = Route.Guilds.DELETE_SCHEDULED_EVENT.compile(guild.getId(), getId());
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public ScheduledEventMembersPaginationAction retrieveInterestedMembers()
    {
        return new ScheduledEventMembersPaginationActionImpl(this);
    }

    public ScheduledEventImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public ScheduledEventImpl setType(Type type)
    {
        this.type = type;
        return this;
    }

    public ScheduledEventImpl setLocation(String location)
    {
        this.location = location;
        return this;
    }

    public ScheduledEventImpl setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public ScheduledEventImpl setImage(String image)
    {
        this.image = image;
        return this;
    }

    public ScheduledEventImpl setCreatorId(long creatorId)
    {
        this.creatorId = creatorId;
        return this;
    }

    public ScheduledEventImpl setCreator(User creator)
    {
        this.creator = creator;
        return this;
    }

    public ScheduledEventImpl setStatus(Status status)
    {
        this.status = status;
        return this;
    }

    public ScheduledEventImpl setStartTime(OffsetDateTime startTime)
    {
        this.startTime = startTime;
        return this;
    }

    public ScheduledEventImpl setEndTime(OffsetDateTime endTime)
    {
        this.endTime = endTime;
        return this;
    }

    public ScheduledEventImpl setInterestedUserCount(int interestedUserCount)
    {
        this.interestedUserCount = interestedUserCount;
        return this;
    }

    @Override
    public int compareTo(@Nonnull ScheduledEvent scheduledEvent)
    {
        Checks.notNull(scheduledEvent, "Scheduled Event");
        Checks.check(this.getGuild().equals(scheduledEvent.getGuild()), "Cannot compare two Scheduled Events belonging to seperate guilds!");

        int startTimeComparison = OffsetDateTime.timeLineOrder().compare(this.getStartTime(), scheduledEvent.getStartTime());
        if (startTimeComparison == 0)
            return Long.compare(this.getIdLong(), scheduledEvent.getIdLong());
        else
            return startTimeComparison;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof ScheduledEventImpl))
            return false;
        return this.id == ((ScheduledEventImpl) o).id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "ScheduledEvent:" + getName() + '(' + id + ')';
    }
}

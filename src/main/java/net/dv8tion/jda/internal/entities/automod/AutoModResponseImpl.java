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

package net.dv8tion.jda.internal.entities.automod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

public class AutoModResponseImpl implements AutoModResponse
{
    private final Type type;
    private final GuildMessageChannel channel;
    private final String customMessage;
    private final long timeoutDuration;

    public AutoModResponseImpl(Type type)
    {
        this.type = type;
        this.channel = null;
        this.customMessage = null;
        this.timeoutDuration = 0;
    }

    public AutoModResponseImpl(Type type, GuildMessageChannel channel)
    {
        this.type = type;
        this.channel = channel;
        this.customMessage = null;
        this.timeoutDuration = 0;
    }

    public AutoModResponseImpl(Type type, String customMessage)
    {
        this.type = type;
        this.customMessage = customMessage;
        this.channel = null;
        this.timeoutDuration = 0;
    }

    public AutoModResponseImpl(Type type, Duration duration)
    {
        this.type = type;
        this.timeoutDuration = duration.getSeconds();
        this.customMessage = null;
        this.channel = null;
    }

    public AutoModResponseImpl(Guild guild, DataObject json)
    {
        this.type = AutoModResponse.Type.fromKey(json.getInt("type", -1));
        this.channel = guild.getChannelById(GuildMessageChannel.class, json.getUnsignedLong("channel_id", 0L));
        this.customMessage = json.getString("custom_message", null);
        this.timeoutDuration = json.getUnsignedLong("duration_seconds", 0L);
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return type;
    }

    @Nullable
    @Override
    public GuildMessageChannel getChannel()
    {
        return channel;
    }

    @Nullable
    @Override
    public String getCustomMessage()
    {
        return customMessage;
    }

    @Nullable
    @Override
    public Duration getTimeoutDuration()
    {
        return timeoutDuration == 0 ? null : Duration.ofSeconds(timeoutDuration);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject action = DataObject.empty();
        action.put("type", type.getKey());
        if (type == Type.BLOCK_MESSAGE && customMessage == null)
            return action;

        DataObject metadata = DataObject.empty();
        if (customMessage != null)
            metadata.put("custom_message", customMessage);
        if (channel != null)
            metadata.put("channel_id", channel.getId());
        if (timeoutDuration > 0)
            metadata.put("duration_seconds", timeoutDuration);
        action.put("metadata", metadata);
        return action;
    }

    @Override
    public int hashCode()
    {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof AutoModResponseImpl))
            return false;
        AutoModResponseImpl o = (AutoModResponseImpl) obj;
        return type == o.type;
    }
}

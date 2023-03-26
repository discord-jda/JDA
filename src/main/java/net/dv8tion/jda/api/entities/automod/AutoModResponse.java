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

package net.dv8tion.jda.api.entities.automod;

import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.entities.automod.AutoModResponseImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

public interface AutoModResponse extends SerializableData
{
    @Nonnull
    Type getType();

    @Nullable
    GuildMessageChannel getChannel();

    @Nullable
    String getCustomMessage();

    @Nullable
    Duration getTimeoutDuration();

    @Nonnull
    static AutoModResponse blockMessage(@Nullable String customMessage)
    {
        return new AutoModResponseImpl(Type.BLOCK_MESSAGE, customMessage);
    }

    @Nonnull
    static AutoModResponse sendAlert(@Nonnull GuildMessageChannel channel)
    {
        Checks.notNull(channel, "Channel");
        return new AutoModResponseImpl(Type.SEND_ALERT_MESSAGE, channel);
    }

    @Nonnull
    static AutoModResponse timeoutMember(@Nonnull Duration duration)
    {
        Checks.notNull(duration, "Duration");
        Checks.check(!duration.isNegative() && !duration.isZero(), "Duration must be positive");
        return new AutoModResponseImpl(Type.TIMEOUT, duration);
    }

    enum Type
    {
        BLOCK_MESSAGE(1),
        SEND_ALERT_MESSAGE(2),
        TIMEOUT(3),
        UNKNOWN(-1)
        ;

        private final int key;

        Type(int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return key;
        }

        @Nonnull
        public static Type fromKey(int key)
        {
            for (Type type : values())
            {
                if (type.key == key)
                    return type;
            }
            return UNKNOWN;
        }
    }
}

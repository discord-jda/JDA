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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Interaction extends ISnowflake
{
    @Nonnull
    Type getType();

    @Nonnull
    String getToken();

    @Nullable
    Guild getGuild();

    @Nullable
    AbstractChannel getChannel();

    @Nonnull
    default ChannelType getChannelType()
    {
        AbstractChannel channel = getChannel();
        return channel != null ? channel.getType() : ChannelType.UNKNOWN;
    }

    @Nonnull
    User getUser();

    @Nullable
    Member getMember();

    enum Type
    {
        UNKNOWN(-1),
        PING(1),
        SLASH_COMMAND(2),
        BUTTON(3)
        ;
        private final int key;

        Type(int type)
        {
            this.key = type;
        }

        public int getKey()
        {
            return key;
        }

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

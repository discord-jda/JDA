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

package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import java.util.EnumSet;

public class ChannelUtil
{
    public static final EnumSet<ChannelType> SLOWMODE_SUPPORTED = EnumSet.of(
        ChannelType.TEXT, ChannelType.FORUM, ChannelType.MEDIA,
        ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_NEWS_THREAD, ChannelType.GUILD_PRIVATE_THREAD,
        ChannelType.STAGE, ChannelType.VOICE
    );

    public static final EnumSet<ChannelType> NSFW_SUPPORTED = EnumSet.of(ChannelType.TEXT, ChannelType.VOICE, ChannelType.FORUM, ChannelType.MEDIA, ChannelType.NEWS, ChannelType.STAGE);

    public static final EnumSet<ChannelType> TOPIC_SUPPORTED = EnumSet.of(ChannelType.TEXT, ChannelType.FORUM, ChannelType.MEDIA, ChannelType.NEWS);

    public static final EnumSet<ChannelType> POST_CONTAINERS = EnumSet.of(ChannelType.FORUM, ChannelType.MEDIA);

    public static final EnumSet<ChannelType> THREAD_CONTAINERS = EnumSet.of(ChannelType.TEXT, ChannelType.NEWS, ChannelType.FORUM, ChannelType.MEDIA);

    public static <T extends Channel> T safeChannelCast(Object instance, Class<T> toObjectClass)
    {
        if (toObjectClass.isInstance(instance))
            return toObjectClass.cast(instance);

        String cleanedClassName = instance.getClass().getSimpleName().replace("Impl", "");
        throw new IllegalStateException(Helpers.format("Cannot convert channel of type %s to %s!", cleanedClassName, toObjectClass.getSimpleName()));
    }
}

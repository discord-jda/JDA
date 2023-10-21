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

package net.dv8tion.jda;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IGuildChannelContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class ChannelConsistencyTest
{
    private static Set<String> getMethodNames(Class<?> clazz)
    {
        return Arrays.stream(clazz.getDeclaredMethods()).map(Method::getName).collect(Collectors.toSet());
    }

    private static String getChannelName(ChannelType type)
    {
        return type.name().substring(0, 1) + type.name().substring(1).toLowerCase(Locale.ROOT);
    }

    @Test
    public void checkCreateChannelMethods()
    {
        Set<String> guildMethods = getMethodNames(Guild.class);

        EnumSet<ChannelType> creatable = EnumSet.complementOf(EnumSet.of(
            ChannelType.PRIVATE, ChannelType.GROUP, ChannelType.CATEGORY,
            ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.GUILD_NEWS_THREAD,
            ChannelType.UNKNOWN
        ));

        for (ChannelType type : creatable)
        {
            String channelName = getChannelName(type);
            String methodName = "create" + channelName + "Channel";
            Assertions.assertTrue(guildMethods.contains(methodName), "Missing method Guild#" + methodName);
        }

        Set<String> categoryMethods = getMethodNames(Category.class);

        for (ChannelType type : creatable)
        {
            String channelName = getChannelName(type);
            String methodName = "create" + channelName + "Channel";
            Assertions.assertTrue(categoryMethods.contains(methodName), "Missing method Category#" + methodName);
        }
    }

    @Test
    public void checkCacheAccessMethods()
    {
        Set<String> jdaMethods = getMethodNames(IGuildChannelContainer.class);
        Set<String> categoryMethods = getMethodNames(Category.class);

        EnumSet<ChannelType> cacheable = EnumSet.complementOf(EnumSet.of(
            ChannelType.PRIVATE, ChannelType.GROUP, ChannelType.CATEGORY,
            ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.GUILD_NEWS_THREAD,
            ChannelType.UNKNOWN
        ));

        for (ChannelType type : cacheable)
        {
            String channelName = getChannelName(type);

            String methodName = "get" + channelName + "ChannelCache";
            Assertions.assertTrue(jdaMethods.contains(methodName), "Missing method IGuildChannelContainer#" + methodName);

            methodName = "get" + channelName + "ChannelsByName";
            Assertions.assertTrue(jdaMethods.contains(methodName), "Missing method IGuildChannelContainer#" + methodName);

            methodName = "get" + channelName + "ChannelById";
            Assertions.assertTrue(jdaMethods.contains(methodName), "Missing method IGuildChannelContainer#" + methodName);

            methodName = "get" + channelName + "Channels";
            Assertions.assertTrue(jdaMethods.contains(methodName), "Missing method IGuildChannelContainer#" + methodName);
            Assertions.assertTrue(categoryMethods.contains(methodName), "Missing method Category#" + methodName);
        }
    }

    @Test
    public void checkManagerExists()
    {
        EnumSet<ChannelType> editable = EnumSet.complementOf(EnumSet.of(
            ChannelType.PRIVATE, ChannelType.GROUP, ChannelType.CATEGORY,
            ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.GUILD_NEWS_THREAD,
            ChannelType.UNKNOWN
        ));

        for (ChannelType type : editable)
        {
            String channelName = getChannelName(type);

            Assertions.assertDoesNotThrow(() -> {
                Class.forName("net.dv8tion.jda.api.managers.channel.concrete." + channelName + "ChannelManager");
            }, "Missing manager interface for ChannelType." + type);
        }
    }
}

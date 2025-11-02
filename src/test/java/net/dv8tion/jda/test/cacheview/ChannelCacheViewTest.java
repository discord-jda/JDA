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

package net.dv8tion.jda.test.cacheview;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;
import net.dv8tion.jda.internal.utils.cache.ChannelCacheViewImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static net.dv8tion.jda.test.ChecksHelper.assertChecks;
import static org.assertj.core.api.Assertions.*;

class ChannelCacheViewTest
{
    @ValueSource(classes = {
        Channel.class,
        MessageChannel.class,
        TextChannel.class,
    })
    @ParameterizedTest
    @SuppressWarnings("unchecked")
    void testValidChannelInterfaceFilters(Class<?> interfaceType)
    {
        ChannelCacheViewImpl<Channel> cache = new ChannelCacheViewImpl<>(Channel.class);
        assertThatNoException()
            .isThrownBy(() -> cache.ofType((Class<Channel>) interfaceType).getElementById(0L));
    }

    @ValueSource(classes = {
        TextChannelImpl.class,
        String.class
    })
    @ParameterizedTest
    @SuppressWarnings("unchecked")
    void testInvalidChannelInterfaceFilters(Class<?> interfaceType)
    {
        ChannelCacheViewImpl<Channel> cache = new ChannelCacheViewImpl<>(Channel.class);

        assertThatThrownBy(() -> cache.ofType((Class<Channel>) interfaceType).getElementById(0L))
            .hasMessage(String.format("Type %s is not a valid channel interface", interfaceType.getSimpleName()));
    }


    @Test
    @SuppressWarnings("unchecked")
    void testNullChannelInterfaceFilters()
    {
        ChannelCacheViewImpl<Channel> cache = new ChannelCacheViewImpl<>(Channel.class);

        assertChecks("Type", (value) -> cache.ofType((Class<Channel>) value))
            .checksNotNull();
    }
}

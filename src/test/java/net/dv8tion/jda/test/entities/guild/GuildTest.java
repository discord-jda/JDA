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

package net.dv8tion.jda.test.entities.guild;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.utils.cache.ChannelCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.util.Arrays;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GuildTest extends IntegrationTest {
    private static EnumSet<ChannelType> getGuildChannelTypes() {
        return EnumSet.copyOf(
                Arrays.stream(ChannelType.values()).filter(ChannelType::isGuild).toList());
    }

    @Test
    void testInvalidateRemovesAllChannelTypes() {
        ChannelCacheViewImpl<?> globalChannelCache = mock(ChannelCacheViewImpl.class);
        doReturn(globalChannelCache).when(jda).getChannelsView();
        doReturn(EnumSet.noneOf(CacheFlag.class)).when(jda).getCacheFlags();
        doAnswer(Answers.RETURNS_MOCKS).when(jda).getGuildsView();
        doAnswer(Answers.RETURNS_MOCKS).when(jda).getClient();
        doAnswer(Answers.RETURNS_MOCKS).when(jda).getAudioManagersView();
        doAnswer(Answers.RETURNS_MOCKS).when(jda).getSelfUser();
        doReturn(new SnowflakeCacheViewImpl<>(User.class, User::getName))
                .when(jda)
                .getUsersView();

        GuildImpl guild = new GuildImpl(jda, 42L);
        EnumSet<ChannelType> channelTypes = getGuildChannelTypes();

        for (ChannelType type : channelTypes) {
            GuildChannel channel = mock(GuildChannel.class);
            doReturn(type).when(channel).getType();
            doReturn((long) type.getId()).when(channel).getIdLong();
            guild.getChannelView().put(channel);
        }

        guild.invalidate();

        assertThat(channelTypes)
                .allSatisfy(type -> verify(globalChannelCache, times(1)).remove(eq(type), anyLong()));
    }
}

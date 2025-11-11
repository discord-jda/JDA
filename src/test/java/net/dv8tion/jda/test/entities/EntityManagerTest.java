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

package net.dv8tion.jda.test.entities;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.handle.EventCache;
import net.dv8tion.jda.internal.utils.cache.ChannelCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SortedChannelCacheViewImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EntityManagerTest extends IntegrationTest {
    @Mock
    private GuildImpl guild;

    @Test
    void createTextChannelWhileInvalidatingCache() {
        EntityBuilder entityBuilder = new EntityBuilder(jda);

        ChannelCacheViewImpl<Channel> globalChannelCache =
                new ChannelCacheViewImpl<>(Channel.class);
        SortedChannelCacheViewImpl<GuildChannel> guildChannelCache =
                new SortedChannelCacheViewImpl<>(GuildChannel.class);

        when(guild.getJDA()).thenReturn(jda);
        when(guild.getChannelView()).thenReturn(guildChannelCache);
        when(jda.getChannelsView()).thenReturn(globalChannelCache);
        when(jda.getTextChannelById(eq(Constants.CHANNEL_ID)))
                .then(invocation -> globalChannelCache.getElementById(Constants.CHANNEL_ID));
        when(guild.getTextChannelById(eq(Constants.CHANNEL_ID)))
                .then(invocation -> guildChannelCache.getElementById(Constants.CHANNEL_ID));
        when(jda.getEventCache()).thenReturn(mock(EventCache.class));

        entityBuilder.createTextChannel(guild, TestData.CHANNEL_CREATE, Constants.GUILD_ID);

        GuildImpl newGuild = mock(GuildImpl.class);
        when(newGuild.getJDA()).thenReturn(jda);
        when(newGuild.getChannelView())
                .thenReturn(new SortedChannelCacheViewImpl<>(GuildChannel.class));
        when(newGuild.getTextChannelById(eq(Constants.CHANNEL_ID)))
                .then(invocation -> newGuild.getChannelView().getElementById(Constants.CHANNEL_ID));

        TextChannel createdChannel = entityBuilder.createTextChannel(
                newGuild, TestData.CHANNEL_CREATE, Constants.GUILD_ID);

        assertThat(newGuild.getChannelView().getElementById(Constants.CHANNEL_ID))
                .isNotNull();
        assertThat(createdChannel)
                .isSameAs(newGuild.getChannelView().getElementById(Constants.CHANNEL_ID));

        reset(jda);

        when(jda.getGuildById(eq(Constants.GUILD_ID))).thenReturn(newGuild);
        assertThat(createdChannel.getGuild()).isSameAs(newGuild);

        verify(jda, times(1)).getGuildById(anyLong());
    }

    static class TestData {
        static final DataObject CHANNEL_CREATE = DataObject.empty()
                .put("id", Constants.CHANNEL_ID)
                .put("name", "test-channel")
                .put("position", 1)
                .put("permission_overwrites", DataArray.empty());
    }
}

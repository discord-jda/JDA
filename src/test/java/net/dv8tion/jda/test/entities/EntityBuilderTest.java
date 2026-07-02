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

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.entities.SelfUserImpl;
import net.dv8tion.jda.internal.handle.EventCache;
import net.dv8tion.jda.internal.utils.cache.ChannelCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.MemberCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SortedChannelCacheViewImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import net.dv8tion.jda.test.util.MockitoVerifyUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EntityBuilderTest extends IntegrationTest {
    @Mock
    private GuildImpl guild;

    @Test
    void createTextChannelWhileInvalidatingCache() {
        EntityBuilder entityBuilder = new EntityBuilder(jda);

        ChannelCacheViewImpl<Channel> globalChannelCache = new ChannelCacheViewImpl<>(Channel.class);
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
        SortedChannelCacheViewImpl<GuildChannel> channelCache = new SortedChannelCacheViewImpl<>(GuildChannel.class);
        when(newGuild.getJDA()).thenReturn(jda);
        when(newGuild.getChannelView()).thenReturn(channelCache);
        when(newGuild.getTextChannelById(eq(Constants.CHANNEL_ID)))
                .then(invocation -> channelCache.getElementById(Constants.CHANNEL_ID));

        TextChannel createdChannel =
                entityBuilder.createTextChannel(newGuild, TestData.CHANNEL_CREATE, Constants.GUILD_ID);

        assertThat(channelCache.getElementById(Constants.CHANNEL_ID)).isNotNull();
        assertThat(createdChannel).isSameAs(channelCache.getElementById(Constants.CHANNEL_ID));

        reset(jda);

        when(jda.getGuildById(eq(Constants.GUILD_ID))).thenReturn(newGuild);
        assertThat(createdChannel.getGuild()).isSameAs(newGuild);

        verify(jda, times(1)).getGuildById(anyLong());
    }

    @Test
    void createMessageForUserAfterBan() {
        EntityBuilder entityBuilder = jda.getEntityBuilder();
        doReturn(new SnowflakeCacheViewImpl<>(User.class, User::getName))
                .when(jda)
                .getUsersView();
        doReturn(new SelfUserImpl(Constants.BUTLER_USER_ID, jda)).when(jda).getSelfUser();

        MemberCacheViewImpl memberCache = mock(MemberCacheViewImpl.class);
        doReturn(memberCache).when(guild).getMembersView();

        DataObject data = DataObject.fromJson("""
                {
                  "mention_everyone": false,
                  "pinned": false,
                  "components": [],
                  "attachments": [],
                  "author": {
                    "primary_guild": null,
                    "global_name": "minn",
                    "avatar_decoration_data": null,
                    "clan": null,
                    "collectibles": null,
                    "display_name_styles": null,
                    "public_flags": 0,
                    "id": "86699011792191488",
                    "avatar": null,
                    "username": "minn",
                    "discriminator": "0"
                  },
                  "flags": 0,
                  "type": 0,
                  "mention_roles": [],
                  "nonce": "1515403436928012378",
                  "edited_timestamp": null,
                  "content": "",
                  "tts": false,
                  "mentions": [],
                  "guild_id": "125227483518861312",
                  "id": "1515403436928012378",
                  "channel_type": 0,
                  "embeds": [],
                  "channel_id": "125227483518861312",
                  "timestamp": "2026-06-14T12:00:00.123456+00:00"
                }
                """);

        assertThatLoggingFrom(() -> {
                    ReceivedMessage message = entityBuilder.createMessageWithLookup(data, guild, true);
                    assertThat(message.getAuthor()).isNotNull();
                    assertThat(message.getMember()).isNotNull();
                })
                .matchesSnapshot("logging");

        assertThat(MockitoVerifyUtils.getInteractions(memberCache)).isEmpty();
        assertInteractionsWithSnapshot(memberCache);
    }

    static class TestData {
        static final DataObject CHANNEL_CREATE = DataObject.empty()
                .put("id", Constants.CHANNEL_ID)
                .put("name", "test-channel")
                .put("position", 1)
                .put("permission_overwrites", DataArray.empty());
    }
}

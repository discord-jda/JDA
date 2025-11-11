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

package net.dv8tion.jda.test.restaction.pagination;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.Method;
import net.dv8tion.jda.api.requests.restaction.pagination.PinnedMessagePaginationAction.PinnedMessage;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.PinnedMessagePaginationActionImpl;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class PinnedMessagePaginationActionTest extends IntegrationTest {
    @Mock
    private GuildMessageChannel channel;

    @Mock
    private GuildImpl guild;

    @BeforeEach
    void setupMocks() {
        when(guild.getJDA()).thenReturn(jda);
        when(jda.getUsersView())
                .thenReturn(new SnowflakeCacheViewImpl<>(User.class, User::getName));
        channel = new TextChannelImpl(Constants.CHANNEL_ID, guild);
    }

    @Test
    void testEmptyPaginationResponse() {
        PinnedMessagePaginationActionImpl action = new PinnedMessagePaginationActionImpl(channel);

        assertThatRequestFrom(action)
                .hasMethod(Method.GET)
                .hasCompiledRoute("channels/" + Constants.CHANNEL_ID + "/messages/pins?limit=50")
                .whenQueueCalled();

        assertThat(captureListCallback(
                        PinnedMessage.class,
                        action,
                        DataObject.empty().put("items", DataArray.empty())))
                .isEmpty();

        assertThatRequestFrom(action)
                .hasMethod(Method.GET)
                .hasCompiledRoute("channels/" + Constants.CHANNEL_ID + "/messages/pins?limit=50")
                .whenQueueCalled();
    }

    @Test
    void testFullResponseSetsBeforeOnNextUse() {
        PinnedMessagePaginationActionImpl action = new PinnedMessagePaginationActionImpl(channel);

        DataArray items = DataArray.empty();
        OffsetDateTime timeStamp = OffsetDateTime.now();

        for (int i = 0; i < 50; i++) {
            timeStamp = timeStamp.plusSeconds(1);
            items.add(DataObject.empty()
                    .put("pinned_at", timeStamp)
                    .put("message", getTestMessage()));
        }

        OffsetDateTime lastTimestamp = timeStamp;

        assertThat(captureListCallback(
                        PinnedMessage.class, action, DataObject.empty().put("items", items)))
                .hasSize(50)
                .last()
                .matches(pinned -> pinned.getTimePinned().equals(lastTimestamp));

        assertThatRequestFrom(action)
                .hasMethod(Method.GET)
                .hasQueryParams("limit", 50, "before", lastTimestamp)
                .whenQueueCalled();
    }

    private DataObject getTestMessage() {
        return DataObject.empty()
                .put("type", 0)
                .put("id", randomSnowflake())
                .put("channel_id", Constants.CHANNEL_ID)
                .put("content", "test content")
                .put("embeds", DataArray.empty())
                .put("attachments", DataArray.empty())
                .put("components", DataArray.empty())
                .put("mentions", DataArray.empty())
                .put("mention_roles", DataArray.empty())
                .put(
                        "author",
                        DataObject.empty()
                                .put("id", Constants.MINN_USER_ID)
                                .put("username", "minn")
                                .put("discriminator", "0")
                                .put("avatar", null));
    }
}

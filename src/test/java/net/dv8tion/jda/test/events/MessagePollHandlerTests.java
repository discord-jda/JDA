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

package net.dv8tion.jda.test.events;

import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.poll.MessagePollVoteAddEvent;
import net.dv8tion.jda.api.events.message.poll.MessagePollVoteRemoveEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.handle.MessagePollVoteHandler;
import net.dv8tion.jda.test.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class MessagePollHandlerTests extends AbstractSocketHandlerTest
{
    @Mock
    protected GuildMessageChannel channel;

    @BeforeEach
    final void setupMessageContext()
    {
        when(jda.getChannelById(eq(MessageChannel.class), eq(Constants.CHANNEL_ID))).thenReturn(channel);
    }

    @Test
    void testMinimalVoteAdd()
    {
        MessagePollVoteHandler handler = new MessagePollVoteHandler(jda, true);

        String messageId = randomSnowflake();

        assertThatEvent(MessagePollVoteAddEvent.class)
            .hasGetterWithValueEqualTo(MessagePollVoteAddEvent::getMessageId, messageId)
            .hasGetterWithValueEqualTo(MessagePollVoteAddEvent::getAnswerId, 1L)
            .hasGetterWithValueEqualTo(MessagePollVoteAddEvent::getUserIdLong, Constants.MINN_USER_ID)
            .isFiredBy(() -> {
                handler.handle(random.nextLong(), event("MESSAGE_POLL_VOTE_ADD", DataObject.empty()
                    .put("answer_id", 1)
                    .put("message_id", messageId)
                    .put("channel_id", Constants.CHANNEL_ID)
                    .put("user_id", Constants.MINN_USER_ID)));
            });
    }

    @Test
    void testMinimalVoteRemove()
    {
        MessagePollVoteHandler handler = new MessagePollVoteHandler(jda, false);

        String messageId = randomSnowflake();

        assertThatEvent(MessagePollVoteRemoveEvent.class)
            .hasGetterWithValueEqualTo(MessagePollVoteRemoveEvent::getMessageId, messageId)
            .hasGetterWithValueEqualTo(MessagePollVoteRemoveEvent::getAnswerId, 1L)
            .hasGetterWithValueEqualTo(MessagePollVoteRemoveEvent::getUserIdLong, Constants.MINN_USER_ID)
            .isFiredBy(() -> {
                handler.handle(random.nextLong(), event("MESSAGE_POLL_VOTE_REMOVE", DataObject.empty()
                    .put("answer_id", 1)
                    .put("message_id", messageId)
                    .put("channel_id", Constants.CHANNEL_ID)
                    .put("user_id", Constants.MINN_USER_ID)));
            });
    }
}

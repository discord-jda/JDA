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

package net.dv8tion.jda.test.restaction;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.requests.Method.POST;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.when;

public class MessageCreateActionTest extends IntegrationTest
{
    private static final String FIXED_CHANNEL_ID = "1234567890";
    private static final String FIXED_NONCE = "123456";
    private static final String ENDPOINT_URL = "channels/" + FIXED_CHANNEL_ID + "/messages";

    @Mock
    protected MessageChannel channel;

    private static DataObject defaultMessageRequest()
    {
        return DataObject.empty()
                .put("allowed_mentions", DataObject.empty()
                    .put("parse", DataArray.empty()
                        .add("users")
                        .add("roles")
                        .add("everyone"))
                    .put("replied_user", true))
                .put("components", DataArray.empty())
                .put("content", "")
                .put("embeds", DataArray.empty())
                .put("poll", null)
                .put("enforce_nonce", true)
                .put("flags", 0)
                .put("nonce", FIXED_NONCE)
                .put("tts", false);
    }

    @BeforeEach
    void setupChannel()
    {
        when(channel.getId()).thenReturn(FIXED_CHANNEL_ID);
        when(channel.getJDA()).thenReturn(jda);
    }

    @Test
    void testEmpty()
    {
        assertThatIllegalStateException().isThrownBy(() ->
            new MessageCreateActionImpl(channel)
                .queue()
        ).withMessage("Cannot build empty messages! Must provide at least one of: content, embed, file, poll, or stickers");
    }

    @Test
    void testContentOnly()
    {
        MessageCreateAction action = new MessageCreateActionImpl(channel)
                .setContent("test content");

        assertThatRequestFrom(action)
            .hasMethod(POST)
            .hasCompiledRoute(ENDPOINT_URL)
            .hasBodyEqualTo(defaultMessageRequest().put("content", "test content"))
            .whenQueueCalled();
    }

    @Test
    void testEmbedOnly()
    {
        MessageCreateAction action = new MessageCreateActionImpl(channel)
            .setEmbeds(new EmbedBuilder()
                .setDescription("test description")
                .build());

        assertThatRequestFrom(action)
            .hasMethod(POST)
            .hasCompiledRoute(ENDPOINT_URL)
            .hasBodyEqualTo(defaultMessageRequest()
                .put("embeds", DataArray.empty()
                    .add(DataObject.empty().put("description", "test description"))))
            .whenQueueCalled();
    }

    @Test
    void testPollOnly()
    {
        MessageCreateAction action = new MessageCreateActionImpl(channel)
            .setPoll(new MessagePollBuilder("Test poll")
                .setDuration(3, TimeUnit.DAYS)
                .setMultiAnswer(true)
                .addAnswer("Test answer 1")
                .addAnswer("Test answer 2", Emoji.fromUnicode("🤔"))
                .addAnswer("Test answer 3", Emoji.fromCustom("minn", 821355005788684298L, true))
                .build());

        assertThatRequestFrom(action)
            .hasMethod(POST)
            .hasCompiledRoute(ENDPOINT_URL)
            .hasBodyEqualTo(defaultMessageRequest()
                .put("poll", DataObject.empty()
                    .put("duration", 72)
                    .put("allow_multiselect", true)
                    .put("layout_type", 1)
                    .put("question", DataObject.empty()
                        .put("text", "Test poll"))
                    .put("answers", DataArray.empty()
                        .add(DataObject.empty()
                            .put("answer_id", 0)
                            .put("poll_media", DataObject.empty()
                                .put("text", "Test answer 1")
                                .put("emoji", null)))
                        .add(DataObject.empty()
                            .put("answer_id", 1)
                            .put("poll_media", DataObject.empty()
                                .put("text", "Test answer 2")
                                .put("emoji", DataObject.empty()
                                    .put("name", "🤔"))))
                        .add(DataObject.empty()
                            .put("answer_id", 2)
                            .put("poll_media", DataObject.empty()
                                .put("text", "Test answer 3")
                                .put("emoji", DataObject.empty()
                                    .put("name", "minn")
                                    .put("id", 821355005788684298L)
                                    .put("animated", true)))))))
            .whenQueueCalled();
    }

    @Nonnull
    protected DataObject normalizeRequestBody(@Nonnull DataObject body)
    {
        return body.put("nonce", FIXED_NONCE);
    }
}

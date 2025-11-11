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

package net.dv8tion.jda.test.entities.message;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import net.dv8tion.jda.test.Resources;
import net.dv8tion.jda.test.components.ComponentTestData;
import net.dv8tion.jda.test.util.TestResourceUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static net.dv8tion.jda.test.util.MockitoVerifyUtils.assertInteractionsContainMethods;
import static net.dv8tion.jda.test.util.MockitoVerifyUtils.getMethodsByPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.*;

public class MessageCreateBuilderTest extends AbstractSnapshotTest {
    @Test
    void testEmptyBuilder_fromMessage() {
        Message message = mock(Message.class);
        when(message.getType()).thenReturn(MessageType.DEFAULT);

        MessageCreateBuilder builder = spy(new MessageCreateBuilder());
        builder.applyMessage(message);
        assertThat(builder)
                .usingRecursiveComparison()
                .isEqualTo(MessageCreateBuilder.fromMessage(message));

        Set<String> expectedCalls = getMessageCreateBuilderSetters();

        Arrays.asList("setFiles", "setAllowedMentions").forEach(expectedCalls::remove);

        assertInteractionsContainMethods(builder, expectedCalls);
        assertThatIllegalStateException().isThrownBy(builder::build);
    }

    @Test
    void testFullBuilder() {
        MessageCreateBuilder builder = spy(new MessageCreateBuilder());

        builder.setContent("Test content")
                .setEmbeds(new EmbedBuilder().setDescription("Test embed").build())
                .setComponents(ComponentTestData.getMinimalComponent(
                        ActionRow.class, Component.Type.ACTION_ROW))
                .useComponentsV2(false)
                .setFiles(Collections.emptyList())
                .setAllowedMentions(Collections.emptyList())
                .setFiles(TestResourceUtil.getFileUpload(Resources.LOGO_PNG))
                .setPoll(MessagePollData.builder("Is this tested?")
                        .addAnswer("Yes")
                        .addAnswer("No")
                        .build())
                .setTTS(true)
                .setVoiceMessage(false)
                .setSuppressEmbeds(false)
                .setSuppressedNotifications(true);

        assertInteractionsContainMethods(builder, getMessageCreateBuilderSetters());

        try (MessageCreateData data = builder.build()) {
            assertWithSnapshot(data);
        }
    }

    private static Set<String> getMessageCreateBuilderSetters() {
        return getMethodsByPattern(MessageCreateBuilder.class, "^(set|use).+$");
    }
}

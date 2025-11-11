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
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import net.dv8tion.jda.test.Resources;
import net.dv8tion.jda.test.components.ComponentTestData;
import net.dv8tion.jda.test.util.TestResourceUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static net.dv8tion.jda.test.util.MockitoVerifyUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessageEditBuilderTest extends AbstractSnapshotTest {
    @Test
    void testEmptyBuilder_fromMessage() {
        Message message = mock(Message.class);
        when(message.getType()).thenReturn(MessageType.DEFAULT);

        MessageEditBuilder builder = spy(new MessageEditBuilder());
        builder.applyMessage(message);
        assertThat(builder).usingRecursiveComparison().isEqualTo(MessageEditBuilder.fromMessage(message));

        Set<String> expectedCalls = getMessageEditBuilderSetters();

        Arrays.asList("setFiles", "setAttachments", "setAllowedMentions").forEach(expectedCalls::remove);

        assertInteractionsContainMethods(builder, expectedCalls);

        try (MessageEditData data = builder.build()) {
            assertWithSnapshot(data);
        }
    }

    @Test
    void testFullBuilder() {
        MessageEditBuilder builder = spy(new MessageEditBuilder());

        builder.setContent("Test content")
                .setEmbeds(new EmbedBuilder().setDescription("Test embed").build())
                .setComponents(ComponentTestData.getMinimalComponent(ActionRow.class, Component.Type.ACTION_ROW))
                .useComponentsV2(false)
                .setAttachments(Collections.emptyList())
                .setReplace(true)
                .setSuppressEmbeds(false)
                .setAllowedMentions(Collections.emptyList())
                .setFiles(TestResourceUtil.getFileUpload(Resources.LOGO_PNG));

        assertInteractionsContainMethods(builder, getMessageEditBuilderSetters());

        try (MessageEditData data = builder.build()) {
            assertWithSnapshot(data);
        }
    }

    private static Set<String> getMessageEditBuilderSetters() {
        return getMethodsByPattern(MessageEditBuilder.class, "^(set|use).+$");
    }
}

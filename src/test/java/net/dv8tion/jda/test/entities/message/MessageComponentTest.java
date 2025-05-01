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
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class MessageComponentTest
{
    private static final FileUpload EXAMPLE_FILE_UPLOAD = FileUpload.fromData(new byte[100], "bytes.bin");
    private static final TextDisplay EXAMPLE_TEXT_DISPLAY = TextDisplay.create(getMaxContentString());
    private static final Section EXAMPLE_SECTION = Section.of(Button.primary("id", "label"), EXAMPLE_TEXT_DISPLAY);
    private static final MediaGallery EXAMPLE_MEDIA_GALLERY = MediaGallery.of(MediaGalleryItem.fromFile(EXAMPLE_FILE_UPLOAD));
    private static final Separator EXAMPLE_SEPARATOR = Separator.createDivider(Separator.Spacing.SMALL);
    private static final FileDisplay EXAMPLE_FILE_DISPLAY = FileDisplay.fromFile(EXAMPLE_FILE_UPLOAD);
    private static final Container EXAMPLE_CONTAINER = Container.of(EXAMPLE_TEXT_DISPLAY);
    private static final ActionRow EXAMPLE_ROW = ActionRow.of(Button.primary("id", "label"));

    @MethodSource("buildInvalidMessage")
    @ParameterizedTest
    void buildInvalidMessage(Function<AbstractMessageBuilder<?, ?>, AbstractMessageBuilder<?, ?>> builderFunction)
    {
        // The builder function may return null when a test case is impossible using the provided builder type
        // (e.g., voice messages are only on MessageCreateBuilder)
        final AbstractMessageBuilder<?, ?> createBuilder = builderFunction.apply(new MessageCreateBuilder());
        if (createBuilder != null)
        {
            assertThat(createBuilder.isValid()).isFalse();
            assertThatIllegalStateException().isThrownBy(createBuilder::build);
        }

        final AbstractMessageBuilder<?, ?> editBuilder = builderFunction.apply(new MessageEditBuilder());
        if (editBuilder != null)
        {
            assertThat(editBuilder.isValid()).isFalse();
            assertThatIllegalStateException().isThrownBy(editBuilder::build);
        }
    }

    static Stream<Arguments> buildInvalidMessage()
    {
        return Stream.of(
                // Attempt to use V2 components in V1 mode
                Arguments.of(message(b -> b.setComponents(EXAMPLE_SECTION))),
                Arguments.of(message(b -> b.setComponents(EXAMPLE_TEXT_DISPLAY))),
                Arguments.of(message(b -> b.setComponents(EXAMPLE_MEDIA_GALLERY))),
                Arguments.of(message(b -> b.setComponents(EXAMPLE_SEPARATOR))),
                Arguments.of(message(b -> b.setComponents(EXAMPLE_FILE_DISPLAY))),
                Arguments.of(message(b -> b.setComponents(EXAMPLE_CONTAINER))),
                // Attempt to use V1 content in V2 mode
                Arguments.of(messageCreate(b -> b.useComponentsV2().setVoiceMessage(true).setFiles(EXAMPLE_FILE_UPLOAD))),
                Arguments.of(message(b -> b.useComponentsV2().setContent("content"))),
                Arguments.of(message(b -> b.useComponentsV2().setEmbeds(new EmbedBuilder().setDescription("description").build()))),
                Arguments.of(messageCreate(b -> b.useComponentsV2().setPoll(new MessagePollBuilder("title").addAnswer("answer").build()))),
                // Attempt to send no components in V2 mode (use a file for it to be not empty)
                Arguments.of(messageCreate(b -> b.useComponentsV2().setFiles(EXAMPLE_FILE_UPLOAD))),
                // Attempt to edit with an entirely new message with no components in V2 mode (use a file for it to be not empty)
                Arguments.of(messageEdit(b -> b.useComponentsV2().setReplace(true).setFiles(EXAMPLE_FILE_UPLOAD))),
                // Attempt to use >MAX_COMPONENT_COUNT top-level
                Arguments.of(message(b -> b.setComponents(mergeItems(getMaxTopLevelV1(), EXAMPLE_ROW)))),
                // Attempt to use >MAX_COMPONENT_COUNT_IN_COMPONENT_TREE total
                Arguments.of(message(b -> b.useComponentsV2().setComponents(mergeItems(getMaxTotal(), EXAMPLE_FILE_DISPLAY)))),
                // Attempt to use >MAX_CONTENT_LENGTH_COMPONENT_V2
                Arguments.of(message(b -> b.useComponentsV2().setComponents(EXAMPLE_TEXT_DISPLAY, TextDisplay.create("1"))))
        );
    }

    @MethodSource("buildValidMessage")
    @ParameterizedTest
    void buildValidMessage(Function<AbstractMessageBuilder<?, ?>, AbstractMessageBuilder<?, ?>> builderFunction)
    {
        // The builder function may return null when a test case is impossible using the provided builder type
        // (e.g., voice messages are only on MessageCreateBuilder)
        final AbstractMessageBuilder<?, ?> createBuilder = builderFunction.apply(new MessageCreateBuilder());
        if (createBuilder != null)
        {
            assertThat(createBuilder.isValid()).isTrue();
            assertThatNoException().isThrownBy(createBuilder::build);
        }

        final AbstractMessageBuilder<?, ?> editBuilder = builderFunction.apply(new MessageEditBuilder());
        if (editBuilder != null)
        {
            assertThat(editBuilder.isValid()).isTrue();
            assertThatNoException().isThrownBy(editBuilder::build);
        }
    }

    static Stream<Arguments> buildValidMessage()
    {
        return Stream.of(
                // Use V2 components
                Arguments.of(message(b -> b.useComponentsV2().setComponents(EXAMPLE_SECTION))),
                Arguments.of(message(b -> b.useComponentsV2().setComponents(EXAMPLE_TEXT_DISPLAY))),
                Arguments.of(message(b -> b.useComponentsV2().setComponents(EXAMPLE_MEDIA_GALLERY))),
                Arguments.of(message(b -> b.useComponentsV2().setComponents(EXAMPLE_SEPARATOR))),
                Arguments.of(message(b -> b.useComponentsV2().setComponents(EXAMPLE_FILE_DISPLAY))),
                Arguments.of(message(b -> b.useComponentsV2().setComponents(EXAMPLE_CONTAINER))),
                // Use V1 content
                Arguments.of(messageCreate(b -> b.setVoiceMessage(true).setFiles(EXAMPLE_FILE_UPLOAD))),
                Arguments.of(message(b -> b.setContent("content"))),
                Arguments.of(message(b -> b.setEmbeds(new EmbedBuilder().setDescription("description").build()))),
                Arguments.of(messageCreate(b -> b.setPoll(new MessagePollBuilder("title").addAnswer("answer").build()))),
                // Send no components in V1 mode
                Arguments.of(messageCreate(b -> b.setFiles(EXAMPLE_FILE_UPLOAD))),
                // Edit with an entirely new message with no components in V1 mode
                Arguments.of(messageEdit(b -> b.setReplace(true).setFiles(EXAMPLE_FILE_UPLOAD))),
                // MAX_COMPONENT_COUNT top-level
                Arguments.of(message(b -> b.useComponentsV2().setComponents(getMaxTopLevelV1()))),
                // Add top-levels until it would break the tree size checks,
                // to make sure the top-level size isn't checked, or at least equal to the max tree size
                Arguments.of(message(b -> b.useComponentsV2().setComponents(getAbsurdTopLevelV2()))),
                // MAX_COMPONENT_COUNT_IN_COMPONENT_TREE total
                Arguments.of(message(b -> b.useComponentsV2().setComponents(getMaxTotal()))),
                // Attempt to use >MAX_CONTENT_LENGTH_COMPONENT_V2
                Arguments.of(message(b -> b.useComponentsV2().setComponents(EXAMPLE_TEXT_DISPLAY)))
        );
    }

    private static String getMaxContentString()
    {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Message.MAX_CONTENT_LENGTH_COMPONENT_V2; i++)
            builder.append('0');
        return builder.toString();
    }

    private static Collection<MessageTopLevelComponent> getMaxTopLevelV1()
    {
        final List<MessageTopLevelComponent> list = new ArrayList<>(Message.MAX_COMPONENT_COUNT);
        for (int i = 0; i < Message.MAX_COMPONENT_COUNT; i++)
            list.add(EXAMPLE_ROW);
        return list;
    }

    private static Collection<MessageTopLevelComponent> getAbsurdTopLevelV2()
    {
        final int limit = Message.MAX_COMPONENT_COUNT_IN_COMPONENT_TREE - 1;
        final List<MessageTopLevelComponent> list = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++)
            list.add(EXAMPLE_SEPARATOR);
        return list;
    }

    private static Collection<MessageTopLevelComponent> getMaxTotal()
    {
        // Artificial limit just to increase the test coverage, as it will have to count nested components
        final int limit = 10;

        final List<MessageTopLevelComponent> containers = new ArrayList<>();
        final List<Separator> current = new ArrayList<>();
        // Containers + container items + to-be-inserted + 1 (new container with remaining items)
        while (containers.size() + (containers.size() * limit) + current.size() + 1  < Message.MAX_COMPONENT_COUNT_IN_COMPONENT_TREE)
        {
            current.add(EXAMPLE_SEPARATOR);
            if (current.size() == limit)
            {
                containers.add(Container.of(current));
                current.clear();
            }
        }
        if (!current.isEmpty())
            containers.add(Container.of(current));

        return containers;
    }

    @SafeVarargs
    private static <E> List<E> mergeItems(Collection<E> list, E... other)
    {
        final List<E> merged = new ArrayList<>(list);
        Collections.addAll(merged, other);
        return merged;
    }

    private static Function<AbstractMessageBuilder<?, ?>, AbstractMessageBuilder<?, ?>> message(Function<AbstractMessageBuilder<?, ?>, AbstractMessageBuilder<?, ?>> function)
    {
        return function;
    }

    private static Function<AbstractMessageBuilder<?, ?>, AbstractMessageBuilder<?, ?>> messageCreate(Function<MessageCreateBuilder, MessageCreateBuilder> function)
    {
        return abstractMessageBuilder ->
        {
            if (abstractMessageBuilder instanceof MessageCreateBuilder)
                return function.apply((MessageCreateBuilder) abstractMessageBuilder);
            return null;
        };
    }

    private static Function<AbstractMessageBuilder<?, ?>, AbstractMessageBuilder<?, ?>> messageEdit(Function<MessageEditBuilder, MessageEditBuilder> function)
    {
        return abstractMessageBuilder ->
        {
            if (abstractMessageBuilder instanceof MessageEditBuilder)
                return function.apply((MessageEditBuilder) abstractMessageBuilder);
            return null;
        };
    }
}

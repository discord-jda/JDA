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

package net.dv8tion.jda.test.components.utils;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponentUnion;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.components.UnknownComponentImpl;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentsUtilTest
{
    private static final UnknownComponentImpl UNKNOWN_COMPONENT = new UnknownComponentImpl(DataObject.empty());

    @MethodSource("testUnknownComponentCannotBeInsertedArguments")
    @ParameterizedTest
    void testUnknownComponentCannotBeInserted(ThrowableAssert.ThrowingCallable callable)
    {
        Assertions.assertThatIllegalArgumentException().isThrownBy(callable);
    }

    static Stream<Arguments> testUnknownComponentCannotBeInsertedArguments()
    {
        // Try everywhere ComponentsUtil is used
        return Stream.of(
                Arguments.of(run(() -> Section.of(UNKNOWN_COMPONENT, TextDisplay.of("0")))),
                Arguments.of(run(() -> Section.of(Button.primary("id", "label"), UNKNOWN_COMPONENT))),
                Arguments.of(run(() -> Modal.create("id", "title").addComponents(UNKNOWN_COMPONENT))),
                Arguments.of(run(() -> new MessageEditBuilder().setComponents(UNKNOWN_COMPONENT))),
                Arguments.of(run(() -> new MessageCreateBuilder().addComponents(UNKNOWN_COMPONENT))),
                Arguments.of(run(() -> ActionRow.of(UNKNOWN_COMPONENT))),
                Arguments.of(run(() -> ActionRow.partitionOf(UNKNOWN_COMPONENT))),
                Arguments.of(run(() -> Container.of(UNKNOWN_COMPONENT)))
        );
    }

    private static ThrowableAssert.ThrowingCallable run(ThrowableAssert.ThrowingCallable runnable)
    {
        return runnable;
    }

    @Test
    void testRemoveComponentFromRow()
    {
        final Button button2 = Button.secondary("button2", "test").withUniqueId(2);
        MessageComponentTree tree = MessageComponentTree.of(
                ActionRow.of(
                        Button.primary("button1", "test").withUniqueId(1),
                        button2
                )
        );

        MessageComponentTree newTree = tree.replace(ComponentReplacer.byUniqueId(1, (Button) null));
        final ActionRow row = newTree.getComponents().getFirst().asActionRow();
        assertThat(row.getComponents()).hasSize(1);
        assertThat(row.getComponents()).contains((ActionRowChildComponentUnion) button2);
    }
}

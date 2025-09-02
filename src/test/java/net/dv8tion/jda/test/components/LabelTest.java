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

package net.dv8tion.jda.test.components;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.components.tree.ModalComponentTree;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LabelTest extends AbstractSnapshotTest
{
    @Test
    void testModalComponentTreeWithLabel()
    {
        ModalComponentTree components = ModalComponentTree.of(
            Label.of("Custom label 1",
                StringSelectMenu.create("menu")
                    .addOption("This is an option", "option-1")
                    .build()),
            Label.of("Custom label 2",
                TextInput.create("input", TextInputStyle.SHORT).build())
        );

        assertWithSnapshot(
            DataObject.empty()
                .put("components", DataArray.fromCollection(components.getComponents()))
        );
    }

    @Nested
    class TextInputChild
    {
        @Test
        void testMinimalTextInput()
        {
            Label label = Label.of("test-label",
                TextInput.create("custom-id", TextInputStyle.SHORT).build()
            );
            assertWithSnapshot(label);
        }

        @Test
        void testFullTextInput()
        {
            Label label = Label.of("test-label",
                TextInput.create("custom-id", TextInputStyle.SHORT)
                    .setStyle(TextInputStyle.PARAGRAPH)
                    .setMinLength(1)
                    .setMaxLength(TextInput.MAX_VALUE_LENGTH)
                    .setRequired(false)
                    .setPlaceholder("Custom placeholder")
                    .setUniqueId(10)
                    .build()
            );

            assertWithSnapshot(label);
        }
    }

    @Nested
    class StringSelectMenuChild
    {
        @Test
        void testMinimalStringSelectMenu()
        {
            Label label = Label.of("test-label",
                StringSelectMenu.create("custom-id")
                    .addOption("Option 1", "option-1")
                    .build()
            );

            assertWithSnapshot(label);
        }

        @Test
        void testFullStringSelectMenu()
        {
            Label label = Label.of("test-label",
                StringSelectMenu.create("custom-id")
                    .setRequired(false)
                    .setDisabled(false)
                    .setPlaceholder("Custom placeholder")
                    .setMinValues(1)
                    .setMaxValues(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                    .setUniqueId(10)
                    .addOption("Option 1", "option-1")
                    .addOption("Option 2", "option-2", Emoji.fromCustom("minn", 821355005788684298L, true))
                    .addOption("Option 3", "option-3", "Custom description")
                    .build()
            );

            assertWithSnapshot(label);
        }
    }

    private void assertWithSnapshot(Label label)
    {
        assertWithSnapshot((SerializableData) label);
    }
}

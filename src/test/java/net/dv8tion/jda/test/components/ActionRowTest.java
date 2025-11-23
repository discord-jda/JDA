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

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponentUnion;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import net.dv8tion.jda.test.ChecksHelper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.dv8tion.jda.api.components.replacer.ComponentReplacer.byUniqueId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class ActionRowTest extends AbstractSnapshotTest {

    private static final ActionRowChildComponentUnion EXAMPLE_BUTTON =
            (ActionRowChildComponentUnion) Button.primary("id", "label").withUniqueId(1);
    private static final ActionRowChildComponentUnion EXAMPLE_MENU =
            (ActionRowChildComponentUnion) EntitySelectMenu.create("id", EntitySelectMenu.SelectTarget.ROLE)
                    .setUniqueId(2)
                    .build();

    @Test
    void testGetMaxAllowedIsUpdated() {
        DataObject actual = DataObject.empty();

        for (Component.Type type : Component.Type.values()) {
            actual.put(type.name(), ActionRow.getMaxAllowed(type));
        }

        snapshotHandler.compareWithSnapshot(actual, null);
    }

    @Test
    void testEmptyRowThrows() {
        ChecksHelper.<ActionRowChildComponentUnion>assertCollectionChecks("Row", ActionRow::of)
                .checksNotNull()
                .checksNotEmpty();
    }

    @Test
    void testCombiningDifferentElementsThrows() {
        List<ActionRowChildComponentUnion> list = Arrays.asList(EXAMPLE_BUTTON, EXAMPLE_MENU);

        assertThatIllegalArgumentException().isThrownBy(() -> ActionRow.of(list));
    }

    @Test
    void testPartitionOf() {
        assertThatIllegalArgumentException().isThrownBy(() -> ActionRow.partitionOf(Collections.emptyList()));

        List<ActionRow> rowsOfSixButtons = ActionRow.partitionOf(
                EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON);
        assertThat(rowsOfSixButtons)
                .isEqualTo(Arrays.asList(
                        ActionRow.of(EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON),
                        ActionRow.of(EXAMPLE_BUTTON)));

        List<ActionRow> rowsOfFiveButtons =
                ActionRow.partitionOf(EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON);
        assertThat(rowsOfFiveButtons)
                .isEqualTo(Collections.singletonList(
                        ActionRow.of(EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON)));
    }

    @Test
    void testReplacerWithValidReplacement() {
        ActionRow actionRow = ActionRow.of(EXAMPLE_BUTTON);

        Button newButton = Button.secondary("custom-id", "Label");
        ActionRow updatedRow = actionRow.replace(byUniqueId(EXAMPLE_BUTTON.getUniqueId(), newButton));

        assertThat(updatedRow.getComponents()).containsExactly((ActionRowChildComponentUnion) newButton);
    }

    @Test
    void testReplacerRemovingOneButton() {
        ActionRow actionRow = ActionRow.of(EXAMPLE_BUTTON, EXAMPLE_BUTTON.withUniqueId(42));
        ActionRow updatedRow = actionRow.replace(byUniqueId(EXAMPLE_BUTTON.getUniqueId(), (Component) null));

        assertThat(updatedRow.getComponents()).containsExactly(EXAMPLE_BUTTON);
    }

    @Test
    void testReplacerWithEmptyingReplacement() {
        ActionRow actionRow = ActionRow.of(EXAMPLE_BUTTON);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> actionRow.replace(byUniqueId(EXAMPLE_BUTTON.getUniqueId(), (Component) null)));
    }

    @Test
    void testReplacerWithInvalidReplacement() {
        int uniqueId = 42;
        ActionRow actionRow = ActionRow.of(EXAMPLE_BUTTON, EXAMPLE_BUTTON.withUniqueId(uniqueId));

        assertThatIllegalArgumentException().isThrownBy(() -> actionRow.replace(byUniqueId(uniqueId, EXAMPLE_MENU)));
    }
}

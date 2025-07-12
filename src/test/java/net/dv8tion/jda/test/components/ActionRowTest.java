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
import net.dv8tion.jda.internal.components.actionrow.ActionRowImpl;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ActionRowTest extends AbstractSnapshotTest
{

    private static final ActionRowChildComponentUnion EXAMPLE_BUTTON =
            (ActionRowChildComponentUnion) Button.primary("id", "label");
    private static final ActionRowChildComponentUnion EXAMPLE_MENU =
            (ActionRowChildComponentUnion) EntitySelectMenu
                    .create("id", EntitySelectMenu.SelectTarget.ROLE)
                    .build();

    @Test
    void testGetMaxAllowedIsUpdated()
    {
        DataObject actual = DataObject.empty();

        for (Component.Type type : Component.Type.values())
            actual.put(type.name(), ActionRow.getMaxAllowed(type));

        snapshotHandler.compareWithSnapshot(actual, null);
    }

    @Test
    void testEmptyRowThrows()
    {
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> new ActionRowImpl(Collections.emptyList(), -1));
    }

    @Test
    void testCombiningDifferentElementsThrows()
    {
        final List<ActionRowChildComponentUnion> list = Arrays.asList(EXAMPLE_BUTTON, EXAMPLE_MENU);

        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> new ActionRowImpl(list, -1));
    }

    @Test
    void testPartitionOf()
    {
        Assertions.assertThatIllegalArgumentException()
                        .isThrownBy(() -> ActionRow.partitionOf(Collections.emptyList()));

        final List<ActionRow> rowsOfSixButtons = ActionRow.partitionOf(EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON);
        Assertions.assertThat(rowsOfSixButtons)
                .isEqualTo(Arrays.asList(
                        ActionRow.of(EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON),
                        ActionRow.of(EXAMPLE_BUTTON)
                ));

        final List<ActionRow> rowsOfFiveButtons = ActionRow.partitionOf(EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON);
        Assertions.assertThat(rowsOfFiveButtons)
                .isEqualTo(Collections.singletonList(
                        ActionRow.of(EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON, EXAMPLE_BUTTON)
                ));
    }
}

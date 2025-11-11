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
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponentUnion;
import net.dv8tion.jda.api.components.section.SectionContentComponentUnion;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.test.ChecksHelper;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static net.dv8tion.jda.api.components.replacer.ComponentReplacer.byUniqueId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class SectionTest {
    private static final SectionAccessoryComponentUnion EXAMPLE_BUTTON =
            (SectionAccessoryComponentUnion) Button.primary("id", "label").withUniqueId(1);
    private static final SectionContentComponentUnion EXAMPLE_TEXT_DISPLAY =
            (SectionContentComponentUnion) TextDisplay.of("content").withUniqueId(2);

    @Test
    void testEmptyContainerThrows() {
        ChecksHelper.<SectionContentComponentUnion>assertCollectionChecks(
                        "Components", coll -> Section.of(EXAMPLE_BUTTON, coll))
                .checksNotNull()
                .checksNotEmpty();
    }

    @Test
    void testContainerWithNoAccessoryThrows() {
        ChecksHelper.<SectionAccessoryComponentUnion>assertChecks(
                        "Accessory",
                        accessory -> Section.of(accessory, Collections.singletonList(EXAMPLE_TEXT_DISPLAY)))
                .checksNotNull();
    }

    @Test
    void testReplacerWithValidReplacement() {
        Section container = Section.of(EXAMPLE_BUTTON, EXAMPLE_TEXT_DISPLAY);

        TextDisplay replacedText = TextDisplay.of("Replaced");
        Section replaced = container.replace(byUniqueId(EXAMPLE_TEXT_DISPLAY.getUniqueId(), replacedText));

        assertThat(replaced.getContentComponents()).containsExactly((SectionContentComponentUnion) replacedText);

        Button replacedButton = Button.secondary("replaced", "Replaced Button");
        replaced = container.replace(byUniqueId(EXAMPLE_BUTTON.getUniqueId(), replacedButton));

        assertThat(replaced.getAccessory()).isSameAs(replacedButton);
    }

    @Test
    void testReplacerWithEmptyingReplacement() {
        Section container = Section.of(EXAMPLE_BUTTON, EXAMPLE_TEXT_DISPLAY);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> container.replace(byUniqueId(EXAMPLE_TEXT_DISPLAY.getUniqueId(), (Component) null)));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> container.replace(byUniqueId(EXAMPLE_BUTTON.getUniqueId(), (Component) null)));
    }
}

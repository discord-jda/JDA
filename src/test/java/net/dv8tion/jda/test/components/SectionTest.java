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

import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponentUnion;
import net.dv8tion.jda.api.components.section.SectionContentComponentUnion;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.internal.components.section.SectionImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class SectionTest
{
    private static final SectionAccessoryComponentUnion EXAMPLE_BUTTON =
            (SectionAccessoryComponentUnion) Button.primary("id", "label");
    private static final SectionContentComponentUnion EXAMPLE_TEXT_DISPLAY =
            (SectionContentComponentUnion) TextDisplay.of("content");

    @Test
    void testEmptyContainerThrows()
    {
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> new SectionImpl(-1, Collections.emptyList(), EXAMPLE_BUTTON));
    }

    @Test
    void testContainerWithNoAccessoryThrows()
    {
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> new SectionImpl(-1, Collections.singletonList(EXAMPLE_TEXT_DISPLAY), null));
    }
}

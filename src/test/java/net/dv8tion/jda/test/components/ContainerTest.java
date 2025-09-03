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
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.test.ChecksHelper;
import org.junit.jupiter.api.Test;

import static net.dv8tion.jda.api.components.replacer.ComponentReplacer.byUniqueId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class ContainerTest
{
    private static final TextDisplay EXAMPLE_TEXT = TextDisplay.of("Test display").withUniqueId(1);

    @Test
    void testEmptyContainerThrows()
    {
        ChecksHelper.<ContainerChildComponentUnion>assertCollectionChecks("Components", Container::of)
                .checksNotNull()
                .checksNotEmpty();
    }

    @Test
    void testReplacerWithValidReplacement()
    {
        Container container = Container.of(EXAMPLE_TEXT);

        TextDisplay replacedText = TextDisplay.of("Replaced");
        Container replaced = container.replace(byUniqueId(EXAMPLE_TEXT.getUniqueId(), replacedText));

        assertThat(replaced.getComponents())
            .containsExactly((ContainerChildComponentUnion) replacedText);
    }

    @Test
    void testReplacerWithEmptyingReplacement()
    {
        Container container = Container.of(EXAMPLE_TEXT);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> container.replace(byUniqueId(EXAMPLE_TEXT.getUniqueId(), (Component) null)));
    }
}

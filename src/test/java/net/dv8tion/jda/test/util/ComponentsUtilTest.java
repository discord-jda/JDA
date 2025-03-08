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

package net.dv8tion.jda.test.util;

import net.dv8tion.jda.api.components.button.Button;
import net.dv8tion.jda.api.components.button.ButtonStyle;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentsUtilTest
{
    @MethodSource("testCustomId")
    @ParameterizedTest
    void testCustomId(String customId, ButtonStyle style, String label, boolean expected)
    {
        final Button button = Button.of(style, customId, label, null);
        assertThat(ComponentsUtil.isSameIdentifier(button, "id")).isEqualTo(expected);
    }

    static Stream<Arguments> testCustomId()
    {
        return Stream.of(
                Arguments.of("id", ButtonStyle.PRIMARY, "Label", true),
                Arguments.of("http://localhost:8080", ButtonStyle.LINK, "Label", false),
                Arguments.of("1234", ButtonStyle.PREMIUM, "", false)
        );
    }
}

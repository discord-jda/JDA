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

package net.dv8tion.jda.test.restaction;

import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.requests.Method;
import net.dv8tion.jda.internal.entities.SelfUserImpl;
import net.dv8tion.jda.test.Constants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class CreateApplicationEmojiTest extends RestActionTest
{
    private static final String EXAMPLE_NAME = "thinking";
    private static final Icon EXAMPLE_ICON = Icon.from(new byte[]{1, 2, 3});

    @BeforeEach
    void setupMocks()
    {
        when(jda.createApplicationEmoji(any(), any())).thenCallRealMethod();
        when(jda.getSelfUser()).thenReturn(new SelfUserImpl(Constants.BUTLER_USER_ID, jda));
    }

    @MethodSource
    @ParameterizedTest
    void testNullArguments(String name, Icon icon)
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> jda.createApplicationEmoji(name, icon));
    }

    static Stream<Arguments> testNullArguments()
    {
        return Stream.of(
            arguments(null, null),
            arguments(null, EXAMPLE_ICON),
            arguments(EXAMPLE_NAME, null)
        );
    }

    @Test
    void testWrongNameFormat()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> jda.createApplicationEmoji("test with spaces", EXAMPLE_ICON))
            .withMessageContaining("must match regex");
    }

    @Test
    void testWrongNameLength()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> jda.createApplicationEmoji(StringUtils.repeat('a', 33), EXAMPLE_ICON))
            .withMessageContaining("must be between 2 and 32 characters long");
    }

    @Test
    void testValidEmoji()
    {
        assertThatRequestFrom(jda.createApplicationEmoji(EXAMPLE_NAME, EXAMPLE_ICON))
            .hasMethod(Method.POST)
            .hasCompiledRoute("applications/" + Constants.BUTLER_USER_ID + "/emojis")
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }
}

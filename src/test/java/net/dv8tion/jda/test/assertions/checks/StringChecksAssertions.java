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

package net.dv8tion.jda.test.assertions.checks;

import org.junit.jupiter.api.function.ThrowingConsumer;

import static net.dv8tion.jda.test.ChecksHelper.*;
import static net.dv8tion.jda.test.TestHelpers.repeat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class StringChecksAssertions extends AbstractChecksAssertions<String, StringChecksAssertions>
{
    public StringChecksAssertions(String name, ThrowingConsumer<String> callable)
    {
        super(name, callable);
    }

    public StringChecksAssertions checksNotEmpty()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> callable.accept(null))
            .withMessage(isNullError(name));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> callable.accept(""))
            .withMessage(isEmptyError(name));
        return this;
    }

    public StringChecksAssertions checksNotBlank()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> callable.accept(null))
            .withMessage(isNullError(name));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> callable.accept(""))
            .withMessage(isBlankError(name));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> callable.accept(" "))
            .withMessage(isBlankError(name));
        return this;
    }

    public StringChecksAssertions checksNotLonger(int maxLength)
    {
        String invalidInput = repeat("s", maxLength + 1);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> callable.accept(invalidInput))
            .withMessage(tooLongError(name, maxLength, invalidInput));
        return this;
    }
}

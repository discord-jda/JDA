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

import static net.dv8tion.jda.test.ChecksHelper.isNullError;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class AbstractChecksAssertions<T, S extends AbstractChecksAssertions<T, S>>
{
    protected final String name;
    protected final ThrowingConsumer<T> callable;

    public AbstractChecksAssertions(String name, ThrowingConsumer<T> callable)
    {
        this.name = name;
        this.callable = callable;
    }

    public S checksNotNull()
    {
        return throwsFor(null, isNullError(name));
    }

    @SuppressWarnings("unchecked")
    public S throwsFor(T input, String expectedError)
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> callable.accept(input))
            .withMessage(expectedError);
        return (S) this;
    }
}

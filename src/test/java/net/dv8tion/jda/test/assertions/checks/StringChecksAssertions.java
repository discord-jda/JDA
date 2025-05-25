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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.function.ThrowingConsumer;

import java.util.regex.Pattern;

import static net.dv8tion.jda.test.ChecksHelper.*;

public class StringChecksAssertions extends AbstractChecksAssertions<String, StringChecksAssertions>
{
    public StringChecksAssertions(String name, ThrowingConsumer<String> callable)
    {
        super(name, callable);
    }

    public StringChecksAssertions checksNotEmpty()
    {
        throwsFor(null, isNullError(name));
        throwsFor("", isEmptyError(name));
        return this;
    }

    public StringChecksAssertions checksNotBlank()
    {
        return checksNotBlank(true);
    }

    public StringChecksAssertions checksNotBlank(boolean checkNull)
    {
        if (checkNull)
            throwsFor(null, isNullError(name));
        throwsFor("", isBlankError(name));
        throwsFor(" ", isBlankError(name));
        return this;
    }

    public StringChecksAssertions checksNotLonger(int maxLength)
    {
        String invalidInput = StringUtils.repeat("s", maxLength + 1);
        throwsFor(invalidInput, tooLongError(name, maxLength, invalidInput));
        return this;
    }

    public StringChecksAssertions checksLowercaseOnly()
    {
        throwsFor("InvalidCasing", isNotLowercase(name, "InvalidCasing"));
        return this;
    }

    public StringChecksAssertions checksRange(int minLength, int maxLength)
    {
        String tooLong = StringUtils.repeat("s", maxLength + 1);
        String tooShort = StringUtils.repeat("s", minLength - 1);
        throwsFor(tooShort, notInRangeError(name, minLength, maxLength, tooShort));
        throwsFor(tooLong, notInRangeError(name, minLength, maxLength, tooLong));
        return this;
    }

    public StringChecksAssertions checksRegex(String input, Pattern regex)
    {
        throwsFor(input, notRegexMatch(name, regex, input));
        return this;
    }

    public StringChecksAssertions checksNoWhitespace()
    {
        String input = "hello world";
        throwsFor(input, containsWhitespaceError(name, input));
        return this;
    }
}

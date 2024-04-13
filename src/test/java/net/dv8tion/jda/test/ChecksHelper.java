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

package net.dv8tion.jda.test;

import net.dv8tion.jda.test.assertions.checks.StringChecksAssertions;
import org.junit.jupiter.api.function.ThrowingConsumer;

public class ChecksHelper
{
    public static String tooLongError(String name, int maxLength, String value)
    {
        return name + " may not be longer than " + maxLength + " characters! Provided: \"" + value + "\"";
    }

    public static String isNullError(String name)
    {
        return name + " may not be null";
    }

    public static String isEmptyError(String name)
    {
        return name + " may not be empty";
    }

    public static String isBlankError(String name)
    {
        return name + " may not be blank";
    }

    public static String notPositiveError(String name)
    {
        return name + " may not be negative or zero";
    }

    public static StringChecksAssertions assertStringChecks(String name, ThrowingConsumer<String> callable)
    {
        return new StringChecksAssertions(name, callable);
    }
}

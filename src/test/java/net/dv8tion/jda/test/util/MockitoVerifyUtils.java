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

import org.intellij.lang.annotations.Language;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;

public class MockitoVerifyUtils {
    public static Set<String> getMethodsByPattern(@Nonnull Class<?> clazz, @Language("RegExp") String regex) {
        Pattern pattern = Pattern.compile(regex);
        return Stream.of(clazz.getMethods())
                .map(Method::getName)
                .filter(name -> pattern.matcher(name).matches())
                .collect(Collectors.toSet());
    }

    @Nonnull
    public static Set<String> getSetters(@Nonnull Class<?> clazz) {
        return getMethodsByPattern(clazz, "^set.+$");
    }

    public static void assertInteractionsContainMethods(@Nonnull Object spy, @Nonnull Set<String> methodNames) {
        assertThat(methodNames).isNotEmpty();

        Set<String> actualCalls = mockingDetails(spy).getInvocations().stream()
                .map(invocation -> invocation.getMethod().getName())
                .collect(Collectors.toSet());

        assertThat(actualCalls)
                .as(
                        "Invocations on %s should include expected calls",
                        spy.getClass().getSimpleName())
                .containsAll(methodNames);
    }
}

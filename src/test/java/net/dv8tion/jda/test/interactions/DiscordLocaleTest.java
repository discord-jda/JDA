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

package net.dv8tion.jda.test.interactions;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.internal.generated.AvailableLocalesEnumDto;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordLocaleTest {
    @Test
    void testDiscordLocaleDefinitions() {
        assertThat(Arrays.stream(DiscordLocale.values())).allSatisfy(locale -> {
            assertThat(locale.getLocale()).isNotBlank();
            assertThat(locale.getLanguageName()).isNotBlank();
            assertThat(locale.getNativeName()).isNotBlank();
        });
    }

    @Test
    void testDiscordLocaleCompleteness() {
        Set<String> discordLocales = Arrays.stream(AvailableLocalesEnumDto.values())
                .map(AvailableLocalesEnumDto::getId)
                .collect(Collectors.toSet());
        Set<String> jdaLocales = Arrays.stream(DiscordLocale.values())
                .map(DiscordLocale::getLocale)
                .collect(Collectors.toSet());

        assertThat(jdaLocales).containsAll(discordLocales);
    }
}

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

package net.dv8tion.jda.test.entities.message;

import net.dv8tion.jda.api.entities.Message;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageRegexTest {
    @ParameterizedTest
    @ValueSource(
            strings = {
                "discord.gg/jda",
                "discord.com/invite/jda",
                "discord.com/invite\\jda",
            })
    void testValidInviteLinks(String link) {
        assertThat(Message.INVITE_PATTERN.matcher(link).matches()).isTrue();
        assertThat(Message.INVITE_PATTERN.matcher("https://" + link).matches()).isTrue();
        assertThat(Message.INVITE_PATTERN.matcher("http://" + link).matches()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "discord.gg\\jda",
                "discord.com\\invite",
                "discord.com\\invite/jda",
            })
    void testInvalidInviteLinks(String link) {
        assertThat(Message.INVITE_PATTERN.matcher(link).matches()).isFalse();
        assertThat(Message.INVITE_PATTERN.matcher("https://" + link).matches()).isFalse();
        assertThat(Message.INVITE_PATTERN.matcher("http://" + link).matches()).isFalse();
    }
}

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

package net.dv8tion.jda.test.entities.scheduledevent;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.internal.entities.ScheduledEventImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ScheduledEventTest extends IntegrationTest {
    @Mock
    Guild guild;

    @Test
    void testGetJumpUrl() {
        String eventId = randomSnowflake();
        String guildId = Long.toUnsignedString(Constants.GUILD_ID);

        when(guild.getId()).thenReturn(guildId);

        ScheduledEvent scheduledEvent =
                new ScheduledEventImpl(Long.parseUnsignedLong(eventId), guild);

        assertThat(scheduledEvent.getJumpUrl())
                .isEqualTo("https://discord.com/events/" + guildId + "/" + eventId);
    }
}

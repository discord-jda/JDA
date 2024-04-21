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

package net.dv8tion.jda.test.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.handle.GuildSetupController;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import net.dv8tion.jda.test.assertions.events.EventFiredAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class AbstractSocketHandlerTest extends IntegrationTest
{
    @Mock
    protected GuildSetupController setupController;
    @Mock
    protected Guild guild;

    @BeforeEach
    final void setupHandlerContext()
    {
        when(jda.getGuildSetupController()).thenReturn(setupController);
        when(setupController.isLocked(anyLong())).thenReturn(false);
        when(jda.getGuildById(eq(Constants.GUILD_ID))).thenReturn(guild);
    }

    protected DataObject event(String type, DataObject data)
    {
        return DataObject.empty()
            .put("s", 1)
            .put("op", 0)
            .put("t", type)
            .put("d", data);
    }

    protected <T> EventFiredAssertions<T> assertThatEvent(Class<T> eventType)
    {
        return new EventFiredAssertions<>(eventType, jda);
    }
}

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

import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.Method;
import net.dv8tion.jda.internal.requests.restaction.CommandEditActionImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

public class CommandEditActionTest extends IntegrationTest
{
    @Mock
    private SelfUser selfUser;

    @BeforeEach
    void setupMocks()
    {
        when(jda.getSelfUser()).thenReturn(selfUser);
        when(selfUser.getApplicationId()).thenReturn(Long.toUnsignedString(Constants.BUTLER_USER_ID));
    }

    @Test
    void testEditSlashCommandById()
    {
        String id = randomSnowflake();
        CommandEditActionImpl action = new CommandEditActionImpl(jda, Command.Type.SLASH, id);

        assertThatNoException().isThrownBy(() -> {
            action.setName("updated-name");
            action.setDescription("Updated description");
            action.setNSFW(true);
        });

        assertThatIllegalArgumentException().isThrownBy(() -> action.setName("updated name with space"));

        assertThatRequestFrom(action)
            .hasMethod(Method.PATCH)
            .hasCompiledRoute("applications/" + Constants.BUTLER_USER_ID + "/commands/" + id)
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }

    @Test
    void testEditMessageCommandById()
    {
        String id = randomSnowflake();
        CommandEditActionImpl action = new CommandEditActionImpl(jda, Command.Type.MESSAGE, id);

        assertThatNoException().isThrownBy(() -> action.setName("updated name with space"));
        assertThatIllegalStateException().isThrownBy(() -> action.setDescription("Updated description"));

        action.setNSFW(true);

        assertThatRequestFrom(action)
            .hasMethod(Method.PATCH)
            .hasCompiledRoute("applications/" + Constants.BUTLER_USER_ID + "/commands/" + id)
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }

    @Test
    void testEditUserCommandById()
    {
        String id = randomSnowflake();
        CommandEditActionImpl action = new CommandEditActionImpl(jda, Command.Type.USER, id);

        assertThatNoException().isThrownBy(() -> action.setName("updated name with space"));
        assertThatIllegalStateException().isThrownBy(() -> action.setDescription("Updated description"));

        action.setNSFW(true);

        assertThatRequestFrom(action)
            .hasMethod(Method.PATCH)
            .hasCompiledRoute("applications/" + Constants.BUTLER_USER_ID + "/commands/" + id)
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }
}

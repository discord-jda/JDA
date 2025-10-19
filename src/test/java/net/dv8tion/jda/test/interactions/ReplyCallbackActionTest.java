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

import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyCallbackActionImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

class ReplyCallbackActionTest extends IntegrationTest
{
    @Mock
    SelfUser selfUser;
    @Mock
    InteractionImpl interaction;
    @Mock
    InteractionHookImpl hook;

    @BeforeEach
    void setupMocks()
    {
        when(jda.getSelfUser()).thenReturn(selfUser);
        when(selfUser.getApplicationIdLong()).thenReturn(Constants.BUTLER_USER_ID);
        doReturn(randomSnowflake()).when(interaction).getId();
        doReturn(randomSnowflake()).when(interaction).getToken();
        doReturn(jda).when(interaction).getJDA();
        doReturn(interaction).when(hook).getInteraction();
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void deferReplyWithComponentsV2(boolean useComponentsV2)
    {
        ReplyCallbackAction action = new ReplyCallbackActionImpl(hook);

        action = action.useComponentsV2(useComponentsV2);

        assertThatRequestFrom(action)
            .hasBodyMatchingSnapshot(String.valueOf(useComponentsV2))
            .whenQueueCalled();
    }
}

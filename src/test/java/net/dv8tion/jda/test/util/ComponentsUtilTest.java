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

import net.dv8tion.jda.api.entities.SkuSnowflake;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.ComponentsUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentsUtilTest
{
    @Test
    void testId()
    {
        final Button button = Button.primary("id", "Label");
        assertThat(ComponentsUtil.isSameIdentifier(button, "id")).isTrue();
    }

    @Test
    void testUrl()
    {
        final Button button = Button.link("http://localhost:8080", "Label");
        assertThat(ComponentsUtil.isSameIdentifier(button, "http://localhost:8080")).isTrue();
    }

    @Test
    void testSku()
    {
        final Button button = Button.premium(SkuSnowflake.fromId(1234), "Label");
        assertThat(ComponentsUtil.isSameIdentifier(button, "1234")).isTrue();
    }
}

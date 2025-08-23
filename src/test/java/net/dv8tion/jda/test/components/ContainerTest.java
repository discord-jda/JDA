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

package net.dv8tion.jda.test.components;

import net.dv8tion.jda.api.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.internal.components.container.ContainerImpl;
import net.dv8tion.jda.test.ChecksHelper;
import org.junit.jupiter.api.Test;

public class ContainerTest
{

    @Test
    void testEmptyContainerThrows()
    {
        ChecksHelper.<ContainerChildComponentUnion>assertCollectionChecks(
                        "Components",
                        coll -> new ContainerImpl(-1, coll, false, null)
                )
                .checksNotNull()
                .checksNotEmpty();
    }
}

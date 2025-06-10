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

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ActionRowTest extends AbstractSnapshotTest
{

    @Test
    void testGetMaxAllowedIsUpdated()
    {
        String actual = Arrays.stream(Component.Type.values())
                .map(type -> type.name() + " = " + ActionRow.getMaxAllowed(type))
                .collect(Collectors.joining("\n"));

        snapshotHandler.compareWithSnapshot(actual, null);
    }
}

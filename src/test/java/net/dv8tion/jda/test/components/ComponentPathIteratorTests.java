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
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.utils.ComponentIterator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class ComponentPathIteratorTests {
    /**
     * This test only makes sure that a contributor adding a new component type is alerted
     * that this code needs to be updated if necessary.
     */
    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testIteratorIsUpdated()
    {
        for (Component.Type type : Component.Type.values())
        {
            switch (type)
            {
            case UNKNOWN:
            case BUTTON:
            case STRING_SELECT:
            case TEXT_INPUT:
            case USER_SELECT:
            case ROLE_SELECT:
            case MENTIONABLE_SELECT:
            case CHANNEL_SELECT:
            case TEXT_DISPLAY:
            case THUMBNAIL:
            case MEDIA_GALLERY:
            case FILE_DISPLAY:
            case SEPARATOR:
                break;
            case ACTION_ROW:
                final ActionRow row = mock(ActionRow.class);
                ComponentIterator.createStream(Collections.singleton(row)).collect(Collectors.toList());
                verify(row, times(1)).getComponents();
                break;
            case SECTION:
                final Section section = mock(Section.class);
                ComponentIterator.createStream(Collections.singleton(section)).collect(Collectors.toList());
                verify(section, times(1)).getContentComponents();
                verify(section, times(1)).getAccessory();
                break;
            case CONTAINER:
                final Container container = mock(Container.class);
                ComponentIterator.createStream(Collections.singleton(container)).collect(Collectors.toList());
                verify(container, times(1)).getComponents();
                break;
            default:
                fail("Please update this test with the new component type, then update ComponentPathIterator if necessary " + type.name());
            }
        }
    }
}

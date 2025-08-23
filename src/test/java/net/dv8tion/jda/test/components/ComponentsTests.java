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
import net.dv8tion.jda.api.components.Components;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.components.tree.ModalComponentTree;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.UnknownComponentImpl;
import net.dv8tion.jda.internal.components.actionrow.ActionRowImpl;
import net.dv8tion.jda.internal.components.buttons.ButtonImpl;
import net.dv8tion.jda.internal.components.container.ContainerImpl;
import net.dv8tion.jda.internal.components.filedisplay.FileDisplayImpl;
import net.dv8tion.jda.internal.components.mediagallery.MediaGalleryImpl;
import net.dv8tion.jda.internal.components.section.SectionImpl;
import net.dv8tion.jda.internal.components.selections.EntitySelectMenuImpl;
import net.dv8tion.jda.internal.components.selections.StringSelectMenuImpl;
import net.dv8tion.jda.internal.components.separator.SeparatorImpl;
import net.dv8tion.jda.internal.components.textdisplay.TextDisplayImpl;
import net.dv8tion.jda.internal.components.textinput.TextInputImpl;
import net.dv8tion.jda.internal.components.thumbnail.ThumbnailImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.fail;

public class ComponentsTests
{
    @EnumSource(ComponentTree.Type.class)
    @ParameterizedTest
    void testParseTreeIsUpdated(ComponentTree.Type type)
    {
        switch(type)
        {
        case ANY:
            testParseTreeIsUpdated(ComponentTree.class);
            break;
        case MESSAGE:
            testParseTreeIsUpdated(MessageComponentTree.class);
            break;
        case MODAL:
            testParseTreeIsUpdated(ModalComponentTree.class);
            break;
        default:
            fail("Please update this test with the new component type (" + type.name() + "), then update Components#parseTree if necessary");
        }
    }

    private <T extends ComponentTree<?>> void testParseTreeIsUpdated(Class<T> clazz)
    {
        // We only want to test that [[Components#parseTree]] recognizes the tree type,
        // don't construct a real tree as that may throw on empty trees
        try (MockedStatic<?> ignored = Mockito.mockStatic(clazz))
        {
            Assertions.assertThatNoException().isThrownBy(() -> Components.parseTree(clazz, DataArray.empty()));
        }
    }

    @EnumSource(Component.Type.class)
    @ParameterizedTest
    void testParseComponentIsUpdated(Component.Type type)
    {
        switch(type)
        {
        case UNKNOWN:
            testParseComponentIsUpdated(type, UnknownComponentImpl.class);
            break;
        case ACTION_ROW:
            testParseComponentIsUpdated(type, ActionRowImpl.class);
            break;
        case BUTTON:
            testParseComponentIsUpdated(type, ButtonImpl.class);
            break;
        case STRING_SELECT:
            testParseComponentIsUpdated(type, StringSelectMenuImpl.class);
            break;
        case TEXT_INPUT:
            testParseComponentIsUpdated(type, TextInputImpl.class);
            break;
        case USER_SELECT:
        case ROLE_SELECT:
        case MENTIONABLE_SELECT:
        case CHANNEL_SELECT:
            testParseComponentIsUpdated(type, EntitySelectMenuImpl.class);
            break;
        case SECTION:
            testParseComponentIsUpdated(type, SectionImpl.class);
            break;
        case TEXT_DISPLAY:
            testParseComponentIsUpdated(type, TextDisplayImpl.class);
            break;
        case THUMBNAIL:
            testParseComponentIsUpdated(type, ThumbnailImpl.class);
            break;
        case MEDIA_GALLERY:
            testParseComponentIsUpdated(type, MediaGalleryImpl.class);
            break;
        case FILE_DISPLAY:
            testParseComponentIsUpdated(type, FileDisplayImpl.class);
            break;
        case SEPARATOR:
            testParseComponentIsUpdated(type, SeparatorImpl.class);
            break;
        case CONTAINER:
            testParseComponentIsUpdated(type, ContainerImpl.class);
            break;
        default:
            fail("Please update this test with the new component type (" + type.name() + "), then update Components#parseComponent if necessary");
        }
    }

    private <T extends Component> void testParseComponentIsUpdated(Component.Type type, Class<T> clazz)
    {
        // We only want to test that [[Components#parseComponent]] recognizes the component type,
        // don't construct a real component as that will throw due to the empty data
        try (MockedConstruction<?> ignored = Mockito.mockConstruction(clazz))
        {
            Assertions.assertThatNoException().isThrownBy(() -> Components.parseComponent(clazz, DataObject.empty().put("type", type.getKey())));
        }
    }
}

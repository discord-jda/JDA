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
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu.SelectTarget;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.UnknownComponentImpl;
import net.dv8tion.jda.test.Resources;

import static org.assertj.core.api.Assertions.fail;

public class ComponentTestData
{
    public static  <T extends Component> T getMinimalComponent(Class<T> componentType, Component.Type type)
    {
        switch (type)
        {
        case UNKNOWN:
            return componentType.cast(new UnknownComponentImpl(DataObject.empty().put("type", 42).put("id", 0)));
        case ACTION_ROW:
            return componentType.cast(ActionRow.of(Button.primary("button", "Click me!")));
        case BUTTON:
            return componentType.cast(Button.primary("button", "Click me!"));
        case STRING_SELECT:
            return componentType.cast(StringSelectMenu.create("select-menu").addOption("Option 1", "option-1").build());
        case TEXT_INPUT:
            return componentType.cast(TextInput.create("text-input", TextInputStyle.SHORT).build());
        case USER_SELECT:
            return componentType.cast(EntitySelectMenu.create("user-menu", SelectTarget.USER).build());
        case ROLE_SELECT:
            return componentType.cast(EntitySelectMenu.create("role-menu", SelectTarget.ROLE).build());
        case MENTIONABLE_SELECT:
            return componentType.cast(EntitySelectMenu.create("mentionable-menu", SelectTarget.USER, SelectTarget.ROLE).build());
        case CHANNEL_SELECT:
            return componentType.cast(EntitySelectMenu.create("channel-menu", SelectTarget.CHANNEL).build());
        case SECTION:
            return componentType.cast(Section.of(
                Thumbnail.fromFile(getImageFileUpload()),
                TextDisplay.of("Section with thumbnail")
            ));
        case TEXT_DISPLAY:
            return componentType.cast(TextDisplay.of("TextDisplay"));
        case THUMBNAIL:
            return componentType.cast(Thumbnail.fromFile(getImageFileUpload()));
        case MEDIA_GALLERY:
            return componentType.cast(MediaGallery.of(MediaGalleryItem.fromFile(getImageFileUpload())));
        case FILE_DISPLAY:
            return componentType.cast(FileDisplay.fromFile(getImageFileUpload()));
        case SEPARATOR:
            return componentType.cast(Separator.createInvisible(Separator.Spacing.LARGE));
        case CONTAINER:
            return componentType.cast(Container.of(
                TextDisplay.of("First text"),
                Separator.createDivider(Separator.Spacing.SMALL),
                FileDisplay.fromFile(getImageFileUpload())
            ));
        case LABEL:
            return componentType.cast(Label.of("Custom label", TextInput.create("input", TextInputStyle.SHORT).build()));
        default:
            return fail("Unhandled component type: " + type);
        }
    }

    private static FileUpload getImageFileUpload()
    {
        return FileUpload.fromData(ComponentTestData.class.getResourceAsStream("/" + Resources.LOGO_PNG), Resources.LOGO_PNG);
    }
}

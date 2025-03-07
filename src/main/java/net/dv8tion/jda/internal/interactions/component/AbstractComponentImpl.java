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

package net.dv8tion.jda.internal.interactions.component;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.container.Container;
import net.dv8tion.jda.api.interactions.components.file.File;
import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGallery;
import net.dv8tion.jda.api.interactions.components.section.Section;
import net.dv8tion.jda.api.interactions.components.selects.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selects.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.separator.Separator;
import net.dv8tion.jda.api.interactions.components.text_display.TextDisplay;
import net.dv8tion.jda.api.interactions.components.text_input.TextInput;
import net.dv8tion.jda.api.interactions.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public abstract class AbstractComponentImpl
{

    // -- Union hooks --

    @Nonnull
    public ActionRow asActionRow()
    {
        return toComponentType(ActionRow.class);
    }

    @Nonnull
    public Button asButton()
    {
        return toComponentType(Button.class);
    }

    @Nonnull
    public StringSelectMenu asStringSelect()
    {
        return toComponentType(StringSelectMenu.class);
    }

    @Nonnull
    public EntitySelectMenu asEntitySelect()
    {
        return toComponentType(EntitySelectMenu.class);
    }

    @Nonnull
    public TextInput asTextInput()
    {
        return toComponentType(TextInput.class);
    }

    @Nonnull
    public Section asSection()
    {
        return toComponentType(Section.class);
    }

    @Nonnull
    public TextDisplay asTextDisplay()
    {
        return toComponentType(TextDisplay.class);
    }

    @Nonnull
    public MediaGallery asMediaGallery()
    {
        return toComponentType(MediaGallery.class);
    }

    @Nonnull
    public Thumbnail asThumbnail() {
        return toComponentType(Thumbnail.class);
    }

    @Nonnull
    public Separator asSeparator()
    {
        return toComponentType(Separator.class);
    }

    @Nonnull
    public File asFile()
    {
        return toComponentType(File.class);
    }

    @Nonnull
    public Container asContainer()
    {
        return toComponentType(Container.class);
    }

    protected <T extends Component> T toComponentType(Class<T> type) {
        return UnionUtil.safeUnionCast("component", this, type);
    }

    public List<FileUpload> getFiles()
    {
        return Collections.emptyList();
    }
}

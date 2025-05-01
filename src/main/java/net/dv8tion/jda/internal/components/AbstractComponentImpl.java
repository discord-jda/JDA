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

package net.dv8tion.jda.internal.components;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.button.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selects.EntitySelectMenu;
import net.dv8tion.jda.api.components.selects.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.Nonnull;

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
    public FileDisplay asFileDisplay()
    {
        return toComponentType(FileDisplay.class);
    }

    @Nonnull
    public Container asContainer()
    {
        return toComponentType(Container.class);
    }

    protected <T extends Component> T toComponentType(Class<T> type) {
        return UnionUtil.safeUnionCast("component", this, type);
    }
}

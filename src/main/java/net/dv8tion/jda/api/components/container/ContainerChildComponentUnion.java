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

package net.dv8tion.jda.api.components.container;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ComponentUnion;
import net.dv8tion.jda.api.components.UnknownComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.UnknownComponentImpl;
import net.dv8tion.jda.internal.components.actionrow.ActionRowImpl;
import net.dv8tion.jda.internal.components.filedisplay.FileDisplayImpl;
import net.dv8tion.jda.internal.components.mediagallery.MediaGalleryImpl;
import net.dv8tion.jda.internal.components.section.SectionImpl;
import net.dv8tion.jda.internal.components.separator.SeparatorImpl;
import net.dv8tion.jda.internal.components.textdisplay.TextDisplayImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a union of {@link ContainerChildComponent ContainerChildComponents} that can be either
 * <ul>
 *     <li>{@link ActionRow}</li>
 *     <li>{@link Section}</li>
 *     <li>{@link TextDisplay}</li>
 *     <li>{@link MediaGallery}</li>
 *     <li>{@link Separator}</li>
 *     <li>{@link FileDisplay}</li>
 *     <li>{@link UnknownComponent}, detectable via {@link #isUnknownComponent()}</li>
 * </ul>
 */
public interface ContainerChildComponentUnion extends ContainerChildComponent, ComponentUnion
{
    @Nonnull
    ActionRow asActionRow();

    @Nonnull
    Section asSection();

    @Nonnull
    TextDisplay asTextDisplay();

    @Nonnull
    MediaGallery asMediaGallery();

    @Nonnull
    Separator asSeparator();

    @Nonnull
    FileDisplay asFile();

    @Nonnull
    static ContainerChildComponentUnion fromData(@Nonnull DataObject data)
    {
        Checks.notNull(data, "Data");

        switch (Component.Type.fromKey(data.getInt("type")))
        {
        case ACTION_ROW:
            return ActionRowImpl.fromData(data);
        case SECTION:
            return SectionImpl.fromData(data);
        case TEXT_DISPLAY:
            return new TextDisplayImpl(data);
        case MEDIA_GALLERY:
            return new MediaGalleryImpl(data);
        case FILE_DISPLAY:
            return new FileDisplayImpl(data);
        case SEPARATOR:
            return new SeparatorImpl(data);
        default:
            return new UnknownComponentImpl(data);
        }
    }

    @Nonnull
    static List<ContainerChildComponentUnion> fromData(@Nonnull DataArray data)
    {
        Checks.notNull(data, "Data");

        return data
                .stream(DataArray::getObject)
                .map(ContainerChildComponentUnion::fromData)
                .collect(Collectors.toList());
    }
}

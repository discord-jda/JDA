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

package net.dv8tion.jda.api.components;

import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponentUnion;
import net.dv8tion.jda.api.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponentUnion;
import net.dv8tion.jda.api.components.section.SectionContentComponentUnion;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponentUnion;
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
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

/**
 * Base interface for {@link Component} union types.
 *
 * @see ActionRowChildComponentUnion
 * @see ContainerChildComponentUnion
 * @see SectionContentComponentUnion
 * @see SectionAccessoryComponentUnion
 * @see MessageTopLevelComponentUnion
 * @see ModalTopLevelComponentUnion
 */
public interface IComponentUnion extends Component
{
    /**
     * Whether this component is an {@link UnknownComponent}.
     *
     * <p>Unknown components have restrictions, see {@link UnknownComponent} for details.
     *
     * <p>This is equivalent to {@code getType() == Type.UNKNOWN}.
     *
     * @return {@code true} if this is an unknown component, {@code false} if not.
     *
     * @see UnknownComponent
     */
    default boolean isUnknownComponent() {
        return this.getType() == Type.UNKNOWN;
    }

    /**
     * Converts the provided {@link DataObject} into an {@link IComponentUnion}.
     * <br>Note that any unsupported component will be represented as an {@link net.dv8tion.jda.api.components.UnknownComponent UnknownComponent}.
     *
     * @param  data
     *         The {@link DataObject} to create the component from
     *
     * @return An {@link IComponentUnion} representing the provided data
     *
     * @throws IllegalArgumentException
     *         If the provided data is null
     */
    @Nonnull
    static IComponentUnion fromData(@Nonnull DataObject data)
    {
        Checks.notNull(data, "Data");

        switch (Component.Type.fromKey(data.getInt("type")))
        {
        case ACTION_ROW:
            return ActionRowImpl.fromData(data);
        case BUTTON:
            return new ButtonImpl(data);
        case STRING_SELECT:
            return new StringSelectMenuImpl(data);
        case TEXT_INPUT:
            return new TextInputImpl(data);
        case USER_SELECT:
        case ROLE_SELECT:
        case MENTIONABLE_SELECT:
        case CHANNEL_SELECT:
            return new EntitySelectMenuImpl(data);
        case SECTION:
            return SectionImpl.fromData(data);
        case TEXT_DISPLAY:
            return new TextDisplayImpl(data);
        case THUMBNAIL:
            return new ThumbnailImpl(data);
        case MEDIA_GALLERY:
            return new MediaGalleryImpl(data);
        case FILE_DISPLAY:
            return new FileDisplayImpl(data);
        case SEPARATOR:
            return new SeparatorImpl(data);
        case CONTAINER:
            return ContainerImpl.fromData(data);
        default:
            return new UnknownComponentImpl(data);
        }
    }
}


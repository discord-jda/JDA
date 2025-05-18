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
    /**
     * Casts this union to a {@link ActionRow}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * ActionRow row = union.asActionRow();
     * ActionRow row2 = (ActionRow) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#ACTION_ROW} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof ActionRow</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link ActionRow}.
     *
     * @return The component as a {@link ActionRow}
     */
    @Nonnull
    ActionRow asActionRow();

    /**
     * Casts this union to a {@link Section}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * Section section = union.asSection();
     * Section section2 = (Section) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#SECTION} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof Section</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link Section}.
     *
     * @return The component as a {@link Section}
     */
    @Nonnull
    Section asSection();

    /**
     * Casts this union to a {@link TextDisplay}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * TextDisplay textDisplay = union.asTextDisplay();
     * TextDisplay textDisplay2 = (TextDisplay) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#TEXT_DISPLAY} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof TextDisplay</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link TextDisplay}.
     *
     * @return The component as a {@link TextDisplay}
     */
    @Nonnull
    TextDisplay asTextDisplay();

    /**
     * Casts this union to a {@link MediaGallery}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * MediaGallery mediaGallery = union.asMediaGallery();
     * MediaGallery mediaGallery2 = (MediaGallery) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#MEDIA_GALLERY} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof MediaGallery</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link MediaGallery}.
     *
     * @return The component as a {@link MediaGallery}
     */
    @Nonnull
    MediaGallery asMediaGallery();

    /**
     * Casts this union to a {@link Separator}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * Separator separator = union.asSeparator();
     * Separator separator2 = (Separator) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#SEPARATOR} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof Separator</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link Separator}.
     *
     * @return The component as a {@link Separator}
     */
    @Nonnull
    Separator asSeparator();

    /**
     * Casts this union to a {@link FileDisplay}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * FileDisplay fileDisplay = union.asFileDisplay();
     * FileDisplay fileDisplay2 = (FileDisplay) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#FILE_DISPLAY} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof FileDisplay</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link FileDisplay}.
     *
     * @return The component as a {@link FileDisplay}
     */
    @Nonnull
    FileDisplay asFileDisplay();

    /**
     * Converts the provided {@link DataObject} into an {@link ContainerChildComponentUnion}.
     *
     * @param  data
     *         The {@link DataObject} to create the component from
     *
     * @return An {@link ContainerChildComponentUnion} representing the provided data
     *
     * @throws IllegalArgumentException
     *         If the provided data is null
     */
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

    /**
     * Converts the provided {@link DataArray} into a {@link List} of {@link ContainerChildComponentUnion}.
     *
     * @param  data
     *         The {@link DataArray} to create the components from
     *
     * @return A {@link List} of {@link ContainerChildComponentUnion} representing the provided data
     *
     * @throws IllegalArgumentException
     *         If the provided data is null
     */
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

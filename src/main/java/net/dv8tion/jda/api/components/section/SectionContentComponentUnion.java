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

package net.dv8tion.jda.api.components.section;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ComponentUnion;
import net.dv8tion.jda.api.components.UnknownComponent;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.UnknownComponentImpl;
import net.dv8tion.jda.internal.components.textdisplay.TextDisplayImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a union of {@link SectionContentComponent SectionContentComponents} that can be either
 * <ul>
 *     <li>{@link TextDisplay}</li>
 *     <li>{@link UnknownComponent}, detectable via {@link #isUnknownComponent()}</li>
 * </ul>
 */
public interface SectionContentComponentUnion extends SectionContentComponent, ComponentUnion
{
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
     * Converts the provided {@link DataObject} into an {@link SectionContentComponentUnion}.
     *
     * @param  data
     *         The {@link DataObject} to create the component from
     *
     * @return An {@link SectionContentComponentUnion} representing the provided data
     *
     * @throws IllegalArgumentException
     *         If the provided data is null
     */
    @Nonnull
    static SectionContentComponentUnion fromData(@Nonnull DataObject data)
    {
        Checks.notNull(data, "Data");

        switch (Component.Type.fromKey(data.getInt("type")))
        {
        case TEXT_DISPLAY:
            return new TextDisplayImpl(data);
        default:
            return new UnknownComponentImpl(data);
        }
    }

    /**
     * Converts the provided {@link DataArray} into a {@link List} of {@link SectionContentComponentUnion}.
     *
     * @param  data
     *         The {@link DataArray} to create the components from
     *
     * @return A {@link List} of {@link SectionContentComponentUnion} representing the provided data
     *
     * @throws IllegalArgumentException
     *         If the provided data is null
     */
    @Nonnull
    static List<SectionContentComponentUnion> fromData(@Nonnull DataArray data)
    {
        Checks.notNull(data, "Data");

        return data
                .stream(DataArray::getObject)
                .map(SectionContentComponentUnion::fromData)
                .collect(Collectors.toList());
    }
}

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

package net.dv8tion.jda.api.components.actionrow;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ComponentUnion;
import net.dv8tion.jda.api.components.UnknownComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.UnknownComponentImpl;
import net.dv8tion.jda.internal.components.buttons.ButtonImpl;
import net.dv8tion.jda.internal.components.selections.EntitySelectMenuImpl;
import net.dv8tion.jda.internal.components.selections.StringSelectMenuImpl;
import net.dv8tion.jda.internal.components.textinput.TextInputImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a union of {@link ActionRowChildComponent ActionRowChildComponents} that can be either
 * <ul>
 *     <li>{@link Button}</li>
 *     <li>{@link StringSelectMenu}</li>
 *     <li>{@link EntitySelectMenu}</li>
 *     <li>{@link UnknownComponent}, detectable via {@link #isUnknownComponent()}</li>
 * </ul>
 */
public interface ActionRowChildComponentUnion extends ActionRowChildComponent, ComponentUnion
{
    /**
     * Casts this union to a {@link Button}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * Button button = union.asButton();
     * Button button2 = (Button) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#BUTTON} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof Button</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link Button}.
     *
     * @return The component as a {@link Button}
     */
    @Nonnull
    Button asButton();

    /**
     * Casts this union to a {@link StringSelectMenu}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * StringSelectMenu stringSelectMenu = union.asStringSelectMenu();
     * StringSelectMenu stringSelectMenu2 = (Button) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#STRING_SELECT} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof StringSelectMenu</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link StringSelectMenu}.
     *
     * @return The component as a {@link StringSelectMenu}
     */
    @Nonnull
    StringSelectMenu asStringSelectMenu();

    /**
     * Casts this union to a {@link StringSelectMenu}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * EntitySelectMenu entitySelectMenu = union.asEntitySelectMenu();
     * EntitySelectMenu entitySelectMenu2 = (Button) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#MENTIONABLE_SELECT} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof EntitySelectMenu</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link EntitySelectMenu}.
     *
     * @return The component as a {@link EntitySelectMenu}
     */
    @Nonnull
    EntitySelectMenu asEntitySelectMenu();

    /**
     * Converts the provided {@link DataObject} into an {@link ActionRowChildComponentUnion}.
     *
     * @param  data
     *         The {@link DataObject} to create the component from
     *
     * @return An {@link ActionRowChildComponentUnion} representing the provided data
     *
     * @throws IllegalArgumentException
     *         If the provided data is null
     */
    @Nonnull
    static ActionRowChildComponentUnion fromData(@Nonnull DataObject data)
    {
        Checks.notNull(data, "Data");

        switch (Component.Type.fromKey(data.getInt("type")))
        {
        case BUTTON:
            return new ButtonImpl(data);
        case STRING_SELECT:
            return new StringSelectMenuImpl(data);
        case TEXT_INPUT:
            return new TextInputImpl(data);
        case USER_SELECT:
        case ROLE_SELECT:
        case CHANNEL_SELECT:
        case MENTIONABLE_SELECT:
            return new EntitySelectMenuImpl(data);
        default:
            return new UnknownComponentImpl(data);
        }
    }

    /**
     * Converts the provided {@link DataArray} into a {@link List} of {@link ActionRowChildComponentUnion}.
     *
     * @param  data
     *         The {@link DataArray} to create the components from
     *
     * @return A {@link List} of {@link ActionRowChildComponentUnion} representing the provided data
     *
     * @throws IllegalArgumentException
     *         If the provided data is null
     */
    @Nonnull
    static List<ActionRowChildComponentUnion> fromData(@Nonnull DataArray data)
    {
        Checks.notNull(data, "Data");

        return data
                .stream(DataArray::getObject)
                .map(ActionRowChildComponentUnion::fromData)
                .collect(Collectors.toList());
    }
}

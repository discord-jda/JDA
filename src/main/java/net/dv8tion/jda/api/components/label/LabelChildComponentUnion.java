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

package net.dv8tion.jda.api.components.label;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.IComponentUnion;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;

import javax.annotation.Nonnull;

/**
 * Represents a union of {@link LabelChildComponent} that can be one of:
 * <ul>
 *     <li>{@link TextInput}</li>
 *     <li>{@link StringSelectMenu}</li>
 *     <li>{@link EntitySelectMenu}</li>
 * </ul>
 */
public interface LabelChildComponentUnion extends LabelChildComponent, IComponentUnion
{
    /**
     * Casts this union to a {@link TextInput}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * TextInput input = union.asTextInput();
     * TextInput input2 = (TextInput) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#TEXT_INPUT} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof TextInput</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link TextInput}.
     *
     * @return The component as a {@link TextInput}
     */
    @Nonnull
    TextInput asTextInput();

    /**
     * Casts this union to a {@link StringSelectMenu}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * StringSelectMenu menu = union.asStringSelectMenu();
     * StringSelectMenu menu2 = (StringSelectMenu) union;
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
     * Casts this union to a {@link EntitySelectMenu}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * EntitySelectMenu menu = union.asEntitySelectMenu();
     * EntitySelectMenu menu2 = (EntitySelectMenu) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is one of:
     * <ul>
     *     <li>{@link Component.Type#USER_SELECT USER_SELECT}</li>
     *     <li>{@link Component.Type#ROLE_SELECT ROLE_SELECT}</li>
     *     <li>{@link Component.Type#MENTIONABLE_SELECT MENTIONABLE_SELECT}</li>
     *     <li>{@link Component.Type#CHANNEL_SELECT CHANNEL_SELECT}</li>
     * </ul>
     * to validate whether you can call this method in addition to normal instanceof checks: <code>component instanceof EntitySelectMenu</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link EntitySelectMenu}.
     *
     * @return The component as a {@link EntitySelectMenu}
     */
    @Nonnull
    EntitySelectMenu asEntitySelectMenu();

    @Nonnull
    @Override
    LabelChildComponentUnion withUniqueId(int uniqueId);
}

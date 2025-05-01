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

package net.dv8tion.jda.api.interactions.modals;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ComponentUnion;
import net.dv8tion.jda.api.components.UnknownComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.UnknownComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a union of {@link ModalTopLevelComponent ModalTopLevelComponents} that can be either
 * <ul>
 *     <li>{@link ActionRow}</li>
 *     <li>{@link UnknownComponent}, detectable via {@link #isUnknownComponent()}</li>
 * </ul>
 */
public interface ModalTopLevelComponentUnion extends ModalTopLevelComponent, ComponentUnion
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

    @Nonnull
    static ModalTopLevelComponentUnion fromData(@Nonnull DataObject data) {
        Checks.notNull(data, "Data");

        int rawType = data.getInt("type", -1);
        Component.Type type = Component.Type.fromKey(rawType);

        switch (type) {
            case ACTION_ROW:
                return (ModalTopLevelComponentUnion) ActionRow.fromData(data);
            default:
                return new UnknownComponentImpl(data);
        }
    }

    @Nonnull
    static List<ModalTopLevelComponentUnion> fromData(@Nonnull DataArray data) {
        Checks.notNull(data, "Data");

        return data
                .stream(DataArray::getObject)
                .map(ModalTopLevelComponentUnion::fromData)
                .collect(Collectors.toList());
    }
}

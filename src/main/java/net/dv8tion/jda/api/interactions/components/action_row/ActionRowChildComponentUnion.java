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

package net.dv8tion.jda.api.interactions.components.action_row;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selects.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selects.StringSelectMenu;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.UnknownComponentImpl;
import net.dv8tion.jda.internal.interactions.component.concrete.ButtonImpl;
import net.dv8tion.jda.internal.interactions.component.concrete.EntitySelectMenuImpl;
import net.dv8tion.jda.internal.interactions.component.concrete.StringSelectMenuImpl;
import net.dv8tion.jda.internal.interactions.component.concrete.TextInputImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a union of {@link Component components} that can be either
 * <ul>
 *     <li>{@link Button}</li>
 *     <li>{@link StringSelectMenu}</li>
 *     <li>{@link EntitySelectMenu}</li>
 *     <li>{@link net.dv8tion.jda.api.interactions.components.UnknownComponent UnknownComponent}, detectable via {@link #isUnknownComponent()}</li>
 * </ul>
 */
public interface ActionRowChildComponentUnion extends ActionRowChildComponent, ComponentUnion
{
    @Nonnull
    Button asButton();

    @Nonnull
    StringSelectMenu asStringSelect();

    @Nonnull
    EntitySelectMenu asEntitySelect();

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
            return new UnknownComponentImpl();
        }
    }

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

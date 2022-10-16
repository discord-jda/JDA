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

package net.dv8tion.jda.internal.interactions.component.select;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenuInteraction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.component.ComponentInteractionImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SelectMenuInteractionImpl extends ComponentInteractionImpl implements StringSelectMenuInteraction
{
    private final List<String> values;
    private final StringSelectMenu menu;

    public SelectMenuInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        values = Collections.unmodifiableList(data.getObject("data").getArray("values")
            .stream(DataArray::getString)
            .collect(Collectors.toList()));

        if (message != null)
        {
            menu = message.getActionRows()
                    .stream()
                    .flatMap(row -> row.getComponents().stream())
                    .filter(StringSelectMenu.class::isInstance)
                    .map(StringSelectMenu.class::cast)
                    .filter(c -> customId.equals(c.getId()))
                    .findFirst()
                    .orElse(null);
        }
        else
        {
            menu = null;
        }
    }

    @Nonnull
    @Override
    public StringSelectMenu getComponent()
    {
        return menu;
    }

    @Nonnull
    @Override
    public Component.Type getComponentType()
    {
        return Component.Type.SELECT_MENU;
    }

    @Nonnull
    @Override
    public List<String> getValues()
    {
        return values;
    }
}

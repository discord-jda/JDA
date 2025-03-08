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

package net.dv8tion.jda.internal.interactions.components.selects;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.selects.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selects.SelectMenuInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.components.ComponentInteractionImpl;

import javax.annotation.Nonnull;

public abstract class SelectMenuInteractionImpl<T, S extends SelectMenu> extends ComponentInteractionImpl implements SelectMenuInteraction<T, S>
{
    private final S menu;

    public SelectMenuInteractionImpl(JDAImpl jda, Class<S> type, DataObject data)
    {
        super(jda, data);
        if (message != null)
        {
            menu = message.getActionRows()
                    .stream()
                    .flatMap(row -> row.getComponents().stream())
                    .filter(type::isInstance)
                    .map(type::cast)
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
    public S getComponent()
    {
        return menu;
    }

    @Nonnull
    @Override
    public Component.Type getComponentType()
    {
        return menu.getType();
    }
}

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

package net.dv8tion.jda.internal.interactions.component.interaction;

import net.dv8tion.jda.api.interactions.components.selects.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.components.selects.StringSelectMenu;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.component.middleman.SelectMenuInteractionImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StringSelectInteractionImpl extends SelectMenuInteractionImpl<String, StringSelectMenu> implements StringSelectInteraction
{
    private final List<String> values;

    public StringSelectInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, StringSelectMenu.class, data);
        this.values = Collections.unmodifiableList(parseValues(data.getObject("data")));
    }

    protected List<String> parseValues(DataObject data)
    {
        return data.optArray("values").map(arr ->
            arr.stream(DataArray::getString)
               .collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

    @Nonnull
    @Override
    public List<String> getValues()
    {
        return values;
    }
}

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

package net.dv8tion.jda.internal.interactions.component;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModalImpl implements Modal
{
    private final String id;
    private final String title;
    private final List<ActionRow> components;

    public ModalImpl(DataObject object)
    {
        this.id = object.getString("id");
        this.title = object.getString("title");
        this.components = Collections.unmodifiableList(object.optArray("components").orElseGet(DataArray::empty)
                    .stream(DataArray::getObject)
                    .map(ActionRow::fromData)
                    .collect(Collectors.toList()));
    }

    public ModalImpl(String id, String title, List<ActionRow> components)
    {
        this.id = id;
        this.title = title;
        this.components = Collections.unmodifiableList(components);
    }

    @Nonnull
    @Override
    public String getId()
    {
        return id;
    }

    @Nonnull
    @Override
    public String getTitle()
    {
        return title;
    }

    @Nonnull
    @Override
    public List<ActionRow> getActionRows()
    {
        return components;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject object = DataObject.empty()
                .put("custom_id", id)
                .put("title", title);

        object.put("components", DataArray.fromCollection(components.stream()
                .map(ActionRow::toData)
                .collect(Collectors.toList())));
        return object;
    }
}

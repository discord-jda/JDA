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

package net.dv8tion.jda.internal.interactions.modal;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponentUnion;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModalImpl implements Modal
{
    private final String id;
    private final String title;
    private final List<ModalTopLevelComponentUnion> components;

    public ModalImpl(DataObject object)
    {
        this.id = object.getString("custom_id");
        this.title = object.getString("title");
        this.components = object.optArray("components").orElseGet(DataArray::empty)
                .stream(DataArray::getObject)
                .map(ModalTopLevelComponentUnion::fromData)
                .collect(Helpers.toUnmodifiableList());
    }

    public ModalImpl(String id, String title, List<ModalTopLevelComponentUnion> components)
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
    public List<ModalTopLevelComponentUnion> getComponents()
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
                .map(Component::toData)
                .collect(Collectors.toList())));
        return object;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", id)
                .addMetadata("title", title)
                .toString();
    }
}

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

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectionMenuImpl implements SelectionMenu
{
    private final String id, placeholder;
    private final int minValues, maxValues;
    private final boolean disabled;
    private final List<SelectOption> options;

    public SelectionMenuImpl(DataObject data)
    {
        this(
            data.getString("custom_id"),
            data.getString("placeholder", null),
            data.getInt("min_values", 1),
            data.getInt("max_values", 1),
            data.getBoolean("disabled"),
            parseOptions(data.getArray("options"))
        );
    }

    public SelectionMenuImpl(String id, String placeholder, int minValues, int maxValues, boolean disabled, List<SelectOption> options)
    {
        this.id = id;
        this.placeholder = placeholder;
        this.minValues = minValues;
        this.maxValues = maxValues;
        this.disabled = disabled;
        this.options = Collections.unmodifiableList(options);
    }

    private static List<SelectOption> parseOptions(DataArray array)
    {
        List<SelectOption> options = new ArrayList<>(array.length());
        array.stream(DataArray::getObject)
            .map(SelectOption::fromData)
            .forEach(options::add);
        return options;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.SELECTION_MENU;
    }

    @Nullable
    @Override
    public String getId()
    {
        return id;
    }

    @Nullable
    @Override
    public String getPlaceholder()
    {
        return placeholder;
    }

    @Override
    public int getMinValues()
    {
        return minValues;
    }

    @Override
    public int getMaxValues()
    {
        return maxValues;
    }

    @Nonnull
    @Override
    public List<SelectOption> getOptions()
    {
        return options;
    }

    @Override
    public boolean isDisabled()
    {
        return disabled;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject data = DataObject.empty();
        data.put("type", 3);
        data.put("custom_id", id);
        data.put("min_values", minValues);
        data.put("max_values", maxValues);
        data.put("disabled", disabled);
        data.put("options", DataArray.fromCollection(options));
        if (placeholder != null)
            data.put("placeholder", placeholder);
        return data;
    }
}

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

package net.dv8tion.jda.internal.interactions.component.middleman;

import net.dv8tion.jda.api.interactions.components.action_row.ActionRowChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.selects.SelectMenu;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SelectMenuImpl extends AbstractComponentImpl implements SelectMenu, ActionRowChildComponentUnion
{
    protected final String id, placeholder;
    protected final int uniqueId;
    protected final int minValues, maxValues;
    protected final boolean disabled;

    public SelectMenuImpl(DataObject data)
    {
        this(
            data.getString("custom_id"),
            data.getInt("id"),
            data.getString("placeholder", null),
            data.getInt("min_values", 1),
            data.getInt("max_values", 1),
            data.getBoolean("disabled")
        );
    }

    public SelectMenuImpl(String id, int uniqueId, String placeholder, int minValues, int maxValues, boolean disabled)
    {
        this.id = id;
        this.uniqueId = uniqueId;
        this.placeholder = placeholder;
        this.minValues = minValues;
        this.maxValues = maxValues;
        this.disabled = disabled;
    }

    @Nullable
    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
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
        data.put("custom_id", id);
        if (uniqueId >= 0)
            data.put("id", uniqueId);
        data.put("min_values", minValues);
        data.put("max_values", maxValues);
        data.put("disabled", disabled);
        if (placeholder != null)
            data.put("placeholder", placeholder);
        return data;
    }

    @Override
    public String toString()
    {
        return new EntityString(SelectMenu.class)
                .setType(getType())
                .addMetadata("id", id)
                .addMetadata("placeholder", placeholder)
                .toString();
    }
}

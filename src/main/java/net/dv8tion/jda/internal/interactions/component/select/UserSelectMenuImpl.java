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

import net.dv8tion.jda.api.interactions.components.selections.UserSelectMenu;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class UserSelectMenuImpl implements UserSelectMenu
{
    private final String id, placeholder;
    private final int minValues, maxValues;
    private final boolean disabled;

    public UserSelectMenuImpl(DataObject data)
    {
        this(
                data.getString("custom_id"),
                data.getString("placeholder", null),
                data.getInt("min_values", 1),
                data.getInt("max_values", 1),
                data.getBoolean("disabled")
        );
    }

    public UserSelectMenuImpl(String id, String placeholder, int minValues, int maxValues, boolean disabled)
    {
        this.id = id;
        this.placeholder = placeholder;
        this.minValues = minValues;
        this.maxValues = maxValues;
        this.disabled = disabled;
    }

    @Nonnull
    @Override
    public Type getType()
    {
    return Type.USER_SELECT_MENU;
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
        data.put("type", 5);
        data.put("custom_id", id);
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
        return "UserSelectMenu:" + id + "(" + placeholder + ")";
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, placeholder, minValues, maxValues, disabled);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof UserSelectMenu))
            return false;
        UserSelectMenu other = (UserSelectMenu) obj;
        return Objects.equals(id, other.getId())
                && Objects.equals(placeholder, other.getPlaceholder())
                && minValues == other.getMinValues()
                && maxValues == other.getMaxValues()
                && disabled == other.isDisabled();
    }
}

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

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class EntitySelectMenuImpl implements EntitySelectMenu
{
    private final String id, placeholder;
    private final int minValues, maxValues;
    private final boolean disabled;
    private final Component.Type type;
    private final EnumSet<ChannelType> channelTypes;

    public EntitySelectMenuImpl(DataObject data)
    {
        EnumSet<ChannelType> channelTypes = EnumSet.noneOf(ChannelType.class);
        for (String type : data.getArray("channel_types").stream(DataArray::getString).toArray(String[]::new))
            channelTypes.add(ChannelType.fromId(Integer.parseInt(type)));


        this.id = data.getString("custom_id");
        this.placeholder = data.getString("placeholder", null);
        this.minValues = data.getInt("min_values", 1);
        this.maxValues = data.getInt("max_values", 1);
        this.disabled = data.getBoolean("disabled");
        this.type = Component.Type.fromKey(data.getInt("type"));
        this.channelTypes = channelTypes;
    }

    public EntitySelectMenuImpl(String id, String placeholder, int minValues, int maxValues, boolean disabled, Component.Type type, EnumSet<ChannelType> channelFilters)
    {
        this.id = id;
        this.placeholder = placeholder;
        this.minValues = minValues;
        this.maxValues = maxValues;
        this.disabled = disabled;
        this.type = type;
        this.channelTypes = channelFilters;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return type;
    }

    @Nullable
    @Override
    public String getId() { return id; }

    @Nullable
    @Override
    public String getPlaceholder()
    {
        return placeholder;
    }

    @Override
    public EnumSet<ChannelType> getChannelTypes() { return channelTypes;}

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
        data.put("type", getType().getKey());
        data.put("custom_id", id);
        data.put("min_values", minValues);
        data.put("max_values", maxValues);
        data.put("disabled", disabled);
        data.put("channel_types", DataArray.fromCollection(this.channelTypes.stream().map(ChannelType::getId).collect(Collectors.toList())));
        if (placeholder != null)
            data.put("placeholder", placeholder);
        return data;
    }

    @Override
    public String toString()
    {
        return "SelectMenu:" + id + "(" + placeholder + ")";
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, placeholder, minValues, maxValues, disabled, getType());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof StringSelectMenu))
            return false;
        StringSelectMenu other = (StringSelectMenu) obj;
        return Objects.equals(id, other.getId())
                && Objects.equals(placeholder, other.getPlaceholder())
                && minValues == other.getMinValues()
                && maxValues == other.getMaxValues()
                && disabled == other.isDisabled()
                && getType() == other.getType();
    }
}

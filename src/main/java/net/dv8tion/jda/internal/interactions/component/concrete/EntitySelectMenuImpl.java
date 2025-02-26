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

package net.dv8tion.jda.internal.interactions.component.concrete;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selects.EntitySelectMenu;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.middleman.SelectMenuImpl;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntitySelectMenuImpl extends SelectMenuImpl implements EntitySelectMenu
{
    protected final Component.Type type;
    protected final EnumSet<ChannelType> channelTypes;
    protected final List<DefaultValue> defaultValues;

    public EntitySelectMenuImpl(DataObject data)
    {
        super(data);
        this.type = Component.Type.fromKey(data.getInt("type"));
        this.channelTypes = Helpers.copyEnumSet(ChannelType.class, data.optArray("channel_types").map(
            arr -> arr.stream(DataArray::getInt).map(ChannelType::fromId).collect(Collectors.toList())
        ).orElse(null));
        this.defaultValues = data.optArray("default_values").map(array ->
            array.stream(DataArray::getObject)
                .map(DefaultValue::fromData)
                .collect(Helpers.toUnmodifiableList())
        ).orElse(Collections.emptyList());
    }

    public EntitySelectMenuImpl(String id, int uniqueId, String placeholder, int minValues, int maxValues, boolean disabled, Type type, EnumSet<ChannelType> channelTypes, List<DefaultValue> defaultValues)
    {
        super(id, uniqueId, placeholder, minValues, maxValues, disabled);
        this.type = type;
        this.channelTypes = channelTypes;
        this.defaultValues = defaultValues;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return type;
    }

    @Nonnull
    @Override
    public EnumSet<SelectTarget> getEntityTypes()
    {
        switch (type)
        {
        case ROLE_SELECT:
            return EnumSet.of(SelectTarget.ROLE);
        case USER_SELECT:
            return EnumSet.of(SelectTarget.USER);
        case CHANNEL_SELECT:
            return EnumSet.of(SelectTarget.CHANNEL);
        case MENTIONABLE_SELECT:
            return EnumSet.of(SelectTarget.ROLE, SelectTarget.USER);
        }
        // Ideally this never happens, so its undocumented
        throw new IllegalStateException("Unsupported type: " + type);
    }

    @Nonnull
    @Override
    public EnumSet<ChannelType> getChannelTypes()
    {
        return channelTypes;
    }

    @Nonnull
    @Override
    public List<DefaultValue> getDefaultValues()
    {
        return defaultValues;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject json = super.toData().put("type", type.getKey());
        if (type == Type.CHANNEL_SELECT && !channelTypes.isEmpty())
            json.put("channel_types", DataArray.fromCollection(channelTypes.stream().map(ChannelType::getId).collect(Collectors.toList())));
        if (!defaultValues.isEmpty())
            json.put("default_values", DataArray.fromCollection(defaultValues));
        return json;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, placeholder, minValues, maxValues, disabled, type, channelTypes, defaultValues);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof EntitySelectMenu))
            return false;
        EntitySelectMenu other = (EntitySelectMenu) obj;
        return Objects.equals(id, other.getId())
                && Objects.equals(placeholder, other.getPlaceholder())
                && minValues == other.getMinValues()
                && maxValues == other.getMaxValues()
                && disabled == other.isDisabled()
                && type == other.getType()
                && channelTypes.equals(other.getChannelTypes())
                && defaultValues.equals(other.getDefaultValues());
    }
}

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

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntitySelectMenuImpl extends SelectMenuImpl implements EntitySelectMenu
{
    protected final Component.Type type;
    protected final EnumSet<ChannelType> channelTypes;

    public EntitySelectMenuImpl(DataObject data)
    {
        super(data);
        this.type = Component.Type.fromKey(data.getInt("type"));
        this.channelTypes = Helpers.copyEnumSet(ChannelType.class, data.optArray("channel_types").map(
            arr -> arr.stream(DataArray::getInt).map(ChannelType::fromId).collect(Collectors.toList())
        ).orElse(null));
    }

    public EntitySelectMenuImpl(String id, String placeholder, int minValues, int maxValues, boolean disabled, Type type, EnumSet<ChannelType> channelTypes)
    {
        super(id, placeholder, minValues, maxValues, disabled);
        this.type = type;
        this.channelTypes = channelTypes;
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

    @NotNull
    @Override
    public DataObject toData()
    {
        DataObject json = super.toData().put("type", type.getKey());
        if (type == Type.CHANNEL_SELECT && !channelTypes.isEmpty())
            json.put("channel_types", DataArray.fromCollection(channelTypes.stream().map(ChannelType::getId).collect(Collectors.toList())));
        return json;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, placeholder, minValues, maxValues, disabled, type, channelTypes);
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
                && channelTypes.equals(other.getChannelTypes());
    }
}

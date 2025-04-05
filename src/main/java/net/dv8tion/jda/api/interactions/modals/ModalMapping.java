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

package net.dv8tion.jda.api.interactions.modals;

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * ID/Value pair for a {@link net.dv8tion.jda.api.events.interaction.ModalInteractionEvent ModalInteractionEvent}.
 *
 * @see    ModalInteractionEvent#getValue(String)
 * @see    ModalInteractionEvent#getValues()
 */
public class ModalMapping
{
    private final String id;
    private final int uniqueId;
    private final String value;
    private final Component.Type type;

    public ModalMapping(DataObject object)
    {
        this.uniqueId = object.getInt("id");
        this.id = object.getString("custom_id");
        this.value = object.getString("value");
        this.type = Component.Type.fromKey(object.getInt("type"));
    }

    /**
     * The custom id of this component
     *
     * @return The custom id of this component
     *
     * @deprecated
     *         Replaced with {@link #getCustomId()}
     */
    @Nonnull
    @Deprecated
    @ForRemoval
    @ReplaceWith("getCustomId()")
    public String getId()
    {
        return id;
    }

    /**
     * The custom id of the component owning this data
     *
     * @return The custom id of the component
     */
    @Nonnull
    public String getCustomId()
    {
        return id;
    }

    /**
     * The unique, numeric id of the component owning this data
     *
     * @return The numeric id of the component
     */
    public int getUniqueId()
    {
        return uniqueId;
    }

    /**
     * The {@link Component.Type Type} of this component
     *
     * @return Type of this component
     */
    @Nonnull
    public Component.Type getType()
    {
        return type;
    }

    /**
     * The String representation of this component.
     *
     * <p>For TextInputs, this returns what the User typed in it.
     *
     * @return The String representation of this component.
     */
    @Nonnull
    public String getAsString()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setType(getType())
                .addMetadata("value", getAsString())
                .toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ModalMapping)) return false;
        ModalMapping that = (ModalMapping) o;
        return type == that.type && Objects.equals(id, that.id) && Objects.equals(uniqueId, that.uniqueId) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, uniqueId, value, type);
    }
}

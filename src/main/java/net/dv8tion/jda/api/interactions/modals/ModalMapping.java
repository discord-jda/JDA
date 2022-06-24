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

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ID/Value pair for a {@link net.dv8tion.jda.api.events.interaction.ModalInteractionEvent ModalInteractionEvent}.
 *
 * @see    ModalInteractionEvent#getValue(String)
 * @see    ModalInteractionEvent#getValues()
 */
public class ModalMapping
{
    private final String id;
    private final DataObject object;
    private final Component.Type type;

    public ModalMapping(DataObject object)
    {
        this.id = object.getString("custom_id");
        this.object = object;
        this.type = Component.Type.fromKey(object.getInt("type"));
    }

    /**
     * The custom id of this component
     *
     * @return The custom id of this component
     */
    @Nonnull
    public String getId()
    {
        return id;
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
     * <p>For {@link net.dv8tion.jda.api.interactions.components.text.TextInput TextInputs}, this returns what the User typed in it.
     *
     * @throws IllegalStateException
     *         If the value of this {@link ModalMapping} is a collection, such as selected values in a select menu.
     *
     * @return The provided value as a string
     */
    @Nonnull
    public String getAsString()
    {
        if (object.isNull("value"))
            typeError("String");
        return object.getString("value");
    }

    /**
     * List of provided values.
     *
     * <p>For {@link net.dv8tion.jda.api.interactions.components.selections.SelectMenu SelectMenus}, this returns the values of all the options the user selected.
     *
     * @throws IllegalStateException
     *         If the value of this {@link ModalMapping} cannot be represented as a List of Strings.
     *
     * @return The provided values as a list of strings
     */
    @Nonnull
    public List<String> getAsStringList()
    {
        if (object.isNull("values"))
            typeError("List<String>");

        return object.getArray("values")
                .stream(DataArray::getString)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ModalMapping)) return false;
        ModalMapping that = (ModalMapping) o;
        if (type != that.type || !Objects.equals(id, that.id))
            return false;

        if (type == Component.Type.TEXT_INPUT)
            return Objects.equals(getAsString(), that.getAsString());

        if (type == Component.Type.SELECT_MENU)
            return Objects.equals(getAsStringList(), that.getAsStringList());

        return false;
    }

    @Override
    public String toString()
    {
        return "ModalMapping[" + getType() + "](id=" + getId() + ")";
    }

    private void typeError(String targetType)
    {
        throw new IllegalStateException("ModalMapping of type " + getType() + " can not be represented as " + targetType + "!");
    }
}

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

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.SelectMenuMentions;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * ID/Value pair for a {@link net.dv8tion.jda.api.events.interaction.ModalInteractionEvent ModalInteractionEvent}.
 *
 * @see    ModalInteractionEvent#getValue(String)
 * @see    ModalInteractionEvent#getValues()
 */
public class ModalMapping
{
    private final InteractionImpl interaction;
    private final String customId;
    private final int uniqueId;
    private final DataObject resolved;
    private final DataObject value;
    private final Component.Type type;

    public ModalMapping(InteractionImpl interaction, DataObject resolved, DataObject object)
    {
        this.interaction = interaction;
        this.uniqueId = object.getInt("id");
        this.customId = object.getString("custom_id");
        this.type = Component.Type.fromKey(object.getInt("type"));
        this.resolved = resolved;
        this.value = object;
    }

    /**
     * The custom id of the component owning this data
     *
     * @return The custom id of the component
     */
    @Nonnull
    public String getCustomId()
    {
        return customId;
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
     * <p>For {@link net.dv8tion.jda.api.components.textinput.TextInput TextInputs}, this returns what the User typed in it.
     *
     * <p>Use {@link #getType()} to check if this method can be used safely!
     *
     * @throws IllegalStateException
     *         If this ModalMapping cannot be represented as a String.
     *
     * @return The String representation of this component.
     */
    @Nonnull
    public String getAsString()
    {
        if (type != Component.Type.TEXT_INPUT)
            typeError("String");

        return value.getString("value");
    }

    /**
     * The String list representation of this component.
     *
     * <p>Return values include:
     * <ul>
     *     <li>
     *         For {@link net.dv8tion.jda.api.components.selections.StringSelectMenu StringSelectMenus},
     *         this returns the values chosen by the User.
     *     </li>
     *     <li>
     *         For {@link net.dv8tion.jda.api.components.selections.EntitySelectMenu EntitySelectMenus},
     *         this returns the entity IDs chosen by the User.
     *     </li>
     * </ul>
     *
     * <p>Use {@link #getType()} to check if this method can be used safely!
     *
     * @throws IllegalStateException
     *         If this ModalMapping cannot be represented as a List of Strings.
     *
     * @return The string list representation of this component.
     */
    @Nonnull
    public List<String> getAsStringList()
    {
        if (type != Component.Type.STRING_SELECT && !type.isEntitySelectMenu())
            typeError("List<String>");

        return value.getArray("values")
                .stream(DataArray::getString)
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * Returns this component's value as a list of Longs.
     *
     * <p>This is available if the component was an {@link net.dv8tion.jda.api.components.selections.EntitySelectMenu EntitySelectMenu}.
     *
     * <p>You can use {@link #getType()} and {@link Component.Type#isEntitySelectMenu()} to check if this method can be used safely.
     *
     * @throws IllegalStateException
     *         If this ModalMapping cannot be represented as such
     *
     * @return This component's value as a list of Longs.
     */
    @Nonnull
    public List<Long> getAsLongList()
    {
        if (!type.isEntitySelectMenu())
            typeError("List<Long>");

        return value.getArray("values")
                .stream(DataArray::getLong)
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * Returns this component's value as a {@link Mentions} object.
     *
     * <p>This is available if the component was an {@link net.dv8tion.jda.api.components.selections.EntitySelectMenu EntitySelectMenu}.
     *
     * <p>You can use {@link #getType()} and {@link Component.Type#isEntitySelectMenu()} to check if this method can be used safely.
     *
     * @throws IllegalStateException
     *         If this ModalMapping cannot be represented as such
     *
     * @return This component's value as a {@link Mentions} object.
     */
    @Nonnull
    public Mentions getAsMentions()
    {
        if (!type.isEntitySelectMenu())
            typeError("Mentions");

        return new SelectMenuMentions(interaction.getJDA(), interaction.getInteractionEntityBuilder(), interaction.getGuild(), resolved, value.getArray("values"));
    }

    /**
     * Returns this component's value as a list of {@link net.dv8tion.jda.api.entities.Message.Attachment Attachment} objects.
     *
     * <p>You can check if {@link #getType()} is equal to {@link Component.Type#FILE_UPLOAD FILE_UPLOAD} to see if this method can be used safely!
     *
     * @throws IllegalStateException
     *         If this ModalMapping cannot be represented as such.
     *
     * @return This component's value as a list of {@link net.dv8tion.jda.api.entities.Message.Attachment Attachment} objects
     */
    @Nonnull
    public List<Message.Attachment> getAsAttachmentList()
    {
        if (type != Component.Type.FILE_UPLOAD)
            typeError("List<Message.Attachment>");

        final DataObject attachments = resolved.getObject("attachments");
        final EntityBuilder entityBuilder = interaction.getJDA().getEntityBuilder();
        return value.getArray("values")
                .stream(DataArray::getString)
                .map(id -> entityBuilder.createMessageAttachment(attachments.getObject(id)))
                .collect(Helpers.toUnmodifiableList());
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setType(getType())
                .addMetadata("customId", customId)
                .toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ModalMapping)) return false;
        ModalMapping that = (ModalMapping) o;
        return type == that.type && Objects.equals(customId, that.customId) && Objects.equals(uniqueId, that.uniqueId) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(customId, uniqueId, value, type);
    }

    private void typeError(String targetType)
    {
        throw new IllegalStateException("ModalMapping of type " + getType() + " can not be represented as " + targetType + "!");
    }
}

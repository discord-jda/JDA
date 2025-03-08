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

package net.dv8tion.jda.internal.components.textinput;

import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponentUnion;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TextInputImpl extends AbstractComponentImpl implements TextInput, ActionRowChildComponentUnion
{
    private final String id;
    private final int uniqueId;
    private final TextInputStyle style;
    private final String label;
    private final int minLength;
    private final int maxLength;
    private final boolean required;
    private final String value;
    private final String placeholder;

    public TextInputImpl(DataObject object)
    {
        this(
                object.getString("custom_id"),
                object.getInt("id"),
                TextInputStyle.fromKey(object.getInt("style", -1)),
                object.getString("label", null),
                object.getInt("min_length", -1),
                object.getInt("max_length", -1),
                object.getBoolean("required", true),
                object.getString("value", null),
                object.getString("placeholder", null)
        );
    }

    public TextInputImpl(
            String id, int uniqueId, TextInputStyle style, String label, int minLength,
            int maxLength, boolean required, String value, String placeholder)
    {
        this.id = id;
        this.uniqueId = uniqueId;
        this.style = style;
        this.label = label;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.required = required;
        this.value = value;
        this.placeholder = placeholder;
    }

    @Nonnull
    @Override
    public TextInput withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new TextInputImpl(id, uniqueId, style, label, minLength, maxLength, required, value, placeholder);
    }

    @Nonnull
    @Override
    public TextInputStyle getStyle()
    {
        return style;
    }

    @Nonnull
    @Override
    public String getCustomId()
    {
        return id;
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public int getMinLength()
    {
        return minLength;
    }

    @Override
    public int getMaxLength()
    {
        return maxLength;
    }

    @Override
    public boolean isRequired()
    {
        return required;
    }

    @Nullable
    @Override
    public String getValue()
    {
        return value;
    }

    @Nullable
    @Override
    public String getPlaceHolder()
    {
        return placeholder;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject obj = DataObject.empty()
                    .put("type", getType().getKey())
                    .put("custom_id", id)
                    .put("style", style.getRaw())
                    .put("required", required)
                    .put("label", label);
        if (minLength != -1)
            obj.put("min_length", minLength);
        if (maxLength != -1)
            obj.put("max_length", maxLength);
        if (value != null)
            obj.put("value", value);
        if (placeholder != null)
            obj.put("placeholder", placeholder);
        return obj;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setType(style)
                .addMetadata("id", id)
                .addMetadata("value", value)
                .toString();
    }
}

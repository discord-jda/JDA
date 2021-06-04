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

package net.dv8tion.jda.api.interactions.components.selections;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SelectOption implements SerializableData
{
    private final String label, value;
    private String description;
    private boolean isDefault;
    private Emoji emoji;

    public SelectOption(@Nonnull String label, @Nonnull String value)
    {
        this(label, value, null, false, null);
    }

    public SelectOption(@Nonnull String label, @Nonnull String value, @Nullable String description, boolean isDefault, @Nullable Emoji emoji)
    {
        Checks.notEmpty(label, "Label");
        Checks.notEmpty(value, "Value");
        Checks.notLonger(label, 25, "Label");
        this.label = label; // max 100
        this.value = value; // max 100
        setDefault(isDefault);
        setEmoji(emoji);
        setDescription(description);
    }

    @Nonnull
    public SelectOption setDescription(String description)
    {
        if (description != null)
            Checks.notLonger(description, 50, "Description");
        this.description = description;
        return this;
    }

    @Nonnull
    public SelectOption setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
        return this;
    }

    @Nonnull
    public SelectOption setEmoji(Emoji emoji)
    {
        this.emoji = emoji;
        return this;
    }

    @Nonnull
    public String getLabel()
    {
        return label;
    }

    @Nonnull
    public String getValue()
    {
        return value;
    }

    @Nullable
    public String getDescription()
    {
        return description;
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    @Nullable
    public Emoji getEmoji()
    {
        return emoji;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject object = DataObject.empty();
        object.put("label", label);
        object.put("value", value);
        object.put("default", isDefault);
        if (emoji != null)
            object.put("emoji", emoji);
        if (description != null && !description.isEmpty())
            object.put("description", description);
        return object;
    }

    @Nonnull
    public static SelectOption fromData(@Nonnull DataObject data)
    {
        return new SelectOption(
                data.getString("label"),
                data.getString("value"),
                data.getString("description", null),
                data.getBoolean("default", false),
                data.optObject("emoji").map(Emoji::fromData).orElse(null)
        );
    }
}

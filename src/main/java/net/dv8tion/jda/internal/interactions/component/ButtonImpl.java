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

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ButtonImpl implements Button
{
    private final String id;
    private final String label;
    private final ButtonStyle style;
    private final String url;
    private final boolean disabled;
    private final Emoji emoji;

    public ButtonImpl(DataObject data)
    {
        this(
            data.getString("custom_id", null),
            data.getString("label", ""),
            ButtonStyle.fromKey(data.getInt("style")),
            data.getString("url", null),
            data.getBoolean("disabled"),
            data.optObject("emoji").map(Emoji::fromData).orElse(null));
    }

    public ButtonImpl(String id, String label, ButtonStyle style, boolean disabled, Emoji emoji)
    {
        this(id, label, style, null, disabled, emoji);
    }

    public ButtonImpl(String id, String label, ButtonStyle style, String url, boolean disabled, Emoji emoji)
    {
        this.id = id;
        this.label = label;
        this.style = style;
        this.url = url;  // max length 512
        this.disabled = disabled;
        this.emoji = emoji;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.BUTTON;
    }

    @Nullable
    @Override
    public String getId()
    {
        return id;
    }

    @Nonnull
    @Override
    public String getLabel()
    {
        return label;
    }

    @Nonnull
    @Override
    public ButtonStyle getStyle()
    {
        return style;
    }

    @Nullable
    @Override
    public String getUrl()
    {
        return url;
    }

    @Nullable
    @Override
    public Emoji getEmoji()
    {
        return emoji;
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
        DataObject json = DataObject.empty();
        json.put("type", 2);
        json.put("label", label);
        json.put("style", style.getKey());
        json.put("disabled", disabled);
        if (emoji != null)
            json.put("emoji", emoji);
        if (url != null)
            json.put("url", url);
        else
            json.put("custom_id", id);
        return json;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, label, style, url, disabled, emoji);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof ButtonImpl)) return false;
        ButtonImpl other = (ButtonImpl) obj;
        return Objects.equals(other.id, id)
            && Objects.equals(other.label, label)
            && Objects.equals(other.url, url)
            && Objects.equals(other.emoji, emoji)
            && other.disabled == disabled
            && other.style == style;
    }

    @Override
    public String toString()
    {
        return "B:" + label + "(" + id + ")";
    }
}

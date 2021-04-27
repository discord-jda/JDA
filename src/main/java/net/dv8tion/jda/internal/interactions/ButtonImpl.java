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

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ButtonImpl implements Button
{
    private final String id;
    private final String label;
    private final Style style;
    private final String url;
    private final boolean disabled;

    public ButtonImpl(DataObject data)
    {
        this(
            data.getString("custom_id", null),
            data.getString("label"),
            Style.fromKey(data.getInt("style")),
            data.getString("url", null),
            data.getBoolean("disabled")
        );
    }

    public ButtonImpl(String id, String label, Style style, boolean disabled)
    {
        this(id, label, style, null, disabled);
    }

    public ButtonImpl(String id, String label, Style style, String url, boolean disabled)
    {
        this.id = id;
        this.label = label;
        this.style = style;
        this.url = url;
        this.disabled = disabled;
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
    public Style getStyle()
    {
        return style;
    }

    @Nullable
    @Override
    public String getUrl()
    {
        return url;
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
        if (url != null)
            json.put("url", url);
        else
            json.put("custom_id", id);
        return json;
    }
}

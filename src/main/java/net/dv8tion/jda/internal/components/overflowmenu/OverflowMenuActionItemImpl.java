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

package net.dv8tion.jda.internal.components.overflowmenu;

import net.dv8tion.jda.api.components.overflowmenu.OverflowMenuActionItem;
import net.dv8tion.jda.api.components.overflowmenu.OverflowMenuGroupChildComponentUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OverflowMenuActionItemImpl extends AbstractComponentImpl
        implements OverflowMenuGroupChildComponentUnion, OverflowMenuActionItem, SerializableData {
    private final int uniqueId;
    private final Style style;
    private final String customId;
    private final String label;
    private final EmojiUnion emoji;
    private final String description;
    private final String url;
    private final boolean disabled;

    public OverflowMenuActionItemImpl(DataObject object) {
        this(
                object.getInt("id", -1),
                Style.fromKey(object.getInt("style")),
                object.getString("custom_id", null),
                object.getString("label"),
                object.optObject("emoji").map(EntityBuilder::createEmoji).orElse(null),
                object.getString("description", null),
                object.getString("url", null),
                object.getBoolean("disabled", false));
    }

    private OverflowMenuActionItemImpl(
            int uniqueId,
            Style style,
            String customId,
            String label,
            Emoji emoji,
            String description,
            String url,
            boolean disabled) {
        this.uniqueId = uniqueId;
        this.style = style;
        this.customId = customId;
        this.label = label;
        this.emoji = (EmojiUnion) emoji;
        this.description = description;
        this.url = url;
        this.disabled = disabled;
    }

    public static OverflowMenuActionItem validated(
            Style style, String customId, String label, Emoji emoji, String description, String url) {
        switch (style) {
            case UNKNOWN:
                throw new IllegalArgumentException("Cannot create an OverflowMenuActionItem of UNKNOWN style");
            case DEFAULT:
            case DANGER:
                Checks.notLonger(customId, ID_MAX_LENGTH, "Custom ID");
                Checks.check(
                        url == null,
                        "Cannot set an URL on a %s-styled OverflowMenuActionItem",
                        style.name().toLowerCase(Locale.ROOT));
                break;
            case LINK:
                Checks.notLonger(url, URL_MAX_LENGTH, "URL");
                Checks.check(customId == null, "Cannot set a custom ID on a link-styled OverflowMenuActionItem");
                break;
        }

        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        if (description != null) {
            Checks.notLonger(description, DESCRIPTION_MAX_LENGTH, "Description");
        }

        return new OverflowMenuActionItemImpl(-1, style, customId, label, emoji, description, url, false);
    }

    @Nonnull
    @Override
    public Type getType() {
        return Type.OVERFLOW_MENU_ITEM;
    }

    @Nonnull
    @Override
    public OverflowMenuActionItemImpl withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new OverflowMenuActionItemImpl(uniqueId, style, customId, label, emoji, description, url, disabled);
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem withDisabled(boolean disabled) {
        return new OverflowMenuActionItemImpl(uniqueId, style, customId, label, emoji, description, url, disabled);
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem withStyle(@Nonnull Style style) {
        Checks.check(style != Style.UNKNOWN, "Cannot create an OverflowMenuActionItem of UNKNOWN style");
        // Allow T => T || DEFAULT <=> DANGER, nothing else
        Checks.check(
                this.style == style
                        || (this.style == Style.DEFAULT && style == Style.DANGER)
                        || (this.style == Style.DANGER && style == Style.DEFAULT),
                "Items can only be switched from and to DEFAULT/DANGER");
        return new OverflowMenuActionItemImpl(uniqueId, style, customId, label, emoji, description, url, disabled);
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem withLabel(@Nonnull String label) {
        Checks.notEmpty(label, "Label");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        return new OverflowMenuActionItemImpl(uniqueId, style, customId, label, emoji, description, url, disabled);
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem withUrl(@Nullable String url) {
        Checks.notEmpty(url, "URL");
        Checks.notLonger(url, URL_MAX_LENGTH, "URL");
        return new OverflowMenuActionItemImpl(uniqueId, style, customId, label, emoji, description, url, disabled);
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem withEmoji(@Nullable Emoji emoji) {
        return new OverflowMenuActionItemImpl(uniqueId, style, customId, label, emoji, description, url, disabled);
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem withDescription(@Nullable String description) {
        if (description != null) {
            Checks.notEmpty(description, "Description");
            Checks.notLonger(description, DESCRIPTION_MAX_LENGTH, "Description");
        }
        return new OverflowMenuActionItemImpl(uniqueId, style, customId, label, emoji, description, url, disabled);
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem asDefault(@Nonnull String customId) {
        Checks.notEmpty(customId, "Custom ID");
        Checks.notLonger(customId, ID_MAX_LENGTH, "Custom ID");
        return new OverflowMenuActionItemImpl(
                uniqueId, Style.DEFAULT, customId, label, emoji, description, null, disabled);
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem asDanger(@Nonnull String customId) {
        Checks.notEmpty(customId, "Custom ID");
        Checks.notLonger(customId, ID_MAX_LENGTH, "Custom ID");
        return new OverflowMenuActionItemImpl(
                uniqueId, Style.DANGER, customId, label, emoji, description, null, disabled);
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem asLink(@Nonnull String url) {
        Checks.notEmpty(url, "URL");
        Checks.notLonger(url, URL_MAX_LENGTH, "URL");
        return new OverflowMenuActionItemImpl(uniqueId, style, null, label, emoji, description, url, disabled);
    }

    @Nonnull
    @Override
    public Style getStyle() {
        return style;
    }

    @Nonnull
    @Override
    public String getLabel() {
        return label;
    }

    @Nullable
    @Override
    public String getUrl() {
        return url;
    }

    @Nullable
    @Override
    public EmojiUnion getEmoji() {
        return emoji;
    }

    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Nullable
    @Override
    public String getCustomId() {
        return customId;
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @Nonnull
    @Override
    public DataObject toData() {
        DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("style", style.getKey())
                .put("label", label);
        if (uniqueId >= 0) {
            json.put("id", uniqueId);
        }
        if (customId != null) {
            json.put("custom_id", customId);
        }
        if (emoji != null) {
            json.put("emoji", emoji);
        }
        if (description != null) {
            json.put("description", description);
        }
        if (url != null) {
            json.put("url", url);
        }
        if (disabled) {
            json.put("disabled", true);
        }
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof OverflowMenuActionItemImpl)) {
            return false;
        }
        OverflowMenuActionItemImpl that = (OverflowMenuActionItemImpl) o;
        return uniqueId == that.uniqueId
                && style == that.style
                && Objects.equals(customId, that.customId)
                && Objects.equals(label, that.label)
                && Objects.equals(emoji, that.emoji)
                && Objects.equals(description, that.description)
                && Objects.equals(url, that.url)
                && disabled == that.disabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, style, customId, label, emoji, description, url, disabled);
    }

    @Override
    public String toString() {
        EntityString entityString = new EntityString(this)
                .setType(style)
                .addMetadata("id", uniqueId)
                .addMetadata("label", label);

        if (url != null) {
            entityString.addMetadata("url", url);
        }
        if (customId != null) {
            entityString.addMetadata("custom id", customId);
        }
        if (emoji != null) {
            entityString.addMetadata("emoji", emoji);
        }
        if (description != null) {
            entityString.addMetadata("description", description);
        }
        if (disabled) {
            entityString.addMetadata("disabled", true);
        }

        return entityString.toString();
    }
}

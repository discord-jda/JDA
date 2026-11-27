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

package net.dv8tion.jda.api.components.overflowmenu;

import net.dv8tion.jda.api.components.ActionComponent;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.internal.components.overflowmenu.OverflowMenuActionItemImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface OverflowMenuActionItem extends ActionComponent, OverflowMenuGroupChildComponent {
    /**
     * The maximum length an item ID can be.
     */
    int ID_MAX_LENGTH = 100;

    /**
     * The maximum length an item label can be.
     */
    int LABEL_MAX_LENGTH = 80;

    /**
     * The maximum length an item URL can be.
     */
    int URL_MAX_LENGTH = 512;

    /**
     * The maximum length an item description can be.
     */
    int DESCRIPTION_MAX_LENGTH = 100;

    @Nonnull
    @CheckReturnValue
    static OverflowMenuActionItem of(
            @Nonnull Style style,
            @Nullable String customId,
            @Nonnull String label,
            @Nullable Emoji emoji,
            @Nullable String description,
            @Nullable String url) {
        return OverflowMenuActionItemImpl.validated(style, customId, label, emoji, description, url);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuActionItem ofDefault(@Nonnull String customId, @Nonnull String label) {
        return ofDefault(customId, label, null, null);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuActionItem ofDefault(
            @Nonnull String customId, @Nonnull String label, @Nullable Emoji emoji, @Nullable String description) {
        return OverflowMenuActionItemImpl.validated(Style.DEFAULT, customId, label, emoji, description, null);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuActionItem ofDanger(@Nonnull String customId, @Nonnull String label) {
        return ofDanger(customId, label, null, null);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuActionItem ofDanger(
            @Nonnull String customId, @Nonnull String label, @Nullable Emoji emoji, @Nullable String description) {
        return OverflowMenuActionItemImpl.validated(Style.DANGER, customId, label, emoji, description, null);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuActionItem ofLink(@Nonnull String label, @Nonnull String url) {
        return ofLink(label, url, null, null);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuActionItem ofLink(
            @Nonnull String label, @Nonnull String url, @Nullable Emoji emoji, @Nullable String description) {
        return OverflowMenuActionItemImpl.validated(Style.LINK, null, label, emoji, description, url);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default OverflowMenuActionItem asDisabled() {
        return (OverflowMenuActionItem) ActionComponent.super.asDisabled();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default OverflowMenuActionItem asEnabled() {
        return (OverflowMenuActionItem) ActionComponent.super.asEnabled();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    OverflowMenuActionItem withUniqueId(int uniqueId);

    @Nonnull
    @Override
    @CheckReturnValue
    OverflowMenuActionItem withDisabled(boolean disabled);

    @Nonnull
    @CheckReturnValue
    OverflowMenuActionItem withStyle(@Nonnull Style style);

    @Nonnull
    @CheckReturnValue
    OverflowMenuActionItem withLabel(@Nonnull String label);

    @Nonnull
    @CheckReturnValue
    OverflowMenuActionItem withUrl(@Nullable String url);

    @Nonnull
    @CheckReturnValue
    OverflowMenuActionItem withEmoji(@Nullable Emoji emoji);

    @Nonnull
    @CheckReturnValue
    OverflowMenuActionItem withDescription(@Nullable String description);

    @Nonnull
    @CheckReturnValue
    OverflowMenuActionItem asDefault(@Nonnull String customId);

    @Nonnull
    @CheckReturnValue
    OverflowMenuActionItem asDanger(@Nonnull String customId);

    @Nonnull
    @CheckReturnValue
    OverflowMenuActionItem asLink(@Nonnull String url);

    @Nonnull
    Style getStyle();

    @Nonnull
    String getLabel();

    @Nullable
    String getUrl();

    @Nullable
    EmojiUnion getEmoji();

    @Nullable
    String getDescription();

    enum Style {
        UNKNOWN(-1),
        DEFAULT(1),
        DANGER(2),
        LINK(3),
        ;

        private final int key;

        Style(int key) {
            this.key = key;
        }

        /**
         * Returns the raw key Discord expects.
         *
         * @return The raw key
         */
        public int getKey() {
            return key;
        }

        /**
         * Returns the style associated with the provided value
         *
         * @param  key
         *         The key to convert
         *
         * @return The item style or {@link #UNKNOWN}
         */
        @Nonnull
        public static Style fromKey(int key) {
            for (Style style : values()) {
                if (style.key == key) {
                    return style;
                }
            }
            return UNKNOWN;
        }
    }
}

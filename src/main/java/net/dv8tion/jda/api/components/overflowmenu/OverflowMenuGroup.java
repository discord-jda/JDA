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

import net.dv8tion.jda.internal.components.overflowmenu.OverflowMenuGroupImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface OverflowMenuGroup {
    int LABEL_MAX_LENGTH = 80;

    int MAX_ITEMS = 8;

    @Nonnull
    @CheckReturnValue
    static OverflowMenuGroup of(@Nonnull OverflowMenuActionItem item, @Nonnull OverflowMenuActionItem... items) {
        return of(null, item, items);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuGroup of(@Nonnull Collection<? extends OverflowMenuActionItem> items) {
        return of(null, items);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuGroup of(
            @Nullable String label, @Nonnull OverflowMenuActionItem item, @Nonnull OverflowMenuActionItem... items) {
        Checks.notNull(item, "Item");
        Checks.noneNull(items, "Items");
        return of(label, Helpers.mergeVararg(item, items));
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuGroup of(@Nullable String label, @Nonnull Collection<? extends OverflowMenuActionItem> items) {
        if (label != null) {
            Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        }
        Checks.notEmpty(items, "Items");
        Checks.noneNull(items, "Items");
        Checks.checkMaxSize(items, "Overflow menu groups can contain at most %d items", MAX_ITEMS);
        return new OverflowMenuGroupImpl(label, items);
    }

    @Nonnull
    @CheckReturnValue
    OverflowMenuGroup withLabel(@Nullable String label);

    @Nonnull
    @CheckReturnValue
    OverflowMenuGroup withItems(@Nonnull Collection<? extends OverflowMenuActionItem> items);

    @Nonnull
    @CheckReturnValue
    default OverflowMenuGroup withItems(
            @Nonnull OverflowMenuActionItem item, @Nonnull OverflowMenuActionItem... items) {
        Checks.notNull(item, "Item");
        Checks.noneNull(items, "Items");
        return withItems(Helpers.mergeVararg(item, items));
    }

    @Nullable
    String getLabel();

    @Nonnull
    @Unmodifiable
    List<OverflowMenuActionItem> getItems();
}

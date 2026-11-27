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

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.replacer.IReplaceable;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent;
import net.dv8tion.jda.internal.components.overflowmenu.OverflowMenuImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface OverflowMenu extends Component, IReplaceable, ActionRowChildComponent, SectionAccessoryComponent {
    int MAX_GROUPS = 8;

    @Nonnull
    @CheckReturnValue
    static OverflowMenu of(@Nonnull OverflowMenuGroup group, @Nonnull OverflowMenuGroup... groups) {
        Checks.notNull(group, "Group");
        Checks.noneNull(groups, "Groups");
        return of(Helpers.mergeVararg(group, groups));
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenu of(@Nonnull Collection<? extends OverflowMenuGroup> groups) {
        Checks.notEmpty(groups, "Groups");
        Checks.noneNull(groups, "Groups");
        Checks.checkMaxSize(groups, "Overflow menus can contain at most %d groups", MAX_GROUPS);
        return new OverflowMenuImpl(groups);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenu ofSingleGroup(
            @Nonnull String label,
            @Nonnull OverflowMenuGroupChildComponent component,
            @Nonnull OverflowMenuGroupChildComponent... components) {
        return of(Collections.singleton(OverflowMenuGroup.of(label, component, components)));
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenu ofSingleGroup(
            @Nonnull String label, @Nonnull Collection<? extends OverflowMenuGroupChildComponent> components) {
        return of(Collections.singleton(OverflowMenuGroup.of(label, components)));
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenu ofSingleGroup(
            @Nonnull OverflowMenuGroupChildComponent component,
            @Nonnull OverflowMenuGroupChildComponent... components) {
        return of(Collections.singleton(OverflowMenuGroup.of(component, components)));
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenu ofSingleGroup(@Nonnull Collection<? extends OverflowMenuGroupChildComponent> components) {
        return of(Collections.singleton(OverflowMenuGroup.of(components)));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    OverflowMenu replace(@Nonnull ComponentReplacer replacer);

    @Nonnull
    @Override
    @CheckReturnValue
    OverflowMenu withUniqueId(int uniqueId);

    @Nonnull
    @CheckReturnValue
    OverflowMenu withGroups(@Nonnull Collection<? extends OverflowMenuGroup> groups);

    @Nonnull
    @CheckReturnValue
    default OverflowMenu withGroups(@Nonnull OverflowMenuGroup group, @Nonnull OverflowMenuGroup... groups) {
        Checks.notNull(group, "Group");
        Checks.noneNull(groups, "Groups");
        return withGroups(Helpers.mergeVararg(group, groups));
    }

    @Nonnull
    @Unmodifiable
    List<OverflowMenuGroup> getGroups();
}

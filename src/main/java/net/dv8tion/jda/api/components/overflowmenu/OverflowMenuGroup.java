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

    int MAX_COMPONENTS = 8;

    @Nonnull
    @CheckReturnValue
    static OverflowMenuGroup of(
            @Nonnull OverflowMenuGroupChildComponent component,
            @Nonnull OverflowMenuGroupChildComponent... components) {
        return of(null, component, components);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuGroup of(@Nonnull Collection<? extends OverflowMenuGroupChildComponent> components) {
        return of(null, components);
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuGroup of(
            @Nullable String label,
            @Nonnull OverflowMenuGroupChildComponent component,
            @Nonnull OverflowMenuGroupChildComponent... components) {
        Checks.notNull(component, "Component");
        Checks.noneNull(components, "Components");
        return of(label, Helpers.mergeVararg(component, components));
    }

    @Nonnull
    @CheckReturnValue
    static OverflowMenuGroup of(
            @Nullable String label, @Nonnull Collection<? extends OverflowMenuGroupChildComponent> components) {
        if (label != null) {
            Checks.notBlank(label, "Label");
            Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        }
        Checks.notEmpty(components, "Components");
        Checks.noneNull(components, "Components");
        Checks.checkMaxSize(components, "Overflow menu groups can contain at most %d components", MAX_COMPONENTS);
        return new OverflowMenuGroupImpl(label, components);
    }

    @Nonnull
    @CheckReturnValue
    OverflowMenuGroup withLabel(@Nullable String label);

    @Nonnull
    @CheckReturnValue
    OverflowMenuGroup withComponents(@Nonnull Collection<? extends OverflowMenuGroupChildComponent> components);

    @Nonnull
    @CheckReturnValue
    default OverflowMenuGroup withComponents(
            @Nonnull OverflowMenuGroupChildComponent component,
            @Nonnull OverflowMenuGroupChildComponent... components) {
        Checks.notNull(component, "Component");
        Checks.noneNull(components, "Components");
        return withComponents(Helpers.mergeVararg(component, components));
    }

    @Nullable
    String getLabel();

    @Nonnull
    @Unmodifiable
    List<OverflowMenuGroupChildComponent> getComponents();
}

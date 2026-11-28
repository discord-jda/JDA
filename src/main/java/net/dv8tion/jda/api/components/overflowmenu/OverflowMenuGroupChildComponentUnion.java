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
import net.dv8tion.jda.api.components.IComponentUnion;
import net.dv8tion.jda.api.components.UnknownComponent;
import net.dv8tion.jda.api.components.menu.MenuActionItem;

import javax.annotation.Nonnull;

/**
 * Represents a union of {@link OverflowMenuGroupChildComponent OverflowMenuGroupChildComponents} that can be one of:
 * <ul>
 *     <li>{@link MenuActionItem}</li>
 *     <li>{@link UnknownComponent}, detectable via {@link #isUnknownComponent()}</li>
 * </ul>
 */
public interface OverflowMenuGroupChildComponentUnion extends IComponentUnion {
    /**
     * Casts this union to a {@link MenuActionItem}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * {@snippet lang = "java":
     * //These are the same!
     * MenuActionItem item = union.asMenuActionItem();
     * MenuActionItem item2 = (MenuActionItem) union;
     *}
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#OVERFLOW_MENU_ITEM} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof MenuActionItem</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link MenuActionItem}.
     *
     * @return The component as a {@link MenuActionItem}
     */
    @Nonnull
    MenuActionItem asMenuActionItem();

    @Nonnull
    @Override
    OverflowMenuGroupChildComponentUnion withUniqueId(int uniqueId);
}

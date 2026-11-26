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

package net.dv8tion.jda.internal.components.utils;

import net.dv8tion.jda.api.components.Component;

import java.util.*;
import java.util.stream.Collectors;

public class ActionRowUtil {
    private static final Map<Component.Type, Set<Component.Type>> COMPATIBILITIES;

    static {
        Map<Component.Type, Set<Component.Type>> compatibilities = new EnumMap<>(Component.Type.class);
        registerCompatibility(compatibilities, Component.Type.BUTTON, Component.Type.OVERFLOW_MENU);

        // Freeze everything
        COMPATIBILITIES = Collections.unmodifiableMap(compatibilities.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Collections.unmodifiableSet(e.getValue()))));
    }

    private static void registerCompatibility(
            Map<Component.Type, Set<Component.Type>> compatibilities, Component.Type first, Component.Type second) {
        compatibilities
                .computeIfAbsent(first, k -> EnumSet.noneOf(Component.Type.class))
                .add(second);
        compatibilities
                .computeIfAbsent(second, k -> EnumSet.noneOf(Component.Type.class))
                .add(first);
    }

    public static boolean canBeMixed(Component.Type first, Component.Type second) {
        return first == second
                || COMPATIBILITIES.getOrDefault(first, Collections.emptySet()).contains(second);
    }

    public static void checkMixedComponents(Set<Component.Type> types) {
        if (types.size() == 1) return;

        for (Component.Type type : types) {
            Set<Component.Type> compatibleTypes = COMPATIBILITIES.get(type);
            // There is more than 1 component type and this component has no compatibilities,
            // therefore it is incompatible with everything else
            if (compatibleTypes == null) {
                throw new IllegalArgumentException("Action rows cannot contain other component types when " + type
                        + " is present! Complete row: " + types);
            }

            // Let's see if the components contain anything that is incompatible
            for (Component.Type siblingType : types) {
                if (type == siblingType) continue;
                if (!compatibleTypes.contains(siblingType)) {
                    throw new IllegalArgumentException("Action rows containing " + type + " cannot also contain "
                            + siblingType + "! Complete row: " + types);
                }
            }
        }
    }
}

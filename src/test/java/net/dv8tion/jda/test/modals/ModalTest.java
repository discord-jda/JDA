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

package net.dv8tion.jda.test.modals;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.attribute.IDisableable;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.test.components.ComponentTestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class ModalTest {
    static Map<Component.Type, Class<? extends LabelChildComponent>> LABEL_CHILDREN = Map.of(
            Component.Type.STRING_SELECT, StringSelectMenu.class,
            Component.Type.USER_SELECT, EntitySelectMenu.class,
            Component.Type.ROLE_SELECT, EntitySelectMenu.class,
            Component.Type.MENTIONABLE_SELECT, EntitySelectMenu.class,
            Component.Type.CHANNEL_SELECT, EntitySelectMenu.class);

    @MethodSource("modalComponents")
    @ParameterizedTest
    public void testDisabledComponents(Component component) {
        Assertions.assertInstanceOf(IDisableable.class, component);
        Assertions.assertInstanceOf(LabelChildComponent.class, component);
        LabelChildComponent mutatedComponent = ((LabelChildComponent) ((IDisableable) component).asDisabled());
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Modal.create("id", "title")
                    .addComponents(Label.of("label", mutatedComponent))
                    .build();
        });
    }

    @MethodSource("modalComponents")
    @ParameterizedTest
    public void testEnabledComponents(Component component) {
        // Enabled is the default state
        Assertions.assertInstanceOf(LabelChildComponent.class, component);
        Assertions.assertDoesNotThrow(() -> {
            Modal.create("id", "title")
                    .addComponents(Label.of("label", (LabelChildComponent) component))
                    .build();
        });
    }

    @Test
    public void testOneInvalidComponentInList() {
        var components = new ArrayList<>(testComponents().toList());
        var mutatedComponentIndex = ThreadLocalRandom.current().nextInt(components.size());
        Assertions.assertInstanceOf(IDisableable.class, components.get(mutatedComponentIndex));
        components.set(mutatedComponentIndex, ((IDisableable) components.get(mutatedComponentIndex)).asDisabled());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Modal.create("id", "title")
                    .addComponents(components.stream()
                            .map(LabelChildComponent.class::cast)
                            .map((child) -> Label.of("Label", child))
                            .toList())
                    .build();
        });
    }

    static Stream<Arguments> modalComponents() {
        return testComponents().map(Arguments::arguments);
    }

    static Stream<Component> testComponents() {
        return LABEL_CHILDREN.entrySet().stream()
                .map(entry -> ComponentTestData.getMinimalComponent(entry.getValue(), entry.getKey()));
    }
}

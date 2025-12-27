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
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import net.dv8tion.jda.test.components.ComponentTestData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class ModalTest extends AbstractSnapshotTest {
    static Map<Component.Type, Class<? extends SelectMenu>> LABEL_CHILDREN = Map.of(
            Component.Type.STRING_SELECT, StringSelectMenu.class,
            Component.Type.USER_SELECT, EntitySelectMenu.class,
            Component.Type.ROLE_SELECT, EntitySelectMenu.class,
            Component.Type.MENTIONABLE_SELECT, EntitySelectMenu.class,
            Component.Type.CHANNEL_SELECT, EntitySelectMenu.class);

    @MethodSource("modalComponents")
    @ParameterizedTest
    public void testDisabledComponents(Component.Type componentType, SelectMenu component) {

        LabelChildComponent mutatedComponent = component.asDisabled();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Modal.create("id", "title")
                        .addComponents(Label.of("label", mutatedComponent))
                        .build())
                .satisfies(exception -> assertWithSnapshot(exception.toString(), componentType.toString()));
    }

    @MethodSource("modalComponents")
    @ParameterizedTest
    public void testEnabledComponents(Component.Type ignored, SelectMenu component) {
        // Enabled is the default state
        assertThatNoException().isThrownBy(() -> Modal.create("id", "title")
                .addComponents(Label.of("label", component))
                .build());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    public void testInvalidComponentInList(int targetIndex) {
        List<SelectMenu> components = new ArrayList<>(testComponents().toList());
        assertThat(components).allSatisfy(component -> assertThat(component).isInstanceOf(IDisableable.class));
        components.set(targetIndex, components.get(targetIndex).asDisabled());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> Modal.create("id", "title")
                        .addComponents(components.stream()
                                .map((child) -> Label.of("Label", child))
                                .toList())
                        .build())
                .satisfies(exception -> assertWithSnapshot(exception.toString(), Integer.toString(targetIndex)));
    }

    static Stream<Arguments> modalComponents() {
        return LABEL_CHILDREN.entrySet().stream()
                .map(entry -> Arguments.of(entry.getKey(), minimalComponent().apply(entry)));
    }

    static Stream<SelectMenu> testComponents() {
        return LABEL_CHILDREN.entrySet().stream()
                .sorted(
                        // Required so the indexes are deterministic.
                        Map.Entry.comparingByKey())
                .map(minimalComponent());
    }

    static Function<Map.Entry<Component.Type, Class<? extends SelectMenu>>, ? extends SelectMenu> minimalComponent() {
        return (entry) -> ComponentTestData.getMinimalComponent(entry.getValue(), entry.getKey());
    }
}

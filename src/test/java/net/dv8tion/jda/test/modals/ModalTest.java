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

import net.dv8tion.jda.api.components.attribute.IDisableable;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import net.dv8tion.jda.test.components.ComponentTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class ModalTest extends AbstractSnapshotTest {
    @MethodSource("modalComponents")
    @ParameterizedTest
    void testDisabledComponents(SelectMenu component) {
        LabelChildComponent mutatedComponent = component.asDisabled();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Modal.create("id", "title")
                        .addComponents(Label.of("label", mutatedComponent))
                        .build())
                .satisfies(exception -> assertWithSnapshot(
                        exception.toString(), component.getType().name()));
    }

    @MethodSource("modalComponents")
    @ParameterizedTest
    void testEnabledComponents(SelectMenu component) {
        assertThatNoException().isThrownBy(() -> Modal.create("id", "title")
                .addComponents(Label.of("label", component))
                .build());
    }

    @Test
    void testMultipleInvalidComponents() {
        List<LabelChildComponent> components = testComponents()
                .map(component -> ((IDisableable) component).asDisabled())
                .map(LabelChildComponent.class::cast)
                .toList();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> Modal.create("id", "title")
                        .addComponents(components.stream()
                                .map((child) -> Label.of("Label", child))
                                .toList())
                        .build())
                .satisfies(exception -> assertWithSnapshot(exception.toString(), null));
    }

    static Stream<Arguments> modalComponents() {
        return testComponents().map(Arguments::of);
    }

    static Stream<LabelChildComponent> testComponents() {
        return ComponentTestData.getMinimalComponents()
                .filter(LabelChildComponent.class::isInstance)
                .filter(IDisableable.class::isInstance)
                .map(LabelChildComponent.class::cast);
    }
}

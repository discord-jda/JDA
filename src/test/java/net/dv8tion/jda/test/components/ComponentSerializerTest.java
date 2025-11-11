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

package net.dv8tion.jda.test.components;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.components.utils.ComponentSerializer;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentSerializerTest extends AbstractComponentTest {
    @MethodSource("getSerializerTestCases")
    @ParameterizedTest
    void testSerializer(Component component) {
        ComponentSerializer serializer = new ComponentSerializer();

        DataObject dataObject = serializer.serialize(component);
        List<FileUpload> fileUploads = serializer.getFileUploads(component);

        assertSerialization(
                serializer,
                Collections.singletonList(component),
                component.getType().name());

        ComponentDeserializer deserializer = new ComponentDeserializer(fileUploads);
        Component deserialized = deserializer.deserializeAs(Component.class, dataObject);

        assertThat(deserialized).isEqualTo(component);
    }

    @MethodSource("getSerializerTestCases")
    @ParameterizedTest
    void testToStringMethods(Component component) {
        assertWithSnapshot(component.toString(), component.getType().toString());
    }

    static Stream<Arguments> getSerializerTestCases() {
        return Arrays.stream(Component.Type.values())
                .map(type -> ComponentTestData.getMinimalComponent(Component.class, type))
                .map(Arguments::of);
    }
}

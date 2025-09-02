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
import net.dv8tion.jda.api.components.IComponentUnion;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.components.utils.ComponentSerializer;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentSerializerTest extends AbstractSnapshotTest
{
    @MethodSource("getSerializerTestCases")
    @ParameterizedTest
    void testSerializer(Component component)
    {
        List<Component> components = Arrays.asList(component);

        ComponentSerializer serializer = new ComponentSerializer(components);

        List<DataObject> dataObjects = serializer.getDataObjects();
        List<String> fileNames = serializer.getFileUploads()
                .stream()
                .map(FileUpload::getName)
                .collect(Collectors.toList());

        assertWithSnapshot(DataArray.fromCollection(dataObjects), component.getType() + "-data");
        assertWithSnapshot(DataArray.fromCollection(fileNames), component.getType() + "-files");

        ComponentDeserializer deserializer = new ComponentDeserializer(serializer.getFileUploads());
        List<IComponentUnion> deserialized = deserializer.deserializeAll(dataObjects);

        assertThat(deserialized).isEqualTo(components);
    }

    static Stream<Arguments> getSerializerTestCases()
    {
        return Arrays.stream(Component.Type.values())
            .map(type -> ComponentTestData.getMinimalComponent(Component.class, type))
            .map(Arguments::of);
    }
}

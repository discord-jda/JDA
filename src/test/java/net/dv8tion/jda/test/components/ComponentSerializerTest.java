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
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.components.tree.ModalComponentTree;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.components.utils.ComponentSerializer;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class ComponentSerializerTest extends AbstractSnapshotTest
{
    @MethodSource("getSerializerTestCases")
    @ParameterizedTest
    void testSerializer(Component component)
    {
        List<Component> components = Collections.singletonList(component);

        ComponentSerializer serializer = new ComponentSerializer();

        List<DataObject> dataObjects = serializer.serializeAll(components);
        List<FileUpload> fileUploads = serializer.getFileUploads(components);

        List<String> fileNames = fileUploads
                .stream()
                .map(FileUpload::getName)
                .collect(Collectors.toList());

        assertWithSnapshot(DataArray.fromCollection(dataObjects), component.getType() + "-data");
        assertWithSnapshot(DataArray.fromCollection(fileNames), component.getType() + "-files");

        ComponentDeserializer deserializer = new ComponentDeserializer(fileUploads);
        List<IComponentUnion> deserialized = deserializer.deserializeAll(dataObjects);

        assertThat(deserialized).isEqualTo(components);
    }

    @MethodSource("getSerializerTestCases")
    @ParameterizedTest
    void testToStringMethods(Component component)
    {
        assertWithSnapshot(component.toString(), component.getType().toString());
    }

    @EnumSource
    @ParameterizedTest
    @SuppressWarnings("rawtypes")
    void testParseTreeIsUpdated(ComponentTree.Type type)
    {
        Class<? extends ComponentTree> treeClass;
        switch(type)
        {
        case ANY:
            treeClass = ComponentTree.class;
            break;
        case MESSAGE:
            treeClass = MessageComponentTree.class;
            break;
        case MODAL:
            treeClass = ModalComponentTree.class;
            break;
        default:
            fail("Please update this test with the new component type (" + type.name() + "), then update Components#parseTree if necessary");
            return;
        }

        // We only want to test that [[Components#parseTree]] recognizes the tree type,
        // don't construct a real tree as that may throw on empty trees
        try (MockedStatic<?> ignored = Mockito.mockStatic(treeClass))
        {
            Assertions.assertThatNoException().isThrownBy(
                    () -> new ComponentDeserializer(Collections.emptyList()).deserializeAsTree(treeClass, DataArray.empty()));
        }
    }

    static Stream<Arguments> getSerializerTestCases()
    {
        return Arrays.stream(Component.Type.values())
            .map(type -> ComponentTestData.getMinimalComponent(Component.class, type))
            .map(Arguments::of);
    }
}

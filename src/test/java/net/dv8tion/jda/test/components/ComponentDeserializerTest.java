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

import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.components.tree.ModalComponentTree;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.components.utils.ComponentSerializer;
import net.dv8tion.jda.api.utils.data.DataArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.fail;

class ComponentDeserializerTest extends AbstractComponentTest
{
    @Test
    void testDeserializeMessageComponentTree() throws Exception
    {
        ComponentDeserializer deserializer = new ComponentDeserializer(Collections.emptyList());
        ComponentSerializer serializer = new ComponentSerializer();
        try (InputStream sample = loadSample("exampleMessageTree.json"))
        {
            DataArray data = DataArray.fromJson(sample);

            MessageComponentTree tree = deserializer.deserializeAsTree(MessageComponentTree.class, data);

            assertThat(tree).isNotNull();

            List<MessageTopLevelComponentUnion> components = tree.getComponents();
            assertSerialization(serializer, components, null);
        }
    }

    @EnumSource
    @ParameterizedTest
    @SuppressWarnings("rawtypes")
    void testParseTreeIsUpdated(ComponentTree.Type type)
    {
        ComponentDeserializer deserializer = new ComponentDeserializer(Collections.emptyList());

        Class<? extends ComponentTree> treeClass = getTreeClass(type);

        // We only want to test that [[Components#parseTree]] recognizes the tree type,
        // don't construct a real tree as that may throw on empty trees
        try (MockedStatic<?> ignored = Mockito.mockStatic(treeClass))
        {
            assertThatNoException().isThrownBy(
                () -> deserializer.deserializeAsTree(treeClass, DataArray.empty()));
        }
    }

    @SuppressWarnings("rawtypes")
    private static Class<? extends ComponentTree> getTreeClass(ComponentTree.Type type)
    {
        switch(type)
        {
        case ANY:
            return ComponentTree.class;
        case MESSAGE:
            return MessageComponentTree.class;
        case MODAL:
            return ModalComponentTree.class;
        default:
            return fail("Please update this test with the new component tree type (" + type.name() + "), then update ComponentDeserializer#deserializeAsTree if necessary");
        }
    }
}

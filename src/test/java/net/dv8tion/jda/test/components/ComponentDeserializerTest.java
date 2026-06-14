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
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.components.tree.ModalComponentTree;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.components.utils.ComponentSerializer;
import net.dv8tion.jda.api.exceptions.DataObjectParsingException;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.filedisplay.FileDisplayImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ComponentDeserializerTest extends AbstractComponentTest {

    private static final String EXAMPLE_FILE_URL =
            "https://github.com/discord-jda/JDA/blob/970afafb99ff35cd55cb91eb167022253247bce4/assets/readme/logo.png";
    private static final FileUpload EMPTY_FILE = FileUpload.fromData(new byte[0], "logo.png");

    @Test
    void testDeserializeMessageComponentTree() throws Exception {
        ComponentDeserializer deserializer = new ComponentDeserializer(Collections.emptyList());
        ComponentSerializer serializer = new ComponentSerializer();
        try (InputStream sample = loadSample("exampleMessageTree.json")) {
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
    void testParseTreeIsUpdated(ComponentTree.Type type) {
        ComponentDeserializer deserializer = new ComponentDeserializer(Collections.emptyList());

        Class<? extends ComponentTree> treeClass = getTreeClass(type);

        // We only want to test that [[Components#parseTree]] recognizes the tree type,
        // don't construct a real tree as that may throw on empty trees
        try (MockedStatic<?> ignored = Mockito.mockStatic(treeClass)) {
            assertThatNoException().isThrownBy(() -> deserializer.deserializeAsTree(treeClass, DataArray.empty()));
        }
    }

    @SuppressWarnings("rawtypes")
    private static Class<? extends ComponentTree> getTreeClass(ComponentTree.Type type) {
        return switch (type) {
            case ANY -> ComponentTree.class;
            case MESSAGE -> MessageComponentTree.class;
            case MODAL -> ModalComponentTree.class;
        };
    }

    @MethodSource("componentsWithUrlMediaToBeSent")
    @ParameterizedTest
    void testRequireMediaProxyFeature(DataObject json) {
        ComponentDeserializer lenientDeserializer = new ComponentDeserializer(List.of());
        ComponentDeserializer strictDeserializer = new ComponentDeserializer(
                List.of(), EnumSet.of(ComponentDeserializer.DeserializerFeature.REQUIRE_MEDIA_PROXY_URL));

        assertThatNoException().isThrownBy(() -> lenientDeserializer.deserializeAs(Component.class, json));

        assertThatExceptionOfType(DataObjectParsingException.class)
                .isThrownBy(() -> strictDeserializer.deserializeAs(Component.class, json))
                .withMessageContaining("proxy_url is missing or null");
    }

    static List<Arguments> componentsWithUrlMediaToBeSent() {
        ComponentSerializer serializer = new ComponentSerializer();

        MediaGallery gallery = MediaGallery.of(MediaGalleryItem.fromUrl(EXAMPLE_FILE_URL));
        FileDisplay fileDisplay = new FileDisplayImpl(EXAMPLE_FILE_URL);
        Thumbnail thumbnail = Thumbnail.fromUrl(EXAMPLE_FILE_URL);

        return List.of(
                Arguments.argumentSet("Media gallery", serializer.serialize(gallery)),
                Arguments.argumentSet("File display", serializer.serialize(fileDisplay)),
                Arguments.argumentSet("Thumbnail", serializer.serialize(thumbnail)));
    }

    @MethodSource("componentsWithLocalMediaToBeSent")
    @ParameterizedTest
    void testRequireMediaProxyFeatureIsNotCheckedForLocalAttachments(DataObject json) {
        ComponentDeserializer strictDeserializer = new ComponentDeserializer(
                List.of(EMPTY_FILE), EnumSet.of(ComponentDeserializer.DeserializerFeature.REQUIRE_MEDIA_PROXY_URL));

        assertThatNoException().isThrownBy(() -> strictDeserializer.deserializeAs(Component.class, json));
    }

    static List<Arguments> componentsWithLocalMediaToBeSent() {
        ComponentSerializer serializer = new ComponentSerializer();

        MediaGallery gallery = MediaGallery.of(MediaGalleryItem.fromUrl("attachment://logo.png"));
        FileDisplay fileDisplay = FileDisplay.fromFileName("logo.png");
        Thumbnail thumbnail = Thumbnail.fromUrl("attachment://logo.png");

        return List.of(
                Arguments.argumentSet("Media gallery", serializer.serialize(gallery)),
                Arguments.argumentSet("File display", serializer.serialize(fileDisplay)),
                Arguments.argumentSet("Thumbnail", serializer.serialize(thumbnail)));
    }
}

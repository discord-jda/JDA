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

package net.dv8tion.jda.api.components;

import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.components.utils.ComponentSerializer;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static net.dv8tion.jda.internal.entities.EntityBuilder.DEFAULT_COMPONENT_DESERIALIZER;

/**
 * Utility class for components.
 *
 * <p>This utility builds on {@link ComponentDeserializer} to deserialize components.
 * The implementation provided by {@link ComponentDeserializer} allows to deserialize components with implicit {@link FileUpload FileUploads} like {@link Thumbnail}.
 *
 * @see ComponentDeserializer
 * @see ComponentSerializer
 */
public class Components {
    /**
     * Converts the provided {@link DataObject} into a component of type {@link T}.
     * <br>Note that any unsupported component will be represented as an {@link net.dv8tion.jda.api.components.UnknownComponent UnknownComponent}.
     *
     * @param  data
     *         The {@link DataArray} to create the component tree from
     *
     * @throws IllegalArgumentException
     *         If the provided data is {@code null}, or the component is not of type {@link T}
     *
     * @return A {@link ComponentTree} representing the provided data
     *
     * @see ComponentDeserializer#deserializeAs(Class, DataObject)
     */
    @Nonnull
    public static <T extends Component> T parseComponent(
            @Nonnull Class<T> componentType, @Nonnull DataObject data) {
        return DEFAULT_COMPONENT_DESERIALIZER.deserializeAs(componentType, data);
    }

    /**
     * Converts the provided {@link DataArray} into a {@link List} of components.
     * <br>Note that any unsupported component will be represented as an {@link net.dv8tion.jda.api.components.UnknownComponent UnknownComponent}.
     *
     * @param  data
     *         The {@link DataArray} to create the components from
     *
     * @throws IllegalArgumentException
     *         If the provided data is {@code null}, or one of the components is not of type {@link T}
     *
     * @return A {@link List} of {@link T} representing the provided data
     *
     * @see ComponentDeserializer#deserializeAs(Class, DataArray)
     */
    @Nonnull
    public static <T extends Component> List<T> parseComponents(
            @Nonnull Class<T> componentType, @Nonnull DataArray data) {
        return DEFAULT_COMPONENT_DESERIALIZER
                .deserializeAs(componentType, data)
                .collect(Collectors.toList());
    }

    /**
     * Converts the provided {@link DataArray} into a {@link ComponentTree}.
     * <br>Note that any unsupported component will be represented as an {@link net.dv8tion.jda.api.components.UnknownComponent UnknownComponent}.
     *
     * @param  data
     *         The {@link DataArray} to create the component tree from
     *
     * @throws IllegalArgumentException
     *         If the provided data is {@code null}, or one of the components is not compatible.
     * @throws UnsupportedOperationException
     *         If the provided tree type is not supported
     *
     * @return A {@link ComponentTree} representing the provided data
     *
     * @see ComponentDeserializer#deserializeAsTree(Class, DataArray)
     */
    @Nonnull
    public static <T extends ComponentTree<?>> T parseTree(
            @Nonnull Class<T> treeType, @Nonnull DataArray data) {
        return DEFAULT_COMPONENT_DESERIALIZER.deserializeAsTree(treeType, data);
    }
}

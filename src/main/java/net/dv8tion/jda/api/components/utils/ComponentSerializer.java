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

package net.dv8tion.jda.api.components.utils;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.message.MessageUtil;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to serialize a list of {@link Component Components} into {@link DataObject}.
 *
 * <p>Since some components include implicit file uploads, such as {@link FileDisplay} and {@link Thumbnail},
 * the included {@link FileUpload} instances can be accessed using {@link #getFileUploads()}.
 * <br>Each uploaded file is referenced in the respective components using {@code attachment://filename}.
 *
 * <p>This separation is done to simplify persistence of these components in preferred formats.
 * For instance, you might want to store the components as JSON Blobs but the files in an object storage.
 *
 * <p>You can use {@link ComponentDeserializer} to deserialize the output again,
 * make sure you also provide any implicit {@link FileUpload} instances.
 *
 * @see ComponentDeserializer
 */
public class ComponentSerializer
{
    private final List<? extends Component> components;

    /**
     * Create a new ComponentSerializer for the provided list of components.
     *
     * @param  components
     *         The components to serialize
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided or any of the components are not serializable
     */
    public ComponentSerializer(@Nonnull List<? extends Component> components)
    {
        Checks.noneNull(components, "Components");
        for (int i = 0; i < components.size(); i++)
        {
            Component component = components.get(i);
            Checks.check(component instanceof SerializableData, "Component at index %s is not serializable.", i);
        }

        this.components = components;
    }

    /**
     * Create a new ComponentSerializer for the provided tree of components.
     *
     * @param  componentTree
     *         The {@link ComponentTree} to serialize
     *
     * @throws NullPointerException
     *         If {@code null} is provided
     * @throws IllegalArgumentException
     *         If any of the components are not serializable
     */
    public ComponentSerializer(@Nonnull ComponentTree<?> componentTree)
    {
        this(componentTree.getComponents());
    }

    /**
     * Serializes the provided components into {@link DataObject} instances.
     *
     * <p>Some components that would implicitly upload a file, for instance {@link Thumbnail},
     * will reference the file using a URI with this format {@code attachment://filename}.
     * The {@code filename} refers to a {@link FileUpload} provided by {@link #getFileUploads()},
     * with a corresponding {@link FileUpload#getName() name}.
     *
     * @return {@link List} of {@link DataObject}
     */
    @Nonnull
    public List<DataObject> getDataObjects()
    {
        return components.stream()
            .map(SerializableData.class::cast)
            .map(SerializableData::toData)
            .collect(Collectors.toList());
    }

    /**
     * Returns the implicit {@link FileUpload} instances used by {@link #getDataObjects()}.
     *
     * @return The implicit {@link FileUpload} instances for the provided components
     */
    @Nonnull
    public List<FileUpload> getFileUploads()
    {
        return MessageUtil.getIndirectFiles(components);
    }
}

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
import net.dv8tion.jda.api.components.IComponentUnion;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.components.tree.ModalComponentTree;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.UnknownComponentImpl;
import net.dv8tion.jda.internal.components.actionrow.ActionRowImpl;
import net.dv8tion.jda.internal.components.attachmentupload.AttachmentUploadImpl;
import net.dv8tion.jda.internal.components.buttons.ButtonImpl;
import net.dv8tion.jda.internal.components.container.ContainerImpl;
import net.dv8tion.jda.internal.components.filedisplay.FileDisplayFileUpload;
import net.dv8tion.jda.internal.components.filedisplay.FileDisplayImpl;
import net.dv8tion.jda.internal.components.label.LabelImpl;
import net.dv8tion.jda.internal.components.mediagallery.MediaGalleryImpl;
import net.dv8tion.jda.internal.components.mediagallery.MediaGalleryItemFileUpload;
import net.dv8tion.jda.internal.components.mediagallery.MediaGalleryItemImpl;
import net.dv8tion.jda.internal.components.section.SectionImpl;
import net.dv8tion.jda.internal.components.selections.EntitySelectMenuImpl;
import net.dv8tion.jda.internal.components.selections.StringSelectMenuImpl;
import net.dv8tion.jda.internal.components.separator.SeparatorImpl;
import net.dv8tion.jda.internal.components.textdisplay.TextDisplayImpl;
import net.dv8tion.jda.internal.components.textinput.TextInputImpl;
import net.dv8tion.jda.internal.components.thumbnail.ThumbnailFileUpload;
import net.dv8tion.jda.internal.components.thumbnail.ThumbnailImpl;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Utility class to deserialize components that were previously serialized by {@link ComponentSerializer}.
 *
 * @see ComponentSerializer
 */
public class ComponentDeserializer {
    private static final String ATTACHMENT_SCHEMA = "attachment://";
    private final Map<String, FileUpload> files;

    /**
     * Create a new deserializer instance with the provided files.
     *
     * @param files
     *        The implicit file uploads used by the components (see {@link ComponentSerializer#getFileUploads(Collection)})
     */
    public ComponentDeserializer(@Nonnull Collection<? extends FileUpload> files) {
        this.files = new LinkedHashMap<>(files.size());
        for (FileUpload file : files) {
            this.files.put(file.getName(), file);
        }
    }

    /**
     * Deserializes all the provided components.
     *
     * @param  components
     *         The list of components to deserialize
     *
     * @throws ParsingException
     *         If any of the components have an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The deserialized components as {@link IComponentUnion}
     *
     * @see    #deserializeAs(Class, List)
     */
    @Nonnull
    public List<IComponentUnion> deserializeAll(@Nonnull List<DataObject> components) {
        Checks.noneNull(components, "Components");
        return components.stream().map(this::parseComponent).collect(Collectors.toList());
    }

    /**
     * Deserializes all the provided components to the provided component type.
     *
     * @param  type
     *         The target component type
     * @param  components
     *         The list of components to deserialize
     *
     * @throws ParsingException
     *         If any of the components have an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the resulting component cannot be cast to the target type
     *
     * @return The deserialized components
     *
     * @see    #deserializeAs(Class, DataObject)
     */
    @Nonnull
    public <T extends Component> Stream<T> deserializeAs(@Nonnull Class<T> type, @Nonnull List<DataObject> components) {
        Checks.notNull(type, "Type");
        Checks.noneNull(components, "Components");
        return components.stream()
                .map(this::parseComponent)
                .map(component -> ComponentsUtil.safeUnionCastWithUnknownType("component", component, type));
    }

    /**
     * Deserializes all the provided components to the provided component type.
     *
     * @param  type
     *         The target component type
     * @param  components
     *         The array of components to deserialize
     *
     * @throws ParsingException
     *         If any of the components have an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the resulting component cannot be cast to the target type
     *
     * @return The deserialized components
     *
     * @see    #deserializeAs(Class, List)
     */
    @Nonnull
    public <T extends Component> Stream<T> deserializeAs(@Nonnull Class<T> type, @Nonnull DataArray components) {
        Checks.notNull(type, "Type");
        Checks.notNull(components, "Components");
        return components.stream(DataArray::getObject)
                .map(this::parseComponent)
                .map(component -> ComponentsUtil.safeUnionCastWithUnknownType("component", component, type));
    }

    /**
     * Deserializes the provided components to the provided component type.
     *
     * @param  type
     *         The target component type
     * @param  component
     *         The component to deserialize
     *
     * @throws ParsingException
     *         If the component has an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the resulting component cannot be cast to the target type
     *
     * @return The deserialized components
     *
     * @see    #deserializeAs(Class, List)
     */
    @Nonnull
    public <T extends Component> T deserializeAs(@Nonnull Class<T> type, @Nonnull DataObject component) {
        Checks.notNull(type, "Type");
        Checks.notNull(component, "Component");
        IComponentUnion componentUnion = parseComponent(component);
        return ComponentsUtil.safeUnionCastWithUnknownType("component", componentUnion, type);
    }

    /**
     * Deserializes the provided components to the provided component type.
     *
     * @param  treeType
     *         The target component tree type (for instance {@link MessageComponentTree})
     * @param  components
     *         The list of components to deserialize
     *
     * @throws ParsingException
     *         If any of the components has an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the resulting component cannot be cast to the target type
     *
     * @return The deserialized components as a component tree
     *
     * @see    #deserializeAs(Class, List)
     */
    @Nonnull
    public <T extends ComponentTree<?>> T deserializeAsTree(
            @Nonnull Class<T> treeType, @Nonnull List<DataObject> components) {
        Checks.notNull(components, "Components");
        return deserializeAsTree(treeType, DataArray.fromCollection(components));
    }

    /**
     * Deserializes the provided components to the provided component type.
     *
     * @param  treeType
     *         The target component tree type (for instance {@link MessageComponentTree})
     * @param  components
     *         The list of components to deserialize
     *
     * @throws ParsingException
     *         If any of the components has an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the resulting component cannot be cast to the target type
     *
     * @return The deserialized components as a component tree
     *
     * @see    #deserializeAs(Class, List)
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public <T extends ComponentTree<?>> T deserializeAsTree(@Nonnull Class<T> treeType, @Nonnull DataArray components) {
        Checks.notNull(treeType, "Tree type");
        Checks.notNull(components, "Components");

        if (MessageComponentTree.class.isAssignableFrom(treeType)) {
            return (T) MessageComponentTree.of(
                    deserializeAs(MessageTopLevelComponent.class, components).collect(Collectors.toList()));
        } else if (ModalComponentTree.class.isAssignableFrom(treeType)) {
            return (T) ModalComponentTree.of(
                    deserializeAs(ModalTopLevelComponent.class, components).collect(Collectors.toList()));
        } else if (ComponentTree.class.isAssignableFrom(treeType)) {
            return (T)
                    ComponentTree.of(deserializeAs(Component.class, components).collect(Collectors.toList()));
        } else {
            throw new UnsupportedOperationException("Cannot deserialize to tree of type " + treeType.getName());
        }
    }

    @Nonnull
    private IComponentUnion parseComponent(@Nonnull DataObject data) {
        switch (Component.Type.fromKey(data.getInt("type"))) {
            case ACTION_ROW:
                return new ActionRowImpl(this, data);
            case BUTTON:
                return new ButtonImpl(data);
            case STRING_SELECT:
                return new StringSelectMenuImpl(data);
            case TEXT_INPUT:
                return new TextInputImpl(data);
            case USER_SELECT:
            case ROLE_SELECT:
            case MENTIONABLE_SELECT:
            case CHANNEL_SELECT:
                return new EntitySelectMenuImpl(data);
            case SECTION:
                return new SectionImpl(this, data);
            case TEXT_DISPLAY:
                return new TextDisplayImpl(data);
            case THUMBNAIL:
                return (IComponentUnion) toThumbnail(data);
            case MEDIA_GALLERY:
                return (IComponentUnion) toMediaGallery(data);
            case FILE_DISPLAY:
                return (IComponentUnion) toFileDisplay(data);
            case SEPARATOR:
                return new SeparatorImpl(data);
            case CONTAINER:
                return new ContainerImpl(this, data);
            case LABEL:
                return new LabelImpl(this, data);
            case FILE_UPLOAD:
                return new AttachmentUploadImpl(data);
            default:
                return new UnknownComponentImpl(data);
        }
    }

    @Nonnull
    private Thumbnail toThumbnail(@Nonnull DataObject data) {
        String url = data.getObject("media").getString("url");
        if (url.startsWith(ATTACHMENT_SCHEMA)) {
            return new ThumbnailFileUpload(
                    data.getInt("id", -1),
                    getFileByUri(url),
                    data.getString("description", null),
                    data.getBoolean("spoiler"));
        }

        return new ThumbnailImpl(data);
    }

    @Nonnull
    private FileDisplay toFileDisplay(@Nonnull DataObject data) {
        String url = data.getObject("file").getString("url");
        if (url.startsWith(ATTACHMENT_SCHEMA)) {
            return new FileDisplayFileUpload(data.getInt("id", -1), getFileByUri(url), data.getBoolean("spoiler"));
        }

        return new FileDisplayImpl(data);
    }

    @Nonnull
    private MediaGallery toMediaGallery(@Nonnull DataObject data) {
        return new MediaGalleryImpl(
                data.getInt("id", -1),
                data.getArray("items").stream(DataArray::getObject)
                        .map(this::toMediaGalleryItem)
                        .collect(Collectors.toList()));
    }

    @Nonnull
    private MediaGalleryItem toMediaGalleryItem(@Nonnull DataObject data) {
        String url = data.getObject("media").getString("url");
        if (url.startsWith(ATTACHMENT_SCHEMA)) {
            return new MediaGalleryItemFileUpload(
                    getFileByUri(url), data.getString("description", null), data.getBoolean("spoiler"));
        }

        return new MediaGalleryItemImpl(data);
    }

    @Nonnull
    private FileUpload getFileByUri(@Nonnull String uri) {
        String name = uri.substring(ATTACHMENT_SCHEMA.length());
        FileUpload file = files.get(name);
        Checks.check(file != null, "File for URI %s is missing", uri);
        return file;
    }
}

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

import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.components.tree.ModalComponentTree;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.UnknownComponentImpl;
import net.dv8tion.jda.internal.components.actionrow.ActionRowImpl;
import net.dv8tion.jda.internal.components.buttons.ButtonImpl;
import net.dv8tion.jda.internal.components.container.ContainerImpl;
import net.dv8tion.jda.internal.components.filedisplay.FileDisplayImpl;
import net.dv8tion.jda.internal.components.mediagallery.MediaGalleryImpl;
import net.dv8tion.jda.internal.components.section.SectionImpl;
import net.dv8tion.jda.internal.components.selections.EntitySelectMenuImpl;
import net.dv8tion.jda.internal.components.selections.StringSelectMenuImpl;
import net.dv8tion.jda.internal.components.separator.SeparatorImpl;
import net.dv8tion.jda.internal.components.textdisplay.TextDisplayImpl;
import net.dv8tion.jda.internal.components.textinput.TextInputImpl;
import net.dv8tion.jda.internal.components.thumbnail.ThumbnailImpl;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for components.
 */
public class Components
{
    /**
     * Converts the provided {@link DataObject} into a component of type {@link T}.
     * <br>Note that any unsupported component will be represented as an {@link net.dv8tion.jda.api.components.UnknownComponent UnknownComponent}.
     *
     * @param  data
     *         The {@link DataArray} to create the component tree from
     *
     * @return A {@link ComponentTree} representing the provided data
     *
     * @throws IllegalArgumentException
     *         If the provided data is {@code null}, or the component is not of type {@link T}
     */
    @Nonnull
    public static <T extends Component> T parseComponent(@Nonnull Class<T> componentType, @Nonnull DataObject data)
    {
        Checks.notNull(componentType, "Component type");
        Checks.notNull(data, "Data");

        final IComponentUnion component = parseComponent(data);
        return ComponentsUtil.safeUnionCastWithUnknownType("component", component, componentType);
    }

    @Nonnull
    private static IComponentUnion parseComponent(@Nonnull DataObject data)
    {
        switch (Component.Type.fromKey(data.getInt("type")))
        {
        case ACTION_ROW:
            return new ActionRowImpl(data);
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
            return new SectionImpl(data);
        case TEXT_DISPLAY:
            return new TextDisplayImpl(data);
        case THUMBNAIL:
            return new ThumbnailImpl(data);
        case MEDIA_GALLERY:
            return new MediaGalleryImpl(data);
        case FILE_DISPLAY:
            return new FileDisplayImpl(data);
        case SEPARATOR:
            return new SeparatorImpl(data);
        case CONTAINER:
            return new ContainerImpl(data);
        default:
            return new UnknownComponentImpl(data);
        }
    }

    /**
     * Converts the provided {@link DataArray} into a {@link List} of components.
     * <br>Note that any unsupported component will be represented as an {@link net.dv8tion.jda.api.components.UnknownComponent UnknownComponent}.
     *
     * @param  data
     *         The {@link DataArray} to create the components from
     *
     * @return A {@link List} of {@link T} representing the provided data
     *
     * @throws IllegalArgumentException
     *         If the provided data is {@code null}, or one of the components is not of type {@link T}
     */
    @Nonnull
    public static <T extends Component> List<T> parseComponents(@Nonnull Class<T> componentType, @Nonnull DataArray data)
    {
        Checks.notNull(componentType, "Component type");
        Checks.notNull(data, "Data");

        return parseTo(componentType, data, Function.identity());
    }

    /**
     * Converts the provided {@link DataArray} into a {@link ComponentTree}.
     * <br>Note that any unsupported component will be represented as an {@link net.dv8tion.jda.api.components.UnknownComponent UnknownComponent}.
     *
     * @param  data
     *         The {@link DataArray} to create the component tree from
     *
     * @return A {@link ComponentTree} representing the provided data
     *
     * @throws IllegalArgumentException
     *         If the provided data is {@code null}, or one of the components is not compatible.
     * @throws UnsupportedOperationException
     *         If the provided tree type is not supported
     *
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends ComponentTree<?>> T parseTree(@Nonnull Class<T> treeType, @Nonnull DataArray data)
    {
        Checks.notNull(treeType, "Tree type");
        Checks.notNull(data, "Data");

        if (MessageComponentTree.class.isAssignableFrom(treeType))
            return (T) parseTo(MessageTopLevelComponentUnion.class, data, MessageComponentTree::of);
        else if (ModalComponentTree.class.isAssignableFrom(treeType))
            return (T) parseTo(ModalTopLevelComponentUnion.class, data, ModalComponentTree::of);
        else if (ComponentTree.class.isAssignableFrom(treeType))
            return (T) parseTo(IComponentUnion.class, data, ComponentTree::of);
        else
            throw new UnsupportedOperationException("Cannot deserialize to tree of type " + treeType.getName());
    }

    @Nonnull
    private static <R, U extends Component> R parseTo(
            @Nonnull Class<U> topLevelComponentType,
            @Nonnull DataArray data,
            @Nonnull Function<List<U>, R> treeFactory
    )
    {
        final List<U> components = data.stream(DataArray::getObject)
                .map(obj -> parseComponent(topLevelComponentType, obj))
                .collect(Collectors.toList());
        return treeFactory.apply(components);
    }
}

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

package net.dv8tion.jda.api.components.section;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.IComponentUnion;
import net.dv8tion.jda.api.components.UnknownComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;

import javax.annotation.Nonnull;

/**
 * Represents a union of {@link SectionAccessoryComponent SectionAccessoryComponents} that can be one of:
 * <ul>
 *     <li>{@link Button}</li>
 *     <li>{@link Thumbnail}</li>
 *     <li>{@link UnknownComponent}, detectable via {@link #isUnknownComponent()}</li>
 * </ul>
 */
public interface SectionAccessoryComponentUnion extends SectionAccessoryComponent, IComponentUnion {
    /**
     * Casts this union to a {@link Button}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * {@snippet lang="java":
     * //These are the same!
     * Button button = union.asButton();
     * Button button2 = (Button) union;
     * }
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#BUTTON} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof Button</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link Button}.
     *
     * @return The component as a {@link Button}
     */
    @Nonnull
    Button asButton();

    /**
     * Casts this union to a {@link Thumbnail}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * {@snippet lang="java":
     * //These are the same!
     * Thumbnail thumbnail = union.asThumbnail();
     * Thumbnail thumbnail2 = (Thumbnail) union;
     * }
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#THUMBNAIL} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof Thumbnail</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link Thumbnail}.
     *
     * @return The component as a {@link Thumbnail}
     */
    @Nonnull
    Thumbnail asThumbnail();

    @Nonnull
    @Override
    SectionAccessoryComponentUnion withUniqueId(int uniqueId);
}

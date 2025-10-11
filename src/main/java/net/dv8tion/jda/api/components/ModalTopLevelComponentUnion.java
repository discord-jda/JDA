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

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import javax.annotation.Nonnull;

/**
 * Represents a union of {@link ModalTopLevelComponent ModalTopLevelComponents} that can be one of:
 * <ul>
 *     <li>{@link Label}</li>
 *     <li>{@link TextDisplay}</li>
 *     <li>{@link UnknownComponent}, detectable via {@link #isUnknownComponent()}</li>
 * </ul>
 */
public interface ModalTopLevelComponentUnion extends ModalTopLevelComponent, IComponentUnion {
    /**
     * Casts this union to a {@link Label}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * Label label = union.asLabel();
     * Label label2 = (Label) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link net.dv8tion.jda.api.components.Component.Type#LABEL LABEL} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof Label</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link Label}.
     *
     * @return The component as a {@link Label}
     */
    @Nonnull
    Label asLabel();

    /**
     * Casts this union to a {@link TextDisplay}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * TextDisplay textDisplay = union.asTextDisplay();
     * TextDisplay textDisplay2 = (TextDisplay) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the component is of type {@link net.dv8tion.jda.api.components.Component.Type#TEXT_DISPLAY TEXT_DISPLAY} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof TextDisplay</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link TextDisplay}.
     *
     * @return The component as a {@link TextDisplay}
     */
    @Nonnull
    TextDisplay asTextDisplay();

    @Nonnull
    @Override
    ModalTopLevelComponentUnion withUniqueId(int uniqueId);
}

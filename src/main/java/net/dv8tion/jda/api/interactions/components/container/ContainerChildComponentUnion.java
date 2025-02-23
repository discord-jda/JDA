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

package net.dv8tion.jda.api.interactions.components.container;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRow;
import net.dv8tion.jda.api.interactions.components.file.File;
import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGallery;
import net.dv8tion.jda.api.interactions.components.section.Section;
import net.dv8tion.jda.api.interactions.components.separator.Separator;
import net.dv8tion.jda.api.interactions.components.text_display.TextDisplay;

import javax.annotation.Nonnull;

/**
 * Represents a union of {@link Component components} that can be either
 * <ul>
 *     <li>{@link ActionRow}</li>
 *     <li>{@link Section}</li>
 *     <li>{@link TextDisplay}</li>
 *     <li>{@link MediaGallery}</li>
 *     <li>{@link Separator}</li>
 *     <li>{@link File}</li>
 *     <li>{@link net.dv8tion.jda.api.interactions.components.UnknownComponent UnknownComponent}, detectable via {@link #isUnknownComponent()}</li>
 * </ul>
 */
public interface ContainerChildComponentUnion extends ContainerChildComponent, ComponentUnion
{
    @Nonnull
    ActionRow asActionRow();

    @Nonnull
    Section asSection();

    @Nonnull
    TextDisplay asTextDisplay();

    @Nonnull
    MediaGallery asMediaGallery();

    @Nonnull
    Separator asSeparator();

    @Nonnull
    File asFile();
}

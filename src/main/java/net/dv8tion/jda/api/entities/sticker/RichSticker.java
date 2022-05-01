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

package net.dv8tion.jda.api.entities.sticker;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Covers more details of stickers which are missing in messages.
 *
 * <p>This is used when stickers are fetched directly from the API or cache, instead of message objects.
 */
public interface RichSticker extends Sticker
{
    /**
     * The {@link Sticker.Type Sticker Type}
     *
     * @return The type
     */
    @Nonnull
    Sticker.Type getType();

    /**
     * Set of tags of the sticker. Tags can be used instead of the name of the sticker as aliases.
     *
     * @return Possibly-empty unmodifiable Set of tags of the sticker
     */
    @Nonnull
    Set<String> getTags();

    /**
     * The description of the sticker, or empty String if the sticker doesn't have one.
     *
     * @return Possibly-empty String containing the description of the sticker
     */
    @Nonnull
    String getDescription();
}

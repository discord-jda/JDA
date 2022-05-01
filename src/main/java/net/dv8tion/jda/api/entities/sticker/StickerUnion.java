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

/**
 * Represents possible {@link RichSticker} types.
 *
 * <p>This delegates the sticker methods for a rich sticker type,
 * but can be converted to a concrete type using either {@link #asGuildSticker()} or {@link #asStandardSticker()}.
 */
public interface StickerUnion extends RichSticker
{
    /**
     * Returns the underlying {@link StandardSticker} if applicable.
     *
     * @throws IllegalStateException
     *         If this is not a {@link StandardSticker}
     *
     * @return The {@link StandardSticker}
     */
    @Nonnull
    StandardSticker asStandardSticker();

    /**
     * Returns the underlying {@link GuildSticker} if applicable.
     *
     * @throws IllegalStateException
     *         If this is not a {@link GuildSticker}
     *
     * @return The {@link GuildSticker}
     */
    @Nonnull
    GuildSticker asGuildSticker();
}

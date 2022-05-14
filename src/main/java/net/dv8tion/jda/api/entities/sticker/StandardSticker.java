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
 * Standard stickers used for nitro and wave buttons on welcome messages.
 *
 * <p>This includes stickers from {@link StickerPack StickerPacks}, such as wumpus or doggos.
 */
public interface StandardSticker extends RichSticker
{
    @Nonnull
    @Override
    default Type getType()
    {
        return Type.STANDARD;
    }

    /**
     * The ID of the pack the sticker is from.
     *
     * @return the ID of the pack the sticker is from
     */
    long getPackIdLong();

    /**
     * The ID of the pack the sticker is from.
     *
     * @return the ID of the pack the sticker is from
     */
    @Nonnull
    default String getPackId()
    {
        return Long.toUnsignedString(getPackIdLong());
    }

    /**
     * The sticker's sort order within its pack
     *
     * @return The sort order value
     */
    int getSortValue();
}

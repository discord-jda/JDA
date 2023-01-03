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

package net.dv8tion.jda.internal.entities.sticker;

import net.dv8tion.jda.api.entities.sticker.StandardSticker;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Set;

public class StandardStickerImpl extends RichStickerImpl implements StandardSticker
{
    private final long packId;
    private final int sortValue;

    public StandardStickerImpl(long id, StickerFormat format, String name,
                               Set<String> tags, String description,
                               long packId, int sortValue)
    {
        super(id, format, name, tags, description);
        this.packId = packId;
        this.sortValue = sortValue;
    }

    @Nonnull
    @Override
    public StandardSticker asStandardSticker()
    {
        return this;
    }

    @Override
    public long getPackIdLong()
    {
        return packId;
    }

    @Override
    public int getSortValue()
    {
        return sortValue;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(name)
                .addMetadata("pack", getPackId())
                .toString();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof StandardStickerImpl))
            return false;
        StandardStickerImpl other = (StandardStickerImpl) obj;
        return id == other.id; // Standard stickers shouldn't change, so we can just compare id
    }
}

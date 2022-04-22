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

import java.util.Set;

public class StandardStickerImpl extends RichStickerImpl implements StandardSticker
{
    private final long packId;
    private final int sortValue;

    public StandardStickerImpl(long id, StickerFormat format, String name,
                               Type type, Set<String> tags, String description,
                               long packId, int sortValue)
    {
        super(id, format, name, type, tags, description);
        this.packId = packId;
        this.sortValue = sortValue;
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
}

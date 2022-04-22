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
import net.dv8tion.jda.api.entities.sticker.StickerPack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class StickerPackImpl implements StickerPack
{
    private final long id;
    private final List<StandardSticker> stickers;
    private final String name, description;
    private final long coverId, bannerId, skuId;

    public StickerPackImpl(long id, List<StandardSticker> stickers,
                           String name, String description,
                           long coverId, long bannerId, long skuId)
    {
        this.id = id;
        this.stickers = Collections.unmodifiableList(stickers);
        this.name = name;
        this.description = description;
        this.coverId = coverId;
        this.bannerId = bannerId;
        this.skuId = skuId;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @NotNull
    @Override
    public List<StandardSticker> getStickers()
    {
        return stickers;
    }

    @NotNull
    @Override
    public String getName()
    {
        return name;
    }

    @NotNull
    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public long getCoverIdLong()
    {
        return coverId;
    }

    @Override
    public long getBannerIdLong()
    {
        return bannerId;
    }

    @Override
    public long getSkuIdLong()
    {
        return skuId;
    }
}

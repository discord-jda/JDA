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

import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.entities.sticker.RichSticker;
import net.dv8tion.jda.api.entities.sticker.Sticker;
import net.dv8tion.jda.api.entities.sticker.StickerUnion;

import javax.annotation.Nonnull;
import java.util.Set;

public class StickerUnionImpl implements StickerUnion
{
    private final RichSticker sticker;

    public StickerUnionImpl(RichSticker sticker)
    {
        this.sticker = sticker;
    }

    @Override
    public long getIdLong()
    {
        return sticker.getIdLong();
    }

    @Nonnull
    @Override
    public StickerFormat getFormatType()
    {
        return sticker.getFormatType();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return sticker.getName();
    }

    @Nonnull
    @Override
    public Sticker.Type getType()
    {
        return sticker.getType();
    }

    @Nonnull
    @Override
    public Set<String> getTags()
    {
        return sticker.getTags();
    }

    @Nonnull
    @Override
    public String getDescription()
    {
        return sticker.getDescription();
    }

    @Nonnull
    @Override
    public StandardStickerImpl asStandardSticker()
    {
        if (sticker instanceof StandardStickerImpl)
            return (StandardStickerImpl) sticker;
        throw new IllegalStateException("Cannot convert sticker of type " + getType() + " to StandardSticker!");
    }

    @Nonnull
    @Override
    public GuildSticker asGuildSticker()
    {
        if (sticker instanceof GuildSticker)
            return (GuildSticker) sticker;
        throw new IllegalStateException("Cannot convert sticker of type " + getType() + " to GuildSticker!");
    }
}

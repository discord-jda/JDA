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

import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Objects;

public class StickerItemImpl implements StickerItem
{
    protected final long id;
    protected final StickerFormat format;
    protected String name;

    public StickerItemImpl(long id, StickerFormat format, String name)
    {
        this.id = id;
        this.format = format;
        this.name = name;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public StickerFormat getFormatType()
    {
        return format;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    public StickerItemImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(name)
                .toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, format, name);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof StickerItemImpl))
            return false;
        StickerItemImpl other = (StickerItemImpl) obj;
        return id == other.id
            && format == other.format
            && Objects.equals(name, other.name);
    }
}

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

import net.dv8tion.jda.api.entities.sticker.RichSticker;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class RichStickerImpl extends StickerItemImpl implements RichSticker
{
    protected final Type type;
    protected final Set<String> tags;
    protected final String description;

    public RichStickerImpl(long id, StickerFormat format, String name,
                           Type type, Set<String> tags, String description)
    {
        super(id, format, name);
        this.type = type;
        this.tags = Collections.unmodifiableSet(tags);
        this.description = description;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return type;
    }

    @Nonnull
    @Override
    public Set<String> getTags()
    {
        return tags;
    }

    @Nonnull
    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString()
    {
        return "RichSticker:" + type + ':' + name + '(' + getId() + ')';
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, format, name, type, tags, description);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof RichStickerImpl))
            return false;
        RichStickerImpl other = (RichStickerImpl) obj;
        return id == other.id
                && format == other.format
                && type == other.type
                && Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Helpers.deepEqualsUnordered(tags, other.tags);
    }
}

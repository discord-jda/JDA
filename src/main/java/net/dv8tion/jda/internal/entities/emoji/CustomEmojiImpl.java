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

package net.dv8tion.jda.internal.entities.emoji;

import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CustomEmojiImpl implements CustomEmoji, EmojiUnion
{
    private final String name;
    private final long id;
    private final boolean animated;

    public CustomEmojiImpl(String name, long id, boolean animated)
    {
        this.name = name;
        this.id = id;
        this.animated = animated;
    }

    @Nonnull
    public String getAsReactionCode()
    {
        return name + ":" + id;
    }

    @Nonnull
    public String getName()
    {
        return name;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public boolean isAnimated()
    {
        return animated;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("name", name)
                .put("id", id)
                .put("animated", animated);
    }

    @Nonnull
    @Override
    public String getAsMention()
    {
        return Helpers.format("<%s:%s:%s>", animated ? "a" : "", name, getId());
    }

    @Nonnull
    @Override
    public String getFormatted()
    {
        return CustomEmoji.super.getFormatted();
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
        if (!(obj instanceof CustomEmoji))
            return false;
        CustomEmoji other = (CustomEmoji) obj;
        return this.id == other.getIdLong();
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(name)
                .toString();
    }

    @Nonnull
    @Override
    public UnicodeEmoji asUnicode()
    {
        throw new IllegalStateException("Cannot convert CustomEmoji into UnicodeEmoji!");
    }

    @Nonnull
    @Override
    public CustomEmoji asCustom()
    {
        return this;
    }
}

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
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.EncodingUtil;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Objects;

public class EmojiImpl implements Emoji, CustomEmoji, UnicodeEmoji
{
    private final String name;
    private final long id;
    private final boolean animated;

    public EmojiImpl(String name, long id, boolean animated)
    {
        this.name = name;
        this.id = id;
        this.animated = animated;
    }

    @Nonnull
    public String getAsCodepoints()
    {
        if (getType() != Type.UNICODE)
            throw new IllegalStateException("Cannot get codepoint for custom emojis");
        return EncodingUtil.encodeCodepoints(name);
    }

    @Nonnull
    public String getAsReactionCode()
    {
        return getType() == Type.CUSTOM
                ? name + ":" + id
                : name;
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
    public Type getType()
    {
        return id != 0 ? Type.CUSTOM : Type.UNICODE;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject json = DataObject.empty().put("name", name);
        if (id != 0)
        {
            json.put("id", id)
                .put("animated", animated);
        }
        return json;
    }

    @Nonnull
    @Override
    public String getAsMention()
    {
        return getType() == Type.UNICODE ? name : Helpers.format("<%s:%s:%s>", animated ? "a" : "", name, Long.toUnsignedString(id));
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
        return Objects.hash(name, id, animated);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof EmojiImpl))
            return false;
        EmojiImpl other = (EmojiImpl) obj;
        return other.id == id && Objects.equals(other.name, name);
    }

    @Override
    public String toString()
    {
        return getType() == Type.CUSTOM
                ? "Emoji:" + name + "(" + id + ")"
                : "Emoji(" + getAsCodepoints() + ')';
    }
}

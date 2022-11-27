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
import net.dv8tion.jda.internal.utils.EncodingUtil;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Objects;

public class UnicodeEmojiImpl implements UnicodeEmoji, EmojiUnion
{
    private final String name;

    public UnicodeEmojiImpl(String name)
    {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nonnull
    @Override
    public String getAsReactionCode()
    {
        return name;
    }

    @Nonnull
    @Override
    public String getAsCodepoints()
    {
        return EncodingUtil.encodeCodepoints(name);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty().put("name", name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof UnicodeEmoji))
            return false;
        return name.equals(((UnicodeEmoji) obj).getName());
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("codepoints", getAsCodepoints())
                .toString();
    }

    @Nonnull
    @Override
    public UnicodeEmoji asUnicode()
    {
        return this;
    }

    @Nonnull
    @Override
    public CustomEmoji asCustom()
    {
        throw new IllegalStateException("Cannot convert UnicodeEmoji into CustomEmoji!");
    }
}

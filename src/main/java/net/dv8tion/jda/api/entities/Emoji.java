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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;

public class Emoji implements SerializableData, IMentionable
{
    private final String name;
    private final long id;
    private final boolean animated;

    private Emoji(String name, long id, boolean animated)
    {
        this.name = name;
        this.id = id;
        this.animated = animated;
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

    public boolean isAnimated()
    {
        return animated;
    }

    public boolean isUnicode()
    {
        return id == 0L;
    }

    public boolean isCustom()
    {
        return !isUnicode();
    }

    @Nonnull
    public static Emoji ofUnicode(@Nonnull String name)
    {
        return new Emoji(name, 0, false);
    }

    @Nonnull
    public static Emoji ofEmote(@Nonnull String name, long id, boolean animated)
    {
        return new Emoji(name, id, animated);
    }

    @Nonnull
    public static Emoji ofEmote(@Nonnull Emote emote)
    {
        return ofEmote(emote.getName(), emote.getIdLong(), emote.isAnimated());
    }

    // either <a?:name:id> or just unicode
    @Nonnull
    public static Emoji parse(@Nonnull String code)
    {
        Matcher matcher = Message.MentionType.EMOTE.getPattern().matcher(code);
        if (matcher.matches())
            return ofEmote(matcher.group(1), Long.parseUnsignedLong(matcher.group(2)), code.startsWith("<a"));
        else
            return ofUnicode(code);
    }

    @Nonnull
    public static Emoji load(@Nonnull DataObject emoji)
    {
        return new Emoji(emoji.getString("name"),
                emoji.getUnsignedLong("id", 0),
                emoji.getBoolean("animated"));
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
        return id == 0L ? name : String.format("<%s:%s:%s>", animated ? "a" : "", name, Long.toUnsignedString(id));
    }
}

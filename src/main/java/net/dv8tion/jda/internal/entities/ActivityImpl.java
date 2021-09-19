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
package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.RichPresence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ActivityImpl implements Activity
{
    protected final String name;
    protected final String url;
    protected final ActivityType type;
    protected final Timestamps timestamps;
    protected final Emoji emoji;

    protected ActivityImpl(String name)
    {
        this(name, null, ActivityType.PLAYING);
    }

    protected ActivityImpl(String name, String url)
    {
        this(name, url, ActivityType.STREAMING);
    }

    protected ActivityImpl(String name, String url, ActivityType type)
    {
        this(name, url, type, null, null);
    }

    protected ActivityImpl(String name, String url, ActivityType type, RichPresence.Timestamps timestamps, Emoji emoji)
    {
        this.name = name;
        this.url = url;
        this.type = type;
        this.timestamps = timestamps;
        this.emoji = emoji;
    }

    @Override
    public boolean isRich()
    {
        return false;
    }

    @Override
    public RichPresence asRichPresence()
    {
        return null;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getUrl()
    {
        return url;
    }

    @Nonnull
    @Override
    public ActivityType getType()
    {
        return type;
    }

    @Nullable
    public RichPresence.Timestamps getTimestamps()
    {
        return timestamps;
    }

    @Nullable
    @Override
    public Emoji getEmoji()
    {
        return emoji;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof ActivityImpl))
            return false;

        ActivityImpl oGame = (ActivityImpl) o;
        return oGame.getType() == type
               && Objects.equals(name, oGame.getName())
               && Objects.equals(url, oGame.getUrl())
               && Objects.equals(timestamps, oGame.timestamps);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, type, url, timestamps);
    }

    @Override
    public String toString()
    {
        if (url != null)
            return String.format("Activity(%s | %s)", name, url);
        else
            return String.format("Activity(%s)", name);
    }
}

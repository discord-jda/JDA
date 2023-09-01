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
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ActivityImpl implements Activity
{
    protected final String name;
    protected final String url;
    protected final String state;
    protected final ActivityType type;
    protected final Timestamps timestamps;
    protected final EmojiUnion emoji;

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
        this(name, null, url, type, null, null);
    }

    protected ActivityImpl(String name, String state, String url, ActivityType type)
    {
        this(name, state, url, type, null, null);
    }

    protected ActivityImpl(String name, String state, String url, ActivityType type, Activity.Timestamps timestamps, EmojiUnion emoji)
    {
        this.name = name;
        this.state = state;
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

    @Nullable
    @Override
    public String getState()
    {
        return state;
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
    public Activity.Timestamps getTimestamps()
    {
        return timestamps;
    }

    @Nullable
    @Override
    public EmojiUnion getEmoji()
    {
        return emoji;
    }

    @Nonnull
    @Override
    public Activity withState(@Nullable String state)
    {
        if (state != null)
        {
            state = state.trim();
            if (state.isEmpty())
                state = null;
            else
                Checks.notLonger(state, MAX_ACTIVITY_STATE_LENGTH, "State");
        }

        return new ActivityImpl(name, state, url, type);
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
               && Objects.equals(state, oGame.state)
               && Objects.equals(url, oGame.getUrl())
               && Objects.equals(timestamps, oGame.timestamps);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, state, type, url, timestamps);
    }

    @Override
    public String toString()
    {
        final EntityString entityString = new EntityString(this)
                .setType(type)
                .setName(name);
        if (url != null) 
            entityString.addMetadata("url", url);

        return entityString.toString();
    }
}

/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.core.entities.Activity;
import net.dv8tion.jda.core.entities.RichPresence;

import javax.annotation.Nullable;
import java.util.Objects;

public class ActivityImpl implements Activity
{
    protected final String name;
    protected final String url;
    protected final ActivityType type;
    protected final Timestamps timestamps;

    protected ActivityImpl(String name)
    {
        this(name, null, ActivityType.DEFAULT);
    }

    protected ActivityImpl(String name, String url)
    {
        this(name, url, ActivityType.STREAMING);
    }

    protected ActivityImpl(String name, String url, ActivityType type)
    {
        this(name, url, type, null);
    }

    protected ActivityImpl(String name, String url, ActivityType type, RichPresence.Timestamps timestamps)
    {
        this.name = name;
        this.url = url;
        this.type = type;
        this.timestamps = timestamps;
    }

    /**
     * Whether this is a <a href="https://discordapp.com/developers/docs/rich-presence/best-practices" target="_blank">Rich Presence</a>
     * <br>If {@code false} the result of {@link #asRichPresence()} is {@code null}
     *
     * @return {@code true} if this is a {@link net.dv8tion.jda.core.entities.RichPresence RichPresence}
     */
    @Override
    public boolean isRich()
    {
        return false;
    }

    /**
     * {@link net.dv8tion.jda.core.entities.RichPresence RichPresence} representation of
     * this Activity.
     *
     * @return RichPresence or {@code null} if {@link #isRich()} returns {@code false}
     */
    @Override
    public RichPresence asRichPresence()
    {
        return null;
    }

    /**
     * The displayed name of the {@link Activity Activity}. If no name has been set, this returns null.
     *
     * @return Possibly-null String containing the Activity's name.
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * The URL of the {@link Activity Activity} if the game is actually a Stream.
     * <br>This will return null for regular games.
     *
     * @return Possibly-null String containing the Activity's URL.
     */
    @Override
    public String getUrl()
    {
        return url;
    }

    /**
     * The type of {@link Activity Activity}.
     *
     * @return Never-null {@link net.dv8tion.jda.core.entities.Activity.ActivityType ActivityType} representing the type of Activity
     */
    @Override
    public ActivityType getType()
    {
        return type;
    }

    /**
     * Information on the match duration, start, and end.
     *
     * @return {@link net.dv8tion.jda.core.entities.RichPresence.Timestamps Timestamps} wrapper of {@code null} if unset
     */
    @Nullable
    public RichPresence.Timestamps getTimestamps()
    {
        return timestamps;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof ActivityImpl))
            return false;
        if (o == this)
            return true;

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

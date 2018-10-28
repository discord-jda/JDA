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
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.core.utils.Checks;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents a Discord {@link Activity Activity}.
 * <br>This should contain all information provided from Discord about a Activity.
 *
 * @since  2.1
 * @author John A. Grosh
 */
public class Activity
{
    protected final String name;
    protected final String url;
    protected final ActivityType type;
    protected final RichPresence.Timestamps timestamps;

    protected Activity(String name)
    {
        this(name, null, ActivityType.DEFAULT);
    }

    protected Activity(String name, String url)
    {
        this(name, url, ActivityType.STREAMING);
    }

    protected Activity(String name, String url, ActivityType type)
    {
        this(name, url, type, null);
    }

    protected Activity(String name, String url, ActivityType type, RichPresence.Timestamps timestamps)
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
    public RichPresence asRichPresence()
    {
        return null;
    }

    /**
     * The displayed name of the {@link Activity Activity}. If no name has been set, this returns null.
     *
     * @return Possibly-null String containing the Activity's name.
     */
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
    public String getUrl()
    {
        return url;
    }

    /**
     * The type of {@link Activity Activity}.
     *
     * @return Never-null {@link net.dv8tion.jda.core.entities.Activity.ActivityType ActivityType} representing the type of Activity
     */
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
        if (!(o instanceof Activity))
            return false;
        if (o == this)
            return true;

        Activity oGame = (Activity) o;
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

    /**
     * Creates a new Activity instance with the specified name.
     * <br>In order to appear as "streaming" in the official client you must
     * provide a valid (see documentation of method) streaming URL in {@link #streaming(String, String) Activity.streaming(String, String)}.
     *
     * @param  name
     *         The not-null name of the newly created game
     *
     * @throws IllegalArgumentException
     *         if the specified name is null, empty or blank
     *
     * @return A valid Activity instance with the provided name with {@link net.dv8tion.jda.core.entities.Activity.ActivityType#DEFAULT}
     */
    public static Activity playing(String name)
    {
        Checks.notBlank(name, "Name");
        return new Activity(name, null, ActivityType.DEFAULT);
    }

    /**
     * Creates a new Activity instance with the specified name and url.
     * <br>The specified URL must be valid according to discord standards in order to display as "streaming" in the official client.
     * A valid streaming URL must be derived from {@code https://twitch.tv/} and can be verified using {@link #isValidStreamingUrl(String)}. (see documentation)
     *
     * @param  name
     *         The not-null name of the newly created game
     * @param  url
     *         The streaming url to use, required to display as "streaming"
     *
     * @throws IllegalArgumentException
     *         If the specified name is null or empty
     *
     * @return A valid Activity instance with the provided name and url
     *
     * @see    #isValidStreamingUrl(String)
     */
    public static Activity streaming(String name, String url)
    {
        Checks.notEmpty(name, "Provided game name");
        ActivityType type;
        if (isValidStreamingUrl(url))
            type = ActivityType.STREAMING;
        else
            type = ActivityType.DEFAULT;
        return new Activity(name, url, type);
    }

    /**
     * Creates a new Activity instance with the specified name.
     * <br>This will display as {@code Listening name} in the official client
     *
     * @param  name
     *         The not-null name of the newly created game
     *
     * @throws IllegalArgumentException
     *         if the specified name is null, empty or blank
     *
     * @return A valid Activity instance with the provided name with {@link net.dv8tion.jda.core.entities.Activity.ActivityType#LISTENING}
     */
    public static Activity listening(String name)
    {
        Checks.notBlank(name, "Name");
        return new Activity(name, null, ActivityType.LISTENING);
    }

    /**
     * Creates a new Activity instance with the specified name.
     * <br>This will display as {@code Watching name} in the official client
     *
     * @param  name
     *         The not-null name of the newly created game
     *
     * @throws IllegalArgumentException
     *         if the specified name is null, empty or blank
     *
     * @return A valid Activity instance with the provided name with {@link net.dv8tion.jda.core.entities.Activity.ActivityType#WATCHING}
     *
     * @incubating This feature is not yet confirmed for the official bot API
     */
    @Incubating
    public static Activity watching(String name)
    {
        Checks.notBlank(name, "Name");
        return new Activity(name, null, ActivityType.WATCHING);
    }

    /**
     * Creates a new Activity instance with the specified name and url.
     *
     * @param  type
     *         The {@link net.dv8tion.jda.core.entities.Activity.ActivityType ActivityType} to use
     * @param  name
     *         The not-null name of the newly created game
     *
     * @throws IllegalArgumentException
     *         If the specified name is null or empty
     *
     * @return A valid Activity instance with the provided name and url
     */
    public static Activity of(ActivityType type, String name)
    {
        return of(type, name, null);
    }

    /**
     * Creates a new Activity instance with the specified name and url.
     * <br>The provided url would only be used for {@link net.dv8tion.jda.core.entities.Activity.ActivityType#STREAMING ActivityType.STREAMING}
     * and should be a twitch url.
     *
     * @param  type
     *         The {@link net.dv8tion.jda.core.entities.Activity.ActivityType ActivityType} to use
     * @param  name
     *         The not-null name of the newly created game
     * @param  url
     *         The streaming url to use, required to display as "streaming".
     *
     * @throws IllegalArgumentException
     *         If the specified name is null or empty
     *
     * @return A valid Activity instance with the provided name and url
     *
     * @see    #isValidStreamingUrl(String)
     */
    public static Activity of(ActivityType type, String name, String url)
    {
        Checks.notNull(type, "Type");
        switch (type)
        {
            case DEFAULT:
                return playing(name);
            case STREAMING:
                return streaming(name, url);
            case LISTENING:
                return listening(name);
            case WATCHING:
                return watching(name);
            default:
                throw new IllegalArgumentException("ActivityType " + type + " is not supported!");
        }
    }

    /**
     * Checks if a given String is a valid Twitch url (ie, one that will display "Streaming" on the Discord client).
     *
     * @param  url
     *         The url to check.
     *
     * @return True if the provided url is valid for triggering Discord's streaming status
     */
    public static boolean isValidStreamingUrl(String url)
    {
        return url != null && url.matches("https?://(www\\.)?twitch\\.tv/.+");
    }

    /**
     * The type game being played, differentiating between a game and stream types.
     */
    public enum ActivityType
    {
        /**
         * The ActivityType used to represent a normal {@link Activity Activity} status.
         */
        DEFAULT(0),
        /**
         * Used to indicate that the {@link Activity Activity} is a stream
         * <br>This type is displayed as "Streaming" in the discord client.
         */
        STREAMING(1),
        /**
         * Used to indicate that the {@link Activity Activity} should display
         * as {@code Listening...} in the official client.
         */
        LISTENING(2),
        /**
         * Used to indicate that the {@link Activity Activity} should display
         * as {@code Watching...} in the official client.
         *
         * @incubating This feature is not yet confirmed for the official bot API
         */
        @Incubating
        WATCHING(3);

        private final int key;

        ActivityType(int key)
        {
            this.key = key;
        }

        /**
         * The Discord defined id key for this ActivityType.
         *
         * @return the id key.
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Gets the ActivityType related to the provided key.
         * <br>If an unknown key is provided, this returns {@link #DEFAULT}
         *
         * @param  key
         *         The Discord key referencing a ActivityType.
         *
         * @return The ActivityType that has the key provided, or {@link #DEFAULT} for unknown key.
         */
        public static ActivityType fromKey(int key)
        {
            switch (key)
            {
                case 0:
                default:
                    return DEFAULT;
                case 1:
                    return STREAMING;
                case 2:
                    return LISTENING;
                case 3:
                    return WATCHING;
            }
        }
    }
}

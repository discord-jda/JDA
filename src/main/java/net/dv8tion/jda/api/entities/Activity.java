/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

/**
 * Represents a Discord {@link Activity Activity}.
 * <br>This should contain all information provided from Discord about a Activity.
 *
 * @since  2.1
 * @author John A. Grosh
 */
public interface Activity
{
    /**
     * Whether this is a <a href="https://discordapp.com/developers/docs/rich-presence/best-practices" target="_blank">Rich Presence</a>
     * <br>If {@code false} the result of {@link #asRichPresence()} is {@code null}
     *
     * @return {@code true} if this is a {@link net.dv8tion.jda.api.entities.RichPresence RichPresence}
     */
    boolean isRich();

    /**
     * {@link net.dv8tion.jda.api.entities.RichPresence RichPresence} representation of
     * this Activity.
     *
     * @return RichPresence or {@code null} if {@link #isRich()} returns {@code false}
     */
    @Nullable
    RichPresence asRichPresence();

    /**
     * The displayed name of the {@link Activity Activity}. If no name has been set, this returns null.
     *
     * @return String containing the Activity's name.
     */
    @Nonnull
    String getName();

    /**
     * The URL of the {@link Activity Activity} if the game is actually a Stream.
     * <br>This will return null for regular games.
     *
     * @return Possibly-null String containing the Activity's URL.
     */
    @Nullable
    String getUrl();

    /**
     * The type of {@link Activity Activity}.
     *
     * @return Never-null {@link net.dv8tion.jda.api.entities.Activity.ActivityType ActivityType} representing the type of Activity
     */
    @Nonnull
    ActivityType getType();

    /**
     * Information on the match duration, start, and end.
     *
     * @return {@link net.dv8tion.jda.api.entities.Activity.Timestamps Timestamps} wrapper of {@code null} if unset
     */
    @Nullable
    Timestamps getTimestamps();

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
     * @return A valid Activity instance with the provided name with {@link net.dv8tion.jda.api.entities.Activity.ActivityType#DEFAULT}
     */
    @Nonnull
    static Activity playing(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        return EntityBuilder.createActivity(name, null, ActivityType.DEFAULT);
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
    @Nonnull
    static Activity streaming(@Nonnull String name, @Nullable String url)
    {
        Checks.notEmpty(name, "Provided game name");
        ActivityType type;
        if (isValidStreamingUrl(url))
            type = ActivityType.STREAMING;
        else
            type = ActivityType.DEFAULT;
        return EntityBuilder.createActivity(name, url, type);
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
     * @return A valid Activity instance with the provided name with {@link net.dv8tion.jda.api.entities.Activity.ActivityType#LISTENING}
     */
    @Nonnull
    static Activity listening(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        return EntityBuilder.createActivity(name, null, ActivityType.LISTENING);
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
     * @return A valid Activity instance with the provided name with {@link net.dv8tion.jda.api.entities.Activity.ActivityType#WATCHING}
     *
     * @incubating This feature is not yet confirmed for the official bot API
     */
    @Nonnull
    @Incubating
    static Activity watching(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        return EntityBuilder.createActivity(name, null, ActivityType.WATCHING);
    }

    /**
     * Creates a new Activity instance with the specified name and url.
     *
     * @param  type
     *         The {@link net.dv8tion.jda.api.entities.Activity.ActivityType ActivityType} to use
     * @param  name
     *         The not-null name of the newly created game
     *
     * @throws IllegalArgumentException
     *         If the specified name is null or empty
     *
     * @return A valid Activity instance with the provided name and url
     */
    @Nonnull
    static Activity of(@Nonnull ActivityType type, @Nonnull String name)
    {
        return of(type, name, null);
    }

    /**
     * Creates a new Activity instance with the specified name and url.
     * <br>The provided url would only be used for {@link net.dv8tion.jda.api.entities.Activity.ActivityType#STREAMING ActivityType.STREAMING}
     * and should be a twitch url.
     *
     * @param  type
     *         The {@link net.dv8tion.jda.api.entities.Activity.ActivityType ActivityType} to use
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
    @Nonnull
    static Activity of(@Nonnull ActivityType type, @Nonnull String name, @Nullable String url)
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
    static boolean isValidStreamingUrl(@Nullable String url)
    {
        return url != null && url.matches("https?://(www\\.)?twitch\\.tv/.+");
    }

    /**
     * The type game being played, differentiating between a game and stream types.
     */
    enum ActivityType
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
        @Nonnull
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

    /**
     * Represents the start and end timestamps for a running match
     */
    class Timestamps
    {
        protected final long start;

        protected final long end;

        public Timestamps(long start, long end)
        {
            this.start = start;
            this.end = end;
        }

        /**
         * Epoch second timestamp of match start, or {@code 0} of unset.
         *
         * @return Epoch second timestamp of match start, or {@code 0} of unset.
         */
        public long getStart()
        {
            return start;
        }

        /**
         * Shortcut for {@code Instant.ofEpochSecond(start)}
         *
         * @return Instant of match start, or {@code null} if unset
         */
        @Nullable
        public Instant getStartTime()
        {
            return start <= 0 ? null : Instant.ofEpochMilli(start);
        }

        /**
         * Epoch second timestamp of match end, or {@code 0} of unset.
         *
         * @return Epoch second timestamp of match end, or {@code 0} of unset.
         */
        public long getEnd()
        {
            return end;
        }

        /**
         * Shortcut for {@code Instant.ofEpochSecond(start)}
         *
         * @return Instant of match start, or {@code null} if unset
         */
        @Nullable
        public Instant getEndTime()
        {
            return end <= 0 ? null : Instant.ofEpochMilli(end);
        }

        /**
         * Calculates the amount of time until {@link #getEndTime()} in terms of the specified unit.
         * <br>If {@link #getEndTime()} is {@code null} this will be negative.
         *
         * @param  unit
         *         The {@link java.time.temporal.TemporalUnit TemporalUnit} to return
         *
         * @throws IllegalArgumentException
         *         If the provided unit is {@code null}
         * @throws ArithmeticException
         *         If a numeric overflow occurs
         * @throws java.time.DateTimeException
         *         If the amount cannot be calculated
         * @throws java.time.temporal.UnsupportedTemporalTypeException
         *         If the provided unit is not supported
         *
         * @return Remaining time in the provided {@link java.time.temporal.TemporalUnit TemporalUnit} or {@code -1} if unset
         *
         * @see    java.time.Instant#until(java.time.temporal.Temporal, java.time.temporal.TemporalUnit) Instant.until(Temporal, TemporalUnit)
         * @see    java.time.temporal.TemporalUnit
         */
        public long getRemainingTime(TemporalUnit unit)
        {
            Checks.notNull(unit, "TemporalUnit");
            Instant end = getEndTime();
            return end != null ? Instant.now().until(end, unit) : -1;
        }

        /**
         * Calculates the elapsed time from {@link #getStartTime()} to now in terms of the specified unit.
         * <br>If {@link #getStartTime()} is {@code null} this will be negative.
         *
         * @param  unit
         *         The {@link java.time.temporal.TemporalUnit TemporalUnit} to return
         *
         * @throws IllegalArgumentException
         *         If the provided unit is {@code null}
         * @throws ArithmeticException
         *         If a numeric overflow occurs
         * @throws java.time.DateTimeException
         *         If the amount cannot be calculated
         * @throws java.time.temporal.UnsupportedTemporalTypeException
         *         If the provided unit is not supported
         *
         * @return Elapsed time in the provided {@link java.time.temporal.TemporalUnit TemporalUnit} or {@code -1} if unset
         *
         * @see    java.time.Instant#until(java.time.temporal.Temporal, java.time.temporal.TemporalUnit) Instant.until(Temporal, TemporalUnit)
         * @see    java.time.temporal.TemporalUnit
         */
        public long getElapsedTime(TemporalUnit unit)
        {
            Checks.notNull(unit, "TemporalUnit");
            Instant start = getStartTime();
            return start != null ? start.until(Instant.now(), unit) : -1;
        }

        @Override
        public String toString()
        {
            return String.format("RichPresenceTimestamp(%d-%d)", start, end);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Timestamps))
                return false;
            Timestamps t = (Timestamps) obj;
            return start == t.start && end == t.end;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(start, end);
        }
    }
}

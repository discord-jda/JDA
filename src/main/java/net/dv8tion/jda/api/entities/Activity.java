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

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a Discord {@link Activity Activity}.
 * <br>This should contain all information provided from Discord about a Activity.
 *
 * @since  2.1
 * @author John A. Grosh
 *
 * @see    #of(ActivityType, String)
 * @see    #of(ActivityType, String, String)
 * @see    #playing(String)
 * @see    #watching(String)
 * @see    #listening(String)
 * @see    #streaming(String, String)
 * @see    #competing(String)
 */
public interface Activity
{
    /** The Pattern used for {@link #isValidStreamingUrl(String)} */
    Pattern STREAMING_URL = Pattern.compile("https?://(www\\.)?(twitch\\.tv/|youtube\\.com/watch\\?v=).+", Pattern.CASE_INSENSITIVE);

    /**
     * Whether this is a <a href="https://discord.com/developers/docs/rich-presence/best-practices" target="_blank">Rich Presence</a>
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
     * The displayed name of the {@link Activity Activity}.
     * <br>For {@link ActivityType#CUSTOM_STATUS} this will return the custom status text.
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
     * The emoji (or custom emoji) attached to a custom status.
     *
     * @return Possibly-null {@link Emoji} used for custom status
     */
    @Nullable
    Emoji getEmoji();

    /**
     * Creates a new Activity instance with the specified name.
     * <br>In order to appear as "streaming" in the official client you must
     * provide a valid (see documentation of method) streaming URL in {@link #streaming(String, String) Activity.streaming(String, String)}.
     *
     * @param  name
     *         The not-null name of the newly created game
     *
     * @throws IllegalArgumentException
     *         if the specified name is null, empty, blank or longer than 128 characters
     *
     * @return A valid Activity instance with the provided name with {@link net.dv8tion.jda.api.entities.Activity.ActivityType#PLAYING}
     */
    @Nonnull
    static Activity playing(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notLonger(name, 128, "Name");
        return EntityBuilder.createActivity(name, null, ActivityType.PLAYING);
    }

    /**
     * Creates a new Activity instance with the specified name and url.
     * <br>The specified URL must be valid according to discord standards in order to display as "streaming" in the official client.
     * A valid streaming URL must be derived from {@code https://twitch.tv/} or {@code https://youtube.com/watch?v=} and can be verified using {@link #isValidStreamingUrl(String)}. (see documentation)
     *
     * @param  name
     *         The not-null name of the newly created game
     * @param  url
     *         The streaming url to use, required to display as "streaming"
     *
     * @throws IllegalArgumentException
     *         If the specified name is null, empty or longer than 128 characters
     *
     * @return A valid Activity instance with the provided name and url
     *
     * @see    #isValidStreamingUrl(String)
     */
    @Nonnull
    static Activity streaming(@Nonnull String name, @Nullable String url)
    {
        Checks.notEmpty(name, "Provided game name");
        name = Helpers.isBlank(name) ? name : name.trim();
        Checks.notLonger(name, 128, "Name");
        ActivityType type;
        if (isValidStreamingUrl(url))
            type = ActivityType.STREAMING;
        else
            type = ActivityType.PLAYING;
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
     *         if the specified name is null, empty, blank or longer than 128 characters
     *
     * @return A valid Activity instance with the provided name with {@link net.dv8tion.jda.api.entities.Activity.ActivityType#LISTENING}
     */
    @Nonnull
    static Activity listening(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notLonger(name, 128, "Name");
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
     *         if the specified name is null, empty, blank or longer than 128 characters
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
        name = name.trim();
        Checks.notLonger(name, 128, "Name");
        return EntityBuilder.createActivity(name, null, ActivityType.WATCHING);
    }

    /**
     * Creates a new Activity instance with the specified name.
     * <br>This will display as {@code Competing in name} in the official client
     * 
     * @param  name
     *         The not-null name of the newly created game
     * 
     * @throws IllegalArgumentException
     *         If the specified name is null, empty, blank or longer than 128 characters
     * 
     * @return A valid Activity instance with the provided name with {@link net.dv8tion.jda.api.entities.Activity.ActivityType#COMPETING}
     *
     * @since  4.2.1
     */
    @Nonnull
    static Activity competing(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notLonger(name, 128, "Name");
        return EntityBuilder.createActivity(name, null, ActivityType.COMPETING);
    }

    /**
     * Creates a new Activity instance with the specified name.
     *
     * @param  type
     *         The {@link net.dv8tion.jda.api.entities.Activity.ActivityType ActivityType} to use
     * @param  name
     *         The not-null name of the newly created game
     *
     * @throws IllegalArgumentException
     *         <ul>
     *           <li>If the specified ActivityType is null or unsupported</li>
     *           <li>If the specified name is null, empty or longer than 128 characters</li>
     *         </ul>
     *
     * @return A valid Activity instance with the provided name
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
     *         <ul>
     *           <li>If the specified ActivityType is null or unsupported</li>
     *           <li>If the specified name is null, empty or longer than 128 characters</li>
     *         </ul>
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
            case PLAYING:
                return playing(name);
            case STREAMING:
                return streaming(name, url);
            case LISTENING:
                return listening(name);
            case WATCHING:
                return watching(name);
            case COMPETING:
                return competing(name);
            default:
                throw new IllegalArgumentException("ActivityType " + type + " is not supported!");
        }
    }

    /**
     * Checks if a given String is a valid Twitch/Youtube streaming url (ie, one that will display "Streaming" on the Discord client).
     *
     * @param  url
     *         The url to check.
     *
     * @return True if the provided url is valid for triggering Discord's streaming status
     */
    static boolean isValidStreamingUrl(@Nullable String url)
    {
        return url != null && STREAMING_URL.matcher(url).matches();
    }

    /**
     * The activity being executed, differentiating between, amongst others, playing, listening and streaming.
     */
    enum ActivityType
    {
        /**
         * Used to indicate that the {@link Activity Activity} should display
         * as {@code Playing...} in the official client.
         */
        PLAYING(0),
        /**
         * Used to indicate that the {@link Activity Activity} is a stream and should be displayed
         * as {@code Streaming...} in the official client.
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
        WATCHING(3),
        /**
         * Used to indicate that the {@link Activity Activity} should display as a custom status
         * in the official client.
         *
         * @incubating This Activity type is <b>read-only</b> for bots
         */
        @Incubating
        CUSTOM_STATUS(4),

        /**
         * Used to indicate that the {@link Activity Activity} should display
         * as {@code Competing in...} in the official client.
         *
         * @since  4.2.1
         */
        COMPETING(5);

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
         * <br>If an unknown key is provided, this returns {@link #PLAYING}
         *
         * @param  key
         *         The Discord key referencing a ActivityType.
         *
         * @return The ActivityType that has the key provided, or {@link #PLAYING} for unknown key.
         */
        @Nonnull
        public static ActivityType fromKey(int key)
        {
            switch (key)
            {
                case 0:
                default:
                    return PLAYING;
                case 1:
                    return STREAMING;
                case 2:
                    return LISTENING;
                case 3:
                    return WATCHING;
                case 4:
                    return CUSTOM_STATUS;
                case 5:
                    return COMPETING;
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
            return Helpers.format("RichPresenceTimestamp(%d-%d)", start, end);
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

    /**
     * Emoji for a custom status.
     * <br>This can be a unicode emoji or a custom emoji (Emote).
     */
    class Emoji implements ISnowflake, IMentionable
    {
        private final String name;
        private final long id;
        private final boolean animated;

        public Emoji(String name, long id, boolean animated)
        {
            this.name = name;
            this.id = id;
            this.animated = animated;
        }

        public Emoji(String name)
        {
            this(name, 0, false);
        }

        /**
         * The name of this emoji. This will be the unicode characters for a unicode emoji
         * and the name of the custom emote otherwise.
         *
         * @return The emoji name
         *
         * @see    #getAsCodepoints()
         */
        @Nonnull
        public String getName()
        {
            return name;
        }

        /**
         * The codepoint notation ({@code "U+XXXX"}) for the unicode of this emoji.
         * Not available for custom emotes.
         *
         * @throws IllegalStateException
         *         If {@link #isEmoji()} is false
         *
         * @return The codepoint notation
         *
         * @see    #getName()
         */
        @Nonnull
        public String getAsCodepoints()
        {
            if (!isEmoji())
                throw new IllegalStateException("Cannot convert custom emote to codepoints");
            return EncodingUtil.encodeCodepoints(name);
        }

        /**
         * The id for this custom emoji.
         *
         * @throws IllegalStateException
         *         If {@link #isEmote()} is false
         *
         * @return The emoji id
         */
        @Override
        public long getIdLong()
        {
            if (!isEmote())
                throw new IllegalStateException("Cannot get id for unicode emoji");
            return id;
        }

        /**
         * Whether this emoji is animated.
         * This is always false for unicode emoji.
         *
         * @return True, if this emoji is animated
         */
        public boolean isAnimated()
        {
            return animated;
        }

        /**
         * Whether this is a unicode emoji.
         *
         * @return True, if this is a unicode emoji
         */
        public boolean isEmoji()
        {
            return id == 0;
        }

        /**
         * Whether this is a custom emoji (Emote)
         *
         * @return True, if this is a custom emoji
         */
        public boolean isEmote()
        {
            return id != 0;
        }

        @Nonnull
        @Override
        public String getAsMention()
        {
            if (isEmoji())
                return name; // unicode name
            // custom emoji format (for messages)
            return String.format("<%s:%s:%s>", isAnimated() ? "a" : "", name, getId());
        }

        @Override
        public int hashCode()
        {
            return id == 0 ? name.hashCode() : Long.hashCode(id);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof Emoji))
                return false;
            Emoji other = (Emoji) obj;
            return id == 0 ? other.name.equals(this.name)
                           : other.id == this.id;
        }

        @Override
        public String toString()
        {
            if (isEmoji())
                return "ActivityEmoji(" + getAsCodepoints() + ')';
            return "ActivityEmoji(" + Long.toUnsignedString(id) + " / " + name + ')';
        }
    }
}

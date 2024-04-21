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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.internal.entities.EntityBuilder
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import net.dv8tion.jda.internal.utils.Helpers
import org.jetbrains.annotations.Contract
import java.time.Instant
import java.time.temporal.TemporalUnit
import java.util.*
import java.util.regex.Pattern
import javax.annotation.Nonnull

/**
 * Represents a Discord [Activity].
 * <br></br>This should contain all information provided from Discord about a Activity.
 *
 * @since  2.1
 * @author John A. Grosh
 *
 * @see .of
 * @see .of
 * @see .playing
 * @see .watching
 * @see .listening
 * @see .streaming
 * @see .competing
 */
interface Activity {
    /**
     * Whether this is a [Rich Presence](https://discord.com/developers/docs/rich-presence/best-practices)
     * <br></br>If `false` the result of [.asRichPresence] is `null`
     *
     * @return `true` if this is a [RichPresence][net.dv8tion.jda.api.entities.RichPresence]
     */
    @JvmField
    val isRich: Boolean

    /**
     * [RichPresence][net.dv8tion.jda.api.entities.RichPresence] representation of
     * this Activity.
     *
     * @return RichPresence or `null` if [.isRich] returns `false`
     */
    fun asRichPresence(): RichPresence?

    @JvmField
    @get:Nonnull
    val name: String?

    /**
     * The user's activity state
     * <br></br>Example: "Looking to Play", "Playing Solo", "In a Group"
     *
     *
     * This shows below the normal activity information in the profile.
     *
     *
     * **Example**<br></br>
     * Code:
     * <pre>`Activity.playing("Trivia")
     * .withState("Question 20")
    `</pre> *
     * Display:
     * <pre>
     * Playing Trivia
     * Question 20
    </pre> *
     *
     * @return The user's current party status
     */
    @JvmField
    val state: String?

    /**
     * The URL of the [Activity] if the game is actually a Stream.
     * <br></br>This will return null for regular games.
     *
     * @return Possibly-null String containing the Activity's URL.
     */
    @JvmField
    val url: String?

    @JvmField
    @get:Nonnull
    val type: ActivityType?

    /**
     * Information on the match duration, start, and end.
     *
     * @return [Timestamps][net.dv8tion.jda.api.entities.Activity.Timestamps] wrapper of `null` if unset
     */
    @JvmField
    val timestamps: Timestamps?

    /**
     * The emoji (or custom emoji) attached to a custom status.
     *
     * @return Possibly-null [Emoji] used for custom status
     */
    val emoji: EmojiUnion?

    /**
     * Adds the provided state to the activity.
     * <br></br>The state is shown below the activity, unless it is a [custom status][.customStatus].
     *
     * @param  state
     * The activity state, or null to unset
     *
     * @throws IllegalArgumentException
     * If the state is longer than {@value #MAX_ACTIVITY_STATE_LENGTH} characters
     *
     * @return New activity instance with the provided state
     */
    @Nonnull
    @Contract("_->new")
    fun withState(state: String?): Activity?

    /**
     * The activity being executed, differentiating between, amongst others, playing, listening and streaming.
     */
    enum class ActivityType(
        /**
         * The Discord defined id key for this ActivityType.
         *
         * @return the id key.
         */
        @JvmField val key: Int
    ) {
        /**
         * Used to indicate that the [Activity] should display
         * as `Playing...` in the official client.
         */
        PLAYING(0),

        /**
         * Used to indicate that the [Activity] is a stream and should be displayed
         * as `Streaming...` in the official client.
         */
        STREAMING(1),

        /**
         * Used to indicate that the [Activity] should display
         * as `Listening...` in the official client.
         */
        LISTENING(2),

        /**
         * Used to indicate that the [Activity] should display
         * as `Watching...` in the official client.
         */
        WATCHING(3),

        /**
         * Used to indicate that the [Activity] should display as a custom status
         * in the official client.
         */
        CUSTOM_STATUS(4),

        /**
         * Used to indicate that the [Activity] should display
         * as `Competing in...` in the official client.
         *
         * @since  4.2.1
         */
        COMPETING(5);

        companion object {
            /**
             * Gets the ActivityType related to the provided key.
             * <br></br>If an unknown key is provided, this returns [.PLAYING]
             *
             * @param  key
             * The Discord key referencing a ActivityType.
             *
             * @return The ActivityType that has the key provided, or [.PLAYING] for unknown key.
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): ActivityType {
                return when (key) {
                    0 -> PLAYING
                    1 -> STREAMING
                    2 -> LISTENING
                    3 -> WATCHING
                    4 -> CUSTOM_STATUS
                    5 -> COMPETING
                    else -> PLAYING
                }
            }
        }
    }

    /**
     * Represents the start and end timestamps for a running match
     */
    class Timestamps(
        /**
         * Epoch second timestamp of match start, or `0` of unset.
         *
         * @return Epoch second timestamp of match start, or `0` of unset.
         */
        @JvmField val start: Long,
        /**
         * Epoch second timestamp of match end, or `0` of unset.
         *
         * @return Epoch second timestamp of match end, or `0` of unset.
         */
        @JvmField val end: Long
    ) {

        val startTime: Instant?
            /**
             * Shortcut for `Instant.ofEpochSecond(start)`
             *
             * @return Instant of match start, or `null` if unset
             */
            get() = if (start <= 0) null else Instant.ofEpochMilli(start)
        val endTime: Instant?
            /**
             * Shortcut for `Instant.ofEpochSecond(start)`
             *
             * @return Instant of match start, or `null` if unset
             */
            get() = if (end <= 0) null else Instant.ofEpochMilli(end)

        /**
         * Calculates the amount of time until [.getEndTime] in terms of the specified unit.
         * <br></br>If [.getEndTime] is `null` this will be negative.
         *
         * @param  unit
         * The [TemporalUnit][java.time.temporal.TemporalUnit] to return
         *
         * @throws IllegalArgumentException
         * If the provided unit is `null`
         * @throws ArithmeticException
         * If a numeric overflow occurs
         * @throws java.time.DateTimeException
         * If the amount cannot be calculated
         * @throws java.time.temporal.UnsupportedTemporalTypeException
         * If the provided unit is not supported
         *
         * @return Remaining time in the provided [TemporalUnit][java.time.temporal.TemporalUnit] or `-1` if unset
         *
         * @see java.time.Instant.until
         * @see java.time.temporal.TemporalUnit
         */
        fun getRemainingTime(unit: TemporalUnit?): Long {
            Checks.notNull(unit, "TemporalUnit")
            val end = endTime
            return if (end != null) Instant.now().until(end, unit) else -1
        }

        /**
         * Calculates the elapsed time from [.getStartTime] to now in terms of the specified unit.
         * <br></br>If [.getStartTime] is `null` this will be negative.
         *
         * @param  unit
         * The [TemporalUnit][java.time.temporal.TemporalUnit] to return
         *
         * @throws IllegalArgumentException
         * If the provided unit is `null`
         * @throws ArithmeticException
         * If a numeric overflow occurs
         * @throws java.time.DateTimeException
         * If the amount cannot be calculated
         * @throws java.time.temporal.UnsupportedTemporalTypeException
         * If the provided unit is not supported
         *
         * @return Elapsed time in the provided [TemporalUnit][java.time.temporal.TemporalUnit] or `-1` if unset
         *
         * @see java.time.Instant.until
         * @see java.time.temporal.TemporalUnit
         */
        fun getElapsedTime(unit: TemporalUnit?): Long {
            Checks.notNull(unit, "TemporalUnit")
            val start = startTime
            return start?.until(Instant.now(), unit) ?: -1
        }

        override fun toString(): String {
            return EntityString("RichPresenceTimestamp")
                .addMetadata("start", start)
                .addMetadata("end", end)
                .toString()
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is Timestamps) return false
            val t = obj
            return start == t.start && end == t.end
        }

        override fun hashCode(): Int {
            return Objects.hash(start, end)
        }
    }

    companion object {
        /**
         * Creates a new Activity instance with the specified name.
         * <br></br>In order to appear as "streaming" in the official client you must
         * provide a valid (see documentation of method) streaming URL in [Activity.streaming(String, String)][.streaming].
         *
         * @param  name
         * The not-null name of the newly created game
         *
         * @throws IllegalArgumentException
         * if the specified name is null, empty, blank or longer than {@value #MAX_ACTIVITY_NAME_LENGTH} characters
         *
         * @return A valid Activity instance with the provided name with [net.dv8tion.jda.api.entities.Activity.ActivityType.PLAYING]
         */
        @JvmStatic
        @Nonnull
        fun playing(@Nonnull name: String): Activity? {
            var name = name
            Checks.notBlank(name, "Name")
            name = name.trim { it <= ' ' }
            Checks.notLonger(name, MAX_ACTIVITY_NAME_LENGTH, "Name")
            return EntityBuilder.createActivity(name, null, ActivityType.PLAYING)
        }

        /**
         * Creates a new Activity instance with the specified name and url.
         * <br></br>The specified URL must be valid according to discord standards in order to display as "streaming" in the official client.
         * A valid streaming URL must be derived from `https://twitch.tv/` or `https://youtube.com/watch?v=` and can be verified using [.isValidStreamingUrl]. (see documentation)
         *
         * @param  name
         * The not-null name of the newly created game
         * @param  url
         * The streaming url to use, required to display as "streaming"
         *
         * @throws IllegalArgumentException
         * If the specified name is null, empty or longer than {@value #MAX_ACTIVITY_NAME_LENGTH} characters
         *
         * @return A valid Activity instance with the provided name and url
         *
         * @see .isValidStreamingUrl
         */
        @JvmStatic
        @Nonnull
        fun streaming(@Nonnull name: String, url: String?): Activity? {
            var name = name
            Checks.notEmpty(name, "Provided game name")
            name = if (Helpers.isBlank(name)) name else name.trim { it <= ' ' }
            Checks.notLonger(name, MAX_ACTIVITY_NAME_LENGTH, "Name")
            val type: ActivityType
            type =
                if (isValidStreamingUrl(url)) ActivityType.STREAMING else ActivityType.PLAYING
            return EntityBuilder.createActivity(name, url, type)
        }

        /**
         * Creates a new Activity instance with the specified name.
         * <br></br>This will display as `Listening to name` in the official client
         *
         * @param  name
         * The not-null name of the newly created game
         *
         * @throws IllegalArgumentException
         * if the specified name is null, empty, blank or longer than {@value #MAX_ACTIVITY_NAME_LENGTH} characters
         *
         * @return A valid Activity instance with the provided name with [net.dv8tion.jda.api.entities.Activity.ActivityType.LISTENING]
         */
        @JvmStatic
        @Nonnull
        fun listening(@Nonnull name: String): Activity? {
            var name = name
            Checks.notBlank(name, "Name")
            name = name.trim { it <= ' ' }
            Checks.notLonger(name, MAX_ACTIVITY_NAME_LENGTH, "Name")
            return EntityBuilder.createActivity(name, null, ActivityType.LISTENING)
        }

        /**
         * Creates a new Activity instance with the specified name.
         * <br></br>This will display as `Watching name` in the official client
         *
         * @param  name
         * The not-null name of the newly created game
         *
         * @throws IllegalArgumentException
         * if the specified name is null, empty, blank or longer than {@value #MAX_ACTIVITY_NAME_LENGTH} characters
         *
         * @return A valid Activity instance with the provided name with [net.dv8tion.jda.api.entities.Activity.ActivityType.WATCHING]
         */
        @JvmStatic
        @Nonnull
        fun watching(@Nonnull name: String): Activity? {
            var name = name
            Checks.notBlank(name, "Name")
            name = name.trim { it <= ' ' }
            Checks.notLonger(name, MAX_ACTIVITY_NAME_LENGTH, "Name")
            return EntityBuilder.createActivity(name, null, ActivityType.WATCHING)
        }

        /**
         * Creates a new Activity instance with the specified name.
         * <br></br>This will display as `Competing in name` in the official client
         *
         * @param  name
         * The not-null name of the newly created game
         *
         * @throws IllegalArgumentException
         * If the specified name is null, empty, blank or longer than {@value #MAX_ACTIVITY_NAME_LENGTH} characters
         *
         * @return A valid Activity instance with the provided name with [net.dv8tion.jda.api.entities.Activity.ActivityType.COMPETING]
         *
         * @since  4.2.1
         */
        @JvmStatic
        @Nonnull
        fun competing(@Nonnull name: String): Activity? {
            var name = name
            Checks.notBlank(name, "Name")
            name = name.trim { it <= ' ' }
            Checks.notLonger(name, MAX_ACTIVITY_NAME_LENGTH, "Name")
            return EntityBuilder.createActivity(name, null, ActivityType.COMPETING)
        }

        /**
         * Creates a new Activity instance with the specified name.
         * <br></br>This will display without a prefix in the official client
         *
         * @param  name
         * The not-null name of the newly created status
         *
         * @throws IllegalArgumentException
         * If the specified name is null, empty, blank or longer than {@value #MAX_ACTIVITY_NAME_LENGTH} characters
         *
         * @return A valid Activity instance with the provided name with [net.dv8tion.jda.api.entities.Activity.ActivityType.CUSTOM_STATUS]
         */
        @JvmStatic
        @Nonnull
        fun customStatus(@Nonnull name: String): Activity? {
            var name = name
            Checks.notBlank(name, "Name")
            name = name.trim { it <= ' ' }
            Checks.notLonger(name, MAX_ACTIVITY_NAME_LENGTH, "Name")
            return EntityBuilder.createActivity(name, null, ActivityType.CUSTOM_STATUS)
        }

        /**
         * Creates a new Activity instance with the specified name.
         *
         * @param  type
         * The [ActivityType][net.dv8tion.jda.api.entities.Activity.ActivityType] to use
         * @param  name
         * The not-null name of the newly created game
         *
         * @throws IllegalArgumentException
         *
         *  * If the specified ActivityType is null or unsupported
         *  * If the specified name is null, empty or longer than {@value #MAX_ACTIVITY_NAME_LENGTH} characters
         *
         *
         * @return A valid Activity instance with the provided name
         */
        @Nonnull
        fun of(@Nonnull type: ActivityType, @Nonnull name: String): Activity? {
            return of(type, name, null)
        }

        /**
         * Creates a new Activity instance with the specified name and url.
         * <br></br>The provided url would only be used for [ActivityType.STREAMING][net.dv8tion.jda.api.entities.Activity.ActivityType.STREAMING]
         * and should be a twitch url.
         *
         * @param  type
         * The [ActivityType][net.dv8tion.jda.api.entities.Activity.ActivityType] to use
         * @param  name
         * The not-null name of the newly created game or custom status text
         * @param  url
         * The streaming url to use, required to display as "streaming".
         *
         * @throws IllegalArgumentException
         *
         *  * If the specified ActivityType is null or unsupported
         *  * If the specified name is null, empty or longer than {@value #MAX_ACTIVITY_NAME_LENGTH} characters
         *
         *
         * @return A valid Activity instance with the provided name and url
         *
         * @see .isValidStreamingUrl
         */
        @Nonnull
        fun of(@Nonnull type: ActivityType, @Nonnull name: String, url: String?): Activity? {
            Checks.notNull(type, "Type")
            return when (type) {
                ActivityType.PLAYING -> playing(
                    name
                )

                ActivityType.STREAMING -> streaming(
                    name,
                    url
                )

                ActivityType.LISTENING -> listening(
                    name
                )

                ActivityType.WATCHING -> watching(
                    name
                )

                ActivityType.COMPETING -> competing(
                    name
                )

                ActivityType.CUSTOM_STATUS -> customStatus(
                    name
                )

                else -> throw IllegalArgumentException("ActivityType $type is not supported!")
            }
        }

        /**
         * Checks if a given String is a valid Twitch/Youtube streaming url (ie, one that will display "Streaming" on the Discord client).
         *
         * @param  url
         * The url to check.
         *
         * @return True if the provided url is valid for triggering Discord's streaming status
         */
        fun isValidStreamingUrl(url: String?): Boolean {
            return url != null && STREAMING_URL.matcher(url).matches()
        }

        /** The Pattern used for [.isValidStreamingUrl]  */
        val STREAMING_URL =
            Pattern.compile("https?://(www\\.)?(twitch\\.tv/|youtube\\.com/watch\\?v=).+", Pattern.CASE_INSENSITIVE)

        /** Maximum length for an activity name  */
        const val MAX_ACTIVITY_NAME_LENGTH = 128

        /** Maximum length for an activity state  */
        const val MAX_ACTIVITY_STATE_LENGTH = 128
    }
}

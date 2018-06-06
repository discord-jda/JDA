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

import net.dv8tion.jda.core.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Used to hold additional information about a users {@link net.dv8tion.jda.core.entities.Game Game}
 * relevant to <a href="https://discordapp.com/developers/docs/rich-presence/best-practices" target="_blank">Rich Presence</a>.
 *
 * @since  3.4.0
 */
public class RichPresence extends Game
{
    protected final long applicationId;

    protected final Party party;
    protected final String details;
    protected final String state;
    protected final Image largeImage;
    protected final Image smallImage;
    protected final String sessionId;
    protected final String syncId;
    protected final int flags;

    protected RichPresence(
        GameType type, String name, String url, long applicationId,
        Party party, String details, String state, Timestamps timestamps, String syncId, String sessionId, int flags,
        String largeImageKey, String largeImageText, String smallImageKey, String smallImageText)
    {
        super(name, url, type, timestamps);
        this.applicationId = applicationId;
        this.party = party;
        this.details = details;
        this.state = state;
        this.sessionId = sessionId;
        this.syncId = syncId;
        this.flags = flags;
        this.largeImage = largeImageKey != null ? new Image(largeImageKey, largeImageText) : null;
        this.smallImage = smallImageKey != null ? new Image(smallImageKey, smallImageText) : null;
    }

    @Override
    public boolean isRich()
    {
        return true;
    }

    @Override
    public RichPresence asRichPresence()
    {
        return this;
    }

    /**
     * The ID for the responsible application.
     *
     * @return The ID for the application
     */
    public long getApplicationIdLong()
    {
        return applicationId;
    }

    /**
     * The ID for the responsible application.
     *
     * @return The ID for the application
     */
    @Nonnull
    public String getApplicationId()
    {
        return Long.toUnsignedString(applicationId);
    }

    /**
     * Session ID for this presence.
     * <br>Used by spotify integration.
     *
     * @return Session ID
     */
    @Nullable
    public String getSessionId()
    {
        return sessionId;
    }

    /**
     * Sync ID for this presence.
     * <br>Used by spotify integration.
     *
     * @return Sync ID
     */
    @Nullable
    public String getSyncId()
    {
        return syncId;
    }

    /**
     * Flags for this presence
     *
     * @return The flags for this presence
     *
     * @see    ActivityFlag
     * @see    ActivityFlag#getFlags(int)
     */
    public int getFlags()
    {
        return flags;
    }

    /**
     * Flags for this presence in an enum set
     *
     * @return The flags for this presence
     *
     * @see    ActivityFlag
     * @see    ActivityFlag#getFlags(int)
     */
    public EnumSet<ActivityFlag> getFlagSet()
    {
        return ActivityFlag.getFlags(getFlags());
    }

    /**
     * The user's current party status
     * <br>Example: "Looking to Play", "Playing Solo", "In a Group"
     *
     * @return The user's current party status
     */
    @Nullable
    public String getState()
    {
        return state;
    }

    /**
     * What the player is currently doing
     * <br>Example: "Competitive - Captain's Mode", "In Queue", "Unranked PvP"
     *
     * @return What the player is currently doing
     */
    @Nullable
    public String getDetails()
    {
        return details;
    }

    /**
     * Information on the active party of the player
     *
     * @return {@link net.dv8tion.jda.core.entities.RichPresence.Party Party} wrapper or {@code null} if unset
     */
    @Nullable
    public Party getParty()
    {
        return party;
    }

    /**
     * Information on the large image displayed in the profile view
     *
     * @return {@link net.dv8tion.jda.core.entities.RichPresence.Image Image} wrapper or {@code null} if unset
     */
    @Nullable
    public Image getLargeImage()
    {
        return largeImage;
    }

    /**
     * Information on the small corner image displayed in the profile view
     *
     * @return {@link net.dv8tion.jda.core.entities.RichPresence.Image Image} wrapper or {@code null} if unset
     */
    @Nullable
    public Image getSmallImage()
    {
        return smallImage;
    }

    @Override
    public String toString()
    {
        return String.format("RichPresence(%s / %s)", name, getApplicationId());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(applicationId, state, details, party, sessionId, syncId, flags, timestamps, largeImage, smallImage);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof RichPresence))
            return false;
        RichPresence p = (RichPresence) o;
        return applicationId == p.applicationId
            && Objects.equals(name, p.name)
            && Objects.equals(url, p.url)
            && Objects.equals(type, p.type)
            && Objects.equals(state, p.state)
            && Objects.equals(details, p.details)
            && Objects.equals(party, p.party)
            && Objects.equals(sessionId, p.sessionId)
            && Objects.equals(syncId, p.syncId)
            && Objects.equals(flags, p.flags)
            && Objects.equals(timestamps, p.timestamps)
            && Objects.equals(largeImage, p.largeImage)
            && Objects.equals(smallImage, p.smallImage);
    }

    /**
     * Used to hold information on images within a Rich Presence profile
     */
    public class Image
    {
        protected final String key;
        protected final String text;

        public Image(String key, String text)
        {
            this.key = key;
            this.text = text;
        }

        /**
         * The key for this image, used for {@link #getUrl()}
         *
         * @return The key for this image
         */
        @Nonnull
        public String getKey()
        {
            return key;
        }

        /**
         * Text which is displayed when hovering the image in the official client
         *
         * @return Hover text for this image, or {@code null}
         */
        @Nullable
        public String getText()
        {
            return text;
        }

        /**
         * URL for this image, combination of {@link #getApplicationId()} and {@link #getKey()}
         *
         * @return URL for this image
         */
        @Nonnull
        public String getUrl()
        {
            if (key.startsWith("spotify:"))
                return "https://i.scdn.co/image/" + key.substring("spotify:".length());
            if (key.startsWith("twitch:"))
                return String.format("https://static-cdn.jtvnw.net/previews-ttv/live_user_%s-108x60.jpg", key.substring("twitch:".length()));
            return "https://cdn.discordapp.com/app-assets/" + applicationId + "/" + key + ".png";
        }

        @Override
        public String toString()
        {
            return String.format("RichPresenceImage(%s | %s)", key, text);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Image))
                return false;
            Image i = (Image) obj;
            return Objects.equals(key, i.key) && Objects.equals(text, i.text);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(key, text);
        }
    }

    /**
     * Represents the start and end timestamps for a running match
     */
    public static class Timestamps
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

    /**
     * Holds information on a player's party
     */
    public static class Party
    {
        protected final String id;
        protected final long size;

        protected final long max;

        public Party(String id, long size, long max)
        {
            this.id = id;
            this.size = size;
            this.max = max;
        }

        /**
         * ID for this party, relevant to the game.
         *
         * @return The ID for this party, or {@code null} if unset
         */
        @Nullable
        public String getId()
        {
            return id;
        }

        /**
         * The current size of this party, or {@code 0} if unset
         *
         * @return The current size of this party, or {@code 0} if unset
         */
        public int getSize()
        {
            return (int)size;
        }

        /**
         * The current size of this party, or {@code 0} if unset
         *
         * @return The current size of this party, or {@code 0} if unset
         */
        public long getSizeAsLong()
        {
            return size;
        }

        /**
         * The maximum size of this party, or {@code 0} if unset
         *
         * @return The maximum size of this party, or {@code 0} if unset
         */
        public int getMax()
        {
            return (int)max;
        }

        /**
         * The maximum size of this party, or {@code 0} if unset
         *
         * @return The maximum size of this party, or {@code 0} if unset
         */
        public long getMaxAsLong()
        {
            return max;
        }

        @Override
        public String toString()
        {
            return String.format("RichPresenceParty(%s | [%d, %d])", id, size, max);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Party))
                return false;
            Party p = (Party) obj;
            return size == p.size && max == p.max && Objects.equals(id, p.id);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(id, size, max);
        }
    }
}

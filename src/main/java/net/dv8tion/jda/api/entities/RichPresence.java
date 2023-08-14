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

import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Used to hold additional information about a users {@link Activity Activity}
 * relevant to <a href="https://discord.com/developers/docs/rich-presence/best-practices" target="_blank">Rich Presence</a>.
 *
 * @since  3.4.0
 *
 * @see    Activity#asRichPresence()
 */
public interface RichPresence extends Activity
{
    /**
     * The ID for the responsible application.
     *
     * @return The ID for the application
     */
    long getApplicationIdLong();

    /**
     * The ID for the responsible application.
     *
     * @return The ID for the application
     */
    @Nonnull
    String getApplicationId();

    /**
     * Session ID for this presence.
     * <br>Used by spotify integration.
     *
     * @return Session ID
     */
    @Nullable
    String getSessionId();

    /**
     * Sync ID for this presence.
     * <br>Used by spotify integration.
     *
     * @return Sync ID
     */
    @Nullable
    String getSyncId();

    /**
     * Flags for this presence
     *
     * @return The flags for this presence
     *
     * @see    ActivityFlag
     * @see    ActivityFlag#getFlags(int)
     */
    int getFlags();

    /**
     * Flags for this presence in an enum set
     *
     * @return The flags for this presence
     *
     * @see    ActivityFlag
     * @see    ActivityFlag#getFlags(int)
     */
    EnumSet<ActivityFlag> getFlagSet();

    /**
     * What the player is currently doing
     * <br>Example: "Competitive - Captain's Mode", "In Queue", "Unranked PvP"
     *
     * @return What the player is currently doing
     */
    @Nullable
    String getDetails();

    /**
     * Information on the active party of the player
     *
     * @return {@link net.dv8tion.jda.api.entities.RichPresence.Party Party} wrapper or {@code null} if unset
     */
    @Nullable
    Party getParty();

    /**
     * Information on the large image displayed in the profile view
     *
     * @return {@link net.dv8tion.jda.api.entities.RichPresence.Image Image} wrapper or {@code null} if unset
     */
    @Nullable
    Image getLargeImage();

    /**
     * Information on the small corner image displayed in the profile view
     *
     * @return {@link net.dv8tion.jda.api.entities.RichPresence.Image Image} wrapper or {@code null} if unset
     */
    @Nullable
    Image getSmallImage();

    /**
     * Used to hold information on images within a Rich Presence profile
     */
    class Image
    {
        protected final String key;
        protected final String text;
        protected final String applicationId;

        public Image(long applicationId, String key, String text)
        {
            this.applicationId = Long.toUnsignedString(applicationId);
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
                return String.format("https://static-cdn.jtvnw.net/previews-ttv/live_user_%s-1920x1080.png", key.substring("twitch:".length()));
            return "https://cdn.discordapp.com/app-assets/" + applicationId + "/" + key + ".png";
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .addMetadata("key", getKey())
                    .addMetadata("text", getText())
                    .toString();
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
     * Holds information on a player's party
     */
    class Party
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
        public long getSize()
        {
            return size;
        }

        /**
         * The maximum size of this party, or {@code 0} if unset
         *
         * @return The maximum size of this party, or {@code 0} if unset
         */
        public long getMax()
        {
            return max;
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .addMetadata("id", getId())
                    .addMetadata("size", getSize())
                    .addMetadata("max", getMax())
                    .toString();
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

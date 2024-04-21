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

import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import javax.annotation.Nonnull

/**
 * Used to hold additional information about a users [Activity]
 * relevant to [Rich Presence](https://discord.com/developers/docs/rich-presence/best-practices).
 *
 * @since  3.4.0
 *
 * @see Activity.asRichPresence
 */
open interface RichPresence : Activity {
    /**
     * The ID for the responsible application.
     *
     * @return The ID for the application
     */
    val applicationIdLong: Long

    @get:Nonnull
    val applicationId: String?

    /**
     * Session ID for this presence.
     * <br></br>Used by spotify integration.
     *
     * @return Session ID
     */
    @JvmField
    val sessionId: String?

    /**
     * Sync ID for this presence.
     * <br></br>Used by spotify integration.
     *
     * @return Sync ID
     */
    @JvmField
    val syncId: String?

    /**
     * Flags for this presence
     *
     * @return The flags for this presence
     *
     * @see ActivityFlag
     *
     * @see ActivityFlag.getFlags
     */
    val flags: Int

    /**
     * Flags for this presence in an enum set
     *
     * @return The flags for this presence
     *
     * @see ActivityFlag
     *
     * @see ActivityFlag.getFlags
     */
    val flagSet: EnumSet<ActivityFlag?>?

    /**
     * What the player is currently doing
     * <br></br>Example: "Competitive - Captain's Mode", "In Queue", "Unranked PvP"
     *
     * @return What the player is currently doing
     */
    val details: String?

    /**
     * Information on the active party of the player
     *
     * @return [Party][net.dv8tion.jda.api.entities.RichPresence.Party] wrapper or `null` if unset
     */
    @JvmField
    val party: Party?

    /**
     * Information on the large image displayed in the profile view
     *
     * @return [Image][net.dv8tion.jda.api.entities.RichPresence.Image] wrapper or `null` if unset
     */
    @JvmField
    val largeImage: Image?

    /**
     * Information on the small corner image displayed in the profile view
     *
     * @return [Image][net.dv8tion.jda.api.entities.RichPresence.Image] wrapper or `null` if unset
     */
    @JvmField
    val smallImage: Image?

    /**
     * Used to hold information on images within a Rich Presence profile
     */
    class Image(applicationId: Long, key: String, text: String) {
        /**
         * The key for this image, used for [.getUrl]
         *
         * @return The key for this image
         */
        @JvmField
        @get:Nonnull
        val key: String
        protected val text: String
        protected val applicationId: String

        init {
            this.applicationId = java.lang.Long.toUnsignedString(applicationId)
            this.key = key
            this.text = text
        }

        /**
         * Text which is displayed when hovering the image in the official client
         *
         * @return Hover text for this image, or `null`
         */
        fun getText(): String? {
            return text
        }

        /**
         * URL for this image, combination of [.getApplicationId] and [.getKey]
         *
         * @return URL for this image
         */
        @Nonnull
        fun getUrl(): String {
            if (key.startsWith("spotify:")) return "https://i.scdn.co/image/" + key.substring("spotify:".length)
            if (key.startsWith("twitch:")) return String.format(
                "https://static-cdn.jtvnw.net/previews-ttv/live_user_%s-1920x1080.png",
                key.substring("twitch:".length)
            )
            return "https://cdn.discordapp.com/app-assets/" + applicationId + "/" + key + ".png"
        }

        public override fun toString(): String {
            return EntityString(this)
                .addMetadata("key", key)
                .addMetadata("text", getText())
                .toString()
        }

        public override fun equals(obj: Any?): Boolean {
            if (!(obj is Image)) return false
            val i: Image = obj
            return Objects.equals(key, i.key) && Objects.equals(text, i.text)
        }

        public override fun hashCode(): Int {
            return Objects.hash(key, text)
        }
    }

    /**
     * Holds information on a player's party
     */
    class Party(id: String, size: Long, max: Long) {
        protected val id: String

        /**
         * The current size of this party, or `0` if unset
         *
         * @return The current size of this party, or `0` if unset
         */
        @JvmField
        val size: Long

        /**
         * The maximum size of this party, or `0` if unset
         *
         * @return The maximum size of this party, or `0` if unset
         */
        @JvmField
        val max: Long

        init {
            this.id = id
            this.size = size
            this.max = max
        }

        /**
         * ID for this party, relevant to the game.
         *
         * @return The ID for this party, or `null` if unset
         */
        fun getId(): String? {
            return id
        }

        public override fun toString(): String {
            return EntityString(this)
                .addMetadata("id", getId())
                .addMetadata("size", size)
                .addMetadata("max", max)
                .toString()
        }

        public override fun equals(obj: Any?): Boolean {
            if (!(obj is Party)) return false
            val p: Party = obj
            return (size == p.size) && (max == p.max) && Objects.equals(id, p.id)
        }

        public override fun hashCode(): Int {
            return Objects.hash(id, size, max)
        }
    }
}

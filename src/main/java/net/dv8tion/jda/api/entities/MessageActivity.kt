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

import net.dv8tion.jda.api.entities.MessageActivity.ActivityType
import net.dv8tion.jda.api.utils.ImageProxy
import javax.annotation.Nonnull

/**
 * Represents a [net.dv8tion.jda.api.entities.Message] activity.
 *
 * @see Message.getActivity
 */
class MessageActivity(
    /**
     * The current [ActivityType][net.dv8tion.jda.api.entities.MessageActivity.ActivityType]
     *
     * @return the type of the activity, or [UNKNOWN][ActivityType.UNKNOWN]
     */
    @get:Nonnull val type: ActivityType, private val partyId: String, private val application: Application
) {
    /**
     * The party id discord uses internally, it may be `null`.
     *
     * @return Possibly-null party id
     */
    fun getPartyId(): String? {
        return partyId
    }

    /**
     * The [Application][net.dv8tion.jda.api.entities.MessageActivity.Application] this [MessageActivity][net.dv8tion.jda.api.entities.MessageActivity] may have.
     *
     * @return A possibly-null [net.dv8tion.jda.api.entities.MessageActivity.Application]
     */
    fun getApplication(): Application? {
        return application
    }

    /**
     * Represents the [Application][net.dv8tion.jda.api.entities.MessageActivity.Application] of a MessageActivity, if it has been set.
     */
    class Application(
        /**
         * The name of this Application.
         *
         * @return the applications name
         */
        @get:Nonnull val name: String,
        /**
         * A short description of this Application.
         *
         * @return the applications description
         */
        @get:Nonnull val description: String,
        /**
         * The icon id of this Application.
         *
         * @return the applications icon id
         */
        val iconId: String?,
        /**
         * The cover aka splash id of this Application.
         *
         * @return the applications cover image/id
         */
        val coverId: String?, override val idLong: Long
    ) : ISnowflake {

        val iconUrl: String?
            /**
             * The url of the icon image for this application.
             *
             * @return the url of the icon
             */
            get() = if (iconId == null) null else "https://cdn.discordapp.com/application/$id/$iconId.png"
        val icon: ImageProxy?
            /**
             * Returns an [ImageProxy] for this application's icon.
             *
             * @return Possibly-null [ImageProxy] of this application's icon
             *
             * @see .getIconUrl
             */
            get() {
                val iconUrl = iconUrl
                return iconUrl?.let { ImageProxy(it) }
            }
        val coverUrl: String?
            /**
             * The url of the cover image for this application.
             *
             * @return the url of the cover/splash
             */
            get() = if (coverId == null) null else "https://cdn.discordapp.com/application/$id/$coverId.png"
        val cover: ImageProxy?
            /**
             * Returns an [ImageProxy] for this cover's icon.
             *
             * @return Possibly-null [ImageProxy] of this cover's icon
             *
             * @see .getCoverUrl
             */
            get() {
                val coverUrl = coverUrl
                return coverUrl?.let { ImageProxy(it) }
            }
    }

    /**
     * An enum representing [MessageActivity][net.dv8tion.jda.api.entities.MessageActivity] types.
     */
    enum class ActivityType(
        /**
         * The id of this [ActivityType][net.dv8tion.jda.api.entities.MessageActivity.ActivityType].
         *
         * @return the id of the type
         */
        val id: Int
    ) {
        /**
         * The [MessageActivity][net.dv8tion.jda.api.entities.MessageActivity] type used for inviting people to join a game.
         */
        JOIN(1),

        /**
         * The [MessageActivity][net.dv8tion.jda.api.entities.MessageActivity] type used for inviting people to spectate a game.
         */
        SPECTATE(2),

        /**
         * The [MessageActivity][net.dv8tion.jda.api.entities.MessageActivity] type used for inviting people to listen (Spotify) together.
         */
        LISTENING(3),

        /**
         * The [MessageActivity][net.dv8tion.jda.api.entities.MessageActivity] type used for requesting to join a game.
         */
        JOIN_REQUEST(5),

        /**
         * Represents any unknown or unsupported [MessageActivity][net.dv8tion.jda.api.entities.MessageActivity] types.
         */
        UNKNOWN(-1);

        companion object {
            @JvmStatic
            @Nonnull
            fun fromId(id: Int): ActivityType {
                for (activityType in entries) {
                    if (activityType.id == id) return activityType
                }
                return UNKNOWN
            }
        }
    }
}

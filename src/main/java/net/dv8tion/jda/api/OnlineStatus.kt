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
package net.dv8tion.jda.api

/**
 * Represents the online presence of a [Member][net.dv8tion.jda.api.entities.Member].
 */
enum class OnlineStatus(
    /**
     * The valid API key for this OnlineStatus
     *
     * @return String representation of the valid API key for this OnlineStatus
     *
     * @see [PRESENCE_UPDATE](https://discord.com/developers/docs/topics/gateway.presence-update)
     */
    @JvmField val key: String
) {
    /**
     * Indicates that the user is currently online (green circle)
     */
    ONLINE("online"),

    /**
     * Indicates that the user is currently idle (orange circle)
     */
    IDLE("idle"),

    /**
     * Indicates that the user is currently on do not disturb (red circle)
     * <br></br>This means the user won't receive notifications for mentions.
     */
    DO_NOT_DISTURB("dnd"),

    /**
     * Indicates that the currently logged in account is set to invisible and shows
     * up as [.OFFLINE] for other users.
     * <br></br>Only available for the currently logged in account.
     * <br></br>Other [Members][net.dv8tion.jda.api.entities.Member] will show up as [OFFLINE][net.dv8tion.jda.api.OnlineStatus.OFFLINE] even when they really are INVISIBLE.
     */
    INVISIBLE("invisible"),

    /**
     * Indicates that a member is currently offline or invisible (grey circle)
     */
    OFFLINE("offline"),

    /**
     * Placeholder for possible future online status values that are not listed here yet.
     */
    UNKNOWN("");

    companion object {
        /**
         * Will get the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] from the provided key.
         * <br></br>If the provided key does not have a matching OnlineStatus, this will return [UNKONWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
         *
         * @param  key
         * The key relating to the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] we wish to retrieve.
         *
         * @return The matching [OnlineStatus][net.dv8tion.jda.api.OnlineStatus]. If there is no match, returns [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN].
         */
        @JvmStatic
        fun fromKey(key: String?): OnlineStatus {
            for (onlineStatus: OnlineStatus in entries) {
                if (onlineStatus.key.equals(key, ignoreCase = true)) {
                    return onlineStatus
                }
            }
            return UNKNOWN
        }
    }
}

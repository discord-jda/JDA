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
package net.dv8tion.jda.api.audio.hooks

import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.api.audio.SpeakingMode
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import java.util.*
import javax.annotation.Nonnull

/**
 * Used to monitor an audio connection, ping, and speaking users.
 * <br></br>This provides functionality similar to the functionalities present in the Discord client related to an audio connection.
 */
interface ConnectionListener {
    /**
     * Called when JDA send a heartbeat packet to Discord and Discord sends an acknowledgement. The time difference
     * between sending and receiving the acknowledgement is calculated as the ping.
     *
     * @param  ping
     * The time, in milliseconds, for round-trip packet travel to discord.
     */
    fun onPing(ping: Long)

    /**
     * Called when the status of the audio channel changes. Used to track the connection state of the audio connection
     * for easy debug and status display for clients.
     *
     * @param  status
     * The new [ConnectionStatus][net.dv8tion.jda.api.audio.hooks.ConnectionStatus] of the audio connection.
     */
    fun onStatusChange(@Nonnull status: ConnectionStatus?)

    /**
     * This method is an easy way to detect if a user is talking. Discord sends us an event when a user starts or stops
     * talking and it is parallel to the audio socket, so this event could come milliseconds before or after audio begins
     * or stops. This method is brilliant for clients wanting to display that a user is currently talking.
     *
     *
     * Unlike the [ AudioReceiveHandler.handleCombinedAudio(CombinedAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleCombinedAudio] and
     * [ AudioReceiveHandler.handleUserAudio(UserAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleUserAudio] methods which are
     * fired extremely often, this method is fired as a flag for the beginning and ending of audio transmission, and as such
     * is only fired when that changes. So while the [ AudioReceiveHandler.handleUserAudio(UserAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleUserAudio] method is fired every time JDA receives audio data from Discord,
     * this is only fired when that stream starts and when it stops.
     * <br></br>If the user speaks for 3 minutes straight without ever stopping, then this would fire 2 times, once at the beginning
     * and once after 3 minutes when they stop talking even though the [ AudioReceiveHandler.handleUserAudio(UserAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleUserAudio] method was fired thousands of times over the course of the 3 minutes.
     *
     * @param  user
     * Never-null [User][net.dv8tion.jda.api.entities.User] who's talking status has changed.
     * @param  speaking
     * If true, the user has begun transmitting audio.
     *
     */
    @ForRemoval
    @ReplaceWith("onUserSpeakingModeUpdate(User, EnumSet<SpeakingMode>)")
    @Deprecated(
        """This method no longer represents the actual speaking state of the user.
                  Discord does not send updates when a user starts and stops speaking anymore.
                  You can use {@link #onUserSpeakingModeUpdate(UserSnowflake, EnumSet)} to see when a user changes their speaking mode,
                  or use an {@link net.dv8tion.jda.api.audio.AudioReceiveHandler AudioReceiveHandler} to check when a user is speaking."""
    )
    fun onUserSpeaking(@Nonnull user: User?, speaking: Boolean) {
    }

    /**
     * This method is an easy way to detect if a user is talking. Discord sends us an event when a user starts or stops
     * talking and it is parallel to the audio socket, so this event could come milliseconds before or after audio begins
     * or stops. This method is brilliant for clients wanting to display that a user is currently talking.
     *
     *
     * Unlike the [ AudioReceiveHandler.handleCombinedAudio(CombinedAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleCombinedAudio] and
     * [ AudioReceiveHandler.handleUserAudio(UserAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleUserAudio] methods which are
     * fired extremely often, this method is fired as a flag for the beginning and ending of audio transmission, and as such
     * is only fired when that changes. So while the [ AudioReceiveHandler.handleUserAudio(UserAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleUserAudio] method is fired every time JDA receives audio data from Discord,
     * this is only fired when that stream starts and when it stops.
     * <br></br>If the user speaks for 3 minutes straight without ever stopping, then this would fire 2 times, once at the beginning
     * and once after 3 minutes when they stop talking even though the [ AudioReceiveHandler.handleUserAudio(UserAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleUserAudio] method was fired thousands of times over the course of the 3 minutes.
     *
     * @param  user
     * Never-null [User][net.dv8tion.jda.api.entities.User] who's talking status has changed.
     * @param  modes
     * EnumSet, containing the active speaking modes.
     * Empty if the user has stopped transmitting audio.
     *
     * @see java.util.EnumSet EnumSet
     *
     * @see net.dv8tion.jda.api.audio.SpeakingMode SpeakingMode
     *
     *
     */
    @ForRemoval
    @ReplaceWith("onUserSpeakingModeUpdate(User, EnumSet<SpeakingMode>)")
    @Deprecated(
        """This method no longer represents the actual speaking state of the user.
                  Discord does not send updates when a user starts and stops speaking anymore.
                  You can use {@link #onUserSpeakingModeUpdate(UserSnowflake, EnumSet)} to see when a user changes their speaking mode,
                  or use an {@link net.dv8tion.jda.api.audio.AudioReceiveHandler AudioReceiveHandler} to check when a user is speaking."""
    )
    fun onUserSpeaking(@Nonnull user: User?, @Nonnull modes: EnumSet<SpeakingMode?>?) {
    }

    /**
     * This method is an easy way to detect if a user is talking. Discord sends us an event when a user starts or stops
     * talking and it is parallel to the audio socket, so this event could come milliseconds before or after audio begins
     * or stops. This method is brilliant for clients wanting to display that a user is currently talking.
     *
     *
     * Unlike the [ AudioReceiveHandler.handleCombinedAudio(CombinedAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleCombinedAudio] and
     * [ AudioReceiveHandler.handleUserAudio(UserAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleUserAudio] methods which are
     * fired extremely often, this method is fired as a flag for the beginning and ending of audio transmission, and as such
     * is only fired when that changes. So while the [ AudioReceiveHandler.handleUserAudio(UserAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleUserAudio] method is fired every time JDA receives audio data from Discord,
     * this is only fired when that stream starts and when it stops.
     * <br></br>If the user speaks for 3 minutes straight without ever stopping, then this would fire 2 times, once at the beginning
     * and once after 3 minutes when they stop talking even though the [ AudioReceiveHandler.handleUserAudio(UserAudio)][net.dv8tion.jda.api.audio.AudioReceiveHandler.handleUserAudio] method was fired thousands of times over the course of the 3 minutes.
     *
     * @param  user
     * Never-null [User][net.dv8tion.jda.api.entities.User] who's talking status has changed.
     * @param  speaking
     * If true, the user has begun transmitting audio.
     * @param  soundshare
     * If true, the user is using soundshare
     *
     */
    @ForRemoval
    @ReplaceWith("onUserSpeakingModeUpdate(User, EnumSet<SpeakingMode>)")
    @Deprecated(
        """This method no longer represents the actual speaking state of the user.
                  Discord does not send updates when a user starts and stops speaking anymore.
                  You can use {@link #onUserSpeakingModeUpdate(UserSnowflake, EnumSet)} to see when a user changes their speaking mode,
                  or use an {@link net.dv8tion.jda.api.audio.AudioReceiveHandler AudioReceiveHandler} to check when a user is speaking."""
    )
    fun onUserSpeaking(@Nonnull user: User?, speaking: Boolean, soundshare: Boolean) {
    }

    /**
     * This method is used to listen for users changing their speaking mode.
     *
     * Whenever a user joins a voice channel, this is fired once to define the initial speaking modes.
     *
     *
     * To detect when a user is speaking, a [AudioReceiveHandler][net.dv8tion.jda.api.audio.AudioReceiveHandler] should be used instead.
     *
     *
     * **Note:** This requires the user to be currently in the cache.
     * You can use [MemberCachePolicy.VOICE][net.dv8tion.jda.api.utils.MemberCachePolicy.VOICE] to cache currently connected users.
     * Alternatively, use [.onUserSpeakingModeUpdate] to avoid cache.
     *
     * @param user
     * The user who changed their speaking mode
     * @param modes
     * The new speaking modes of the user
     */
    fun onUserSpeakingModeUpdate(@Nonnull user: User?, @Nonnull modes: EnumSet<SpeakingMode?>?) {}

    /**
     * This method is used to listen for users changing their speaking mode.
     *
     * Whenever a user joins a voice channel, this is fired once to define the initial speaking modes.
     *
     *
     * To detect when a user is speaking, a [AudioReceiveHandler][net.dv8tion.jda.api.audio.AudioReceiveHandler] should be used instead.
     *
     *
     * This method works independently of the user cache. The provided user might not be cached.
     *
     * @param user
     * The user who changed their speaking mode
     * @param modes
     * The new speaking modes of the user
     */
    fun onUserSpeakingModeUpdate(@Nonnull user: UserSnowflake?, @Nonnull modes: EnumSet<SpeakingMode?>?) {}
}

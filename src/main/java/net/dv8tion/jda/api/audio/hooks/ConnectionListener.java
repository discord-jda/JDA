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

package net.dv8tion.jda.api.audio.hooks;

import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.EnumSet;

import javax.annotation.Nonnull;

/**
 * Used to monitor an audio connection, ping, and speaking users.
 * <br>This provides functionality similar to the functionalities present in the Discord client related to an audio connection.
 */
public interface ConnectionListener {
    /**
     * Called when JDA send a heartbeat packet to Discord and Discord sends an acknowledgement. The time difference
     * between sending and receiving the acknowledgement is calculated as the ping.
     *
     * @param  ping
     *         The time, in milliseconds, for round-trip packet travel to discord.
     */
    default void onPing(long ping) {}

    /**
     * Called when the status of the audio channel changes. Used to track the connection state of the audio connection
     * for easy debug and status display for clients.
     *
     * @param  status
     *         The new {@link net.dv8tion.jda.api.audio.hooks.ConnectionStatus ConnectionStatus} of the audio connection.
     */
    default void onStatusChange(@Nonnull ConnectionStatus status) {}

    /**
     * This method is used to listen for users changing their speaking mode.
     * <p>Whenever a user joins a voice channel, this is fired once to define the initial speaking modes.
     *
     * <p>To detect when a user is speaking, a {@link net.dv8tion.jda.api.audio.AudioReceiveHandler AudioReceiveHandler} should be used instead.
     *
     * <p><b>Note:</b> This requires the user to be currently in the cache.
     * You can use {@link net.dv8tion.jda.api.utils.MemberCachePolicy#VOICE MemberCachePolicy.VOICE} to cache currently connected users.
     * Alternatively, use {@link #onUserSpeakingModeUpdate(UserSnowflake, EnumSet)} to avoid cache.
     *
     * @param user
     *        The user who changed their speaking mode
     * @param modes
     *        The new speaking modes of the user
     */
    default void onUserSpeakingModeUpdate(@Nonnull User user, @Nonnull EnumSet<SpeakingMode> modes) {}

    /**
     * This method is used to listen for users changing their speaking mode.
     * <p>Whenever a user joins a voice channel, this is fired once to define the initial speaking modes.
     *
     * <p>To detect when a user is speaking, a {@link net.dv8tion.jda.api.audio.AudioReceiveHandler AudioReceiveHandler} should be used instead.
     *
     * <p>This method works independently of the user cache. The provided user might not be cached.
     *
     * @param user
     *        The user who changed their speaking mode
     * @param modes
     *        The new speaking modes of the user
     */
    default void onUserSpeakingModeUpdate(@Nonnull UserSnowflake user, @Nonnull EnumSet<SpeakingMode> modes) {}
}

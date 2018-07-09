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

package net.dv8tion.jda.core.audio.hooks;

import net.dv8tion.jda.core.audio.SpeakingMode;
import net.dv8tion.jda.core.entities.User;

import java.util.EnumSet;

/**
 * Used to monitor an audio connection, ping, and speaking users.
 * <br>This provides functionality similar to the functionalities present in the Discord client related to an audio connection.
 */
public interface ConnectionListener
{
    /**
     * Called when JDA send a heartbeat packet to Discord and Discord sends an acknowledgement. The time difference
     * between sending and receiving the acknowledgement is calculated as the ping.
     *
     * @param  ping
     *         The time, in milliseconds, for round-trip packet travel to discord.
     */
    void onPing(long ping);

    /**
     * Called when the status of the audio channel changes. Used to track the connection state of the audio connection
     * for easy debug and status display for clients.
     *
     * @param  status
     *         The new {@link net.dv8tion.jda.core.audio.hooks.ConnectionStatus ConnectionStatus} of the audio connection.
     */
    void onStatusChange(ConnectionStatus status);

    /**
     * This method is an easy way to detect if a user is talking. Discord sends us an event when a user starts or stops
     * talking and it is parallel to the audio socket, so this event could come milliseconds before or after audio begins
     * or stops. This method is brilliant for clients wanting to display that a user is currently talking.
     * <p>
     * Unlike the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleCombinedAudio(net.dv8tion.jda.core.audio.CombinedAudio)
     * AudioReceiveHandler.handleCombinedAudio(CombinedAudio)} and
     * {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)
     * AudioReceiveHandler.handleUserAudio(UserAudio)} methods which are
     * fired extremely often, this method is fired as a flag for the beginning and ending of audio transmission, and as such
     * is only fired when that changes. So while the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)
     * AudioReceiveHandler.handleUserAudio(UserAudio)} method is fired every time JDA receives audio data from Discord,
     * this is only fired when that stream starts and when it stops.
     * <br>If the user speaks for 3 minutes straight without ever stopping, then this would fire 2 times, once at the beginning
     * and once after 3 minutes when they stop talking even though the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)
     * AudioReceiveHandler.handleUserAudio(UserAudio)} method was fired thousands of times over the course of the 3 minutes.
     *
     * @param  user
     *         Never-null {@link net.dv8tion.jda.core.entities.User User} who's talking status has changed.
     * @param  speaking
     *         If true, the user has begun transmitting audio.
     */
    void onUserSpeaking(User user, boolean speaking);

    /**
     * This method is an easy way to detect if a user is talking. Discord sends us an event when a user starts or stops
     * talking and it is parallel to the audio socket, so this event could come milliseconds before or after audio begins
     * or stops. This method is brilliant for clients wanting to display that a user is currently talking.
     * <p>
     * Unlike the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleCombinedAudio(net.dv8tion.jda.core.audio.CombinedAudio)
     * AudioReceiveHandler.handleCombinedAudio(CombinedAudio)} and
     * {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)
     * AudioReceiveHandler.handleUserAudio(UserAudio)} methods which are
     * fired extremely often, this method is fired as a flag for the beginning and ending of audio transmission, and as such
     * is only fired when that changes. So while the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)
     * AudioReceiveHandler.handleUserAudio(UserAudio)} method is fired every time JDA receives audio data from Discord,
     * this is only fired when that stream starts and when it stops.
     * <br>If the user speaks for 3 minutes straight without ever stopping, then this would fire 2 times, once at the beginning
     * and once after 3 minutes when they stop talking even though the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)
     * AudioReceiveHandler.handleUserAudio(UserAudio)} method was fired thousands of times over the course of the 3 minutes.
     *
     * @param  user
     *         Never-null {@link net.dv8tion.jda.core.entities.User User} who's talking status has changed.
     * @param  modes
     *         EnumSet, containing the active speaking modes.
     *         Empty if the user has stopped transmitting audio.
     *
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.core.audio.SpeakingMode SpeakingMode
     */
    default void onUserSpeaking(User user, EnumSet<SpeakingMode> modes) {}


    /**
     * This method is an easy way to detect if a user is talking. Discord sends us an event when a user starts or stops
     * talking and it is parallel to the audio socket, so this event could come milliseconds before or after audio begins
     * or stops. This method is brilliant for clients wanting to display that a user is currently talking.
     * <p>
     * Unlike the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleCombinedAudio(net.dv8tion.jda.core.audio.CombinedAudio)
     * AudioReceiveHandler.handleCombinedAudio(CombinedAudio)} and
     * {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)
     * AudioReceiveHandler.handleUserAudio(UserAudio)} methods which are
     * fired extremely often, this method is fired as a flag for the beginning and ending of audio transmission, and as such
     * is only fired when that changes. So while the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)
     * AudioReceiveHandler.handleUserAudio(UserAudio)} method is fired every time JDA receives audio data from Discord,
     * this is only fired when that stream starts and when it stops.
     * <br>If the user speaks for 3 minutes straight without ever stopping, then this would fire 2 times, once at the beginning
     * and once after 3 minutes when they stop talking even though the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)
     * AudioReceiveHandler.handleUserAudio(UserAudio)} method was fired thousands of times over the course of the 3 minutes.
     *
     * @param  user
     *         Never-null {@link net.dv8tion.jda.core.entities.User User} who's talking status has changed.
     * @param  speaking
     *         If true, the user has begun transmitting audio.
     * @param  soundshare
     *         If true, the user is using soundshare
     */
    default void onUserSpeaking(User user, boolean speaking, boolean soundshare) {}
}

/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.core.entities.User;

public interface ConnectionListener
{
    void onPing(long ping);
    void onStatusChange(ConnectionStatus status);

    /**
     * This method is an easy way to detect if a user is talking. Discord sends us an event when a user starts or stops
     * talking and it is parallel to the audio socket, so this event could come milliseconds before or after audio begins
     * or stops. This method is brilliant for clients wanting to display that a user is currently talking.<p>
     *
     * Unlike the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleCombinedAudio(net.dv8tion.jda.core.audio.CombinedAudio)}
     * and {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)} methods which are
     * fired extremely often, this method is fired as a flag for the beginning and ending of audio transmission, and as such
     * is only fired when that changes. So while the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)}
     * method is fired every time JDA receives audio data from Discord, this is only fired when that stream starts and when it stops.
     * If the user speaks for 3 minutes straight without ever stopping, then this would fire 2 times, once at the beginning
     * and once after 3 minutes when they stop talking even though the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#handleUserAudio(net.dv8tion.jda.core.audio.UserAudio)}
     * method was fired thousands of times over the course of the 3 minutes.
     *
     * @param user
     *          Never-null {@link net.dv8tion.jda.core.entities.User User} who's talking status has changed.
     * @param speaking
     *          If true, the user has begun transmitting audio.
     */
    void onUserSpeaking(User user, boolean speaking);
}

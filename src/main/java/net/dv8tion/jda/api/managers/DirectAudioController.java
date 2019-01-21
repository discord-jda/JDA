/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 * Direct access to internal gateway communication.
 * <br>This should only be used if a {@link net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor VoiceDispatchInterceptor} has been provided.
 *
 * <p>For normal operation use {@link Guild#getAudioManager()} instead.
 */
public interface DirectAudioController
{
    /**
     * The associated JDA instance
     *
     * @return The JDA instance
     */
    JDA getJDA();

    /**
     * Requests a voice server endpoint for connecting to the voice gateway.
     *
     * @param channel
     *        The channel to connect to
     *
     * @see   #reconnect(VoiceChannel)
     */
    void connect(VoiceChannel channel);

    /**
     * Requests to terminate the connection to a voice channel.
     *
     * @param guild
     *        The guild we were connected to
     *
     * @see   #reconnect(VoiceChannel)
     */
    void disconnect(Guild guild);

    /**
     * Requests to reconnect to the voice channel in the target guild.
     *
     * @param channel
     *        The channel we were connected to
     */
    void reconnect(VoiceChannel channel);

    /**
     * Used to update the internal state of the voice request. When a connection
     * was successfully established JDA will stop sending requests for the initial connect.
     * <br>This is done to retry the voice updates in case of a partial service failure.
     *
     * <p>Should be called when:
     * <ol>
     *     <li>Receiving a Voice State Update for the current account and we were previously connected (moved or disconnected)</li>
     *     <li>Receiving a Voice Server Update (initial connect or region change)</li>
     * </ol>
     *
     * Note that the voice state update will always be received prior to a voice server update.
     * <br>THe internal dispatch handlers already call this when needed, a library end-user never needs to call this method.
     *
     * @param guild
     *        The guild to update the state for
     * @param channel
     *        The new channel, or null to signal disconnect
     */
    void update(Guild guild, VoiceChannel channel);
}

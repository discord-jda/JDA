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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nonnull;

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
    @Nonnull
    JDA getJDA();

    /**
     * Requests a voice server endpoint for connecting to the voice gateway.
     *
     * @param channel
     *        The channel to connect to
     *
     * @see   #reconnect(AudioChannel)
     */
    void connect(@Nonnull AudioChannel channel);

    /**
     * Requests to terminate the connection to a voice channel.
     *
     * @param guild
     *        The guild we were connected to
     *
     * @see   #reconnect(AudioChannel)
     */
    void disconnect(@Nonnull Guild guild);

    /**
     * Requests to reconnect to the voice channel in the target guild.
     *
     * @param channel
     *        The channel we were connected to
     */
    void reconnect(@Nonnull AudioChannel channel);
}

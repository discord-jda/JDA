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

package net.dv8tion.jda.api.audio.dave;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

import javax.annotation.Nonnull;

/**
 * Factory for {@link DaveSession DaveSessions}.
 *
 * <p>All audio/video connections on Discord use the "Discord Audio &amp; Video Encryption" (DAVE) protocol.
 * By default, JDA does not implement this and falls back to "passthrough mode", which only does transport encryption through AEAD.
 *
 * <p>To use audio connections with JDA (such as {@link AudioManager#openAudioConnection(AudioChannel)}),
 * you must provide an implementation of this factory
 * either in {@link JDABuilder#setAudioModuleConfig(AudioModuleConfig)} or {@link DefaultShardManagerBuilder#setAudioModuleConfig(AudioModuleConfig)}.
 */
public interface DaveSessionFactory {
    /**
     * Create a new DAVE session.
     *
     * <p>The session should not yet be started.
     * JDA will invoke {@link DaveSession#initialize()} once a connection to the voice gateway is established.
     *
     * <p>The dave session should be started/created in {@link DaveSession#onSelectProtocolAck(int)},
     * which will provide the target protocol version.
     *
     * @param callbacks
     *        The {@link DaveProtocolCallbacks callbacks} to facilitate the protocol communication
     * @param userId
     *        The id of the connecting bot (self user)
     * @param channelId
     *        The id of the channel or group of the connection (audio channel)
     *
     * @return {@link DaveSession}
     */
    @Nonnull
    DaveSession createDaveSession(@Nonnull DaveProtocolCallbacks callbacks, long userId, long channelId);
}

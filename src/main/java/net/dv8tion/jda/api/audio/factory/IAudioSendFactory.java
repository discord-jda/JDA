/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.audio.factory;

import javax.annotation.Nonnull;

/**
 * Factory interface for the creation of new {@link net.dv8tion.jda.api.audio.factory.IAudioSendSystem IAudioSendSystem} objects.
 * <br>JDA, by default, uses {@link net.dv8tion.jda.api.audio.factory.DefaultSendFactory DefaultSendFactory} for the
 * creation of its UDP audio packet sending system.
 * <p>
 * Implementations of this interface are provided to
 * {@link net.dv8tion.jda.api.JDABuilder#setAudioSendFactory(IAudioSendFactory) JDABuilder.setAudioSendFactory(IAudioSendFactory)}.
 */
public interface IAudioSendFactory
{
    /**
     * Called by JDA's audio system when a new {@link net.dv8tion.jda.api.audio.factory.IAudioSendSystem IAudioSendSystem}
     * instance is needed to handle the sending of UDP audio packets to discord.
     *
     * @param  packetProvider
     *         The connection provided to the new {@link net.dv8tion.jda.api.audio.factory.IAudioSendSystem IAudioSendSystem}
     *         object for proper setup and usage.
     *
     * @return The newly constructed IAudioSendSystem, ready for {@link IAudioSendSystem#start()} to be called.
     */
    @Nonnull
    IAudioSendSystem createSendSystem(@Nonnull IPacketProvider packetProvider);
}

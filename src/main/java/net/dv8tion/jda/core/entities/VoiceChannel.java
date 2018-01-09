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
package net.dv8tion.jda.core.entities;

/**
 * Represents a Discord Voice Channel. A specification of {@link net.dv8tion.jda.core.entities.AudioChannel}.
 * <br>As a VoiceChannel has no extra functionality as its {@link net.dv8tion.jda.core.entities.Channel Channel} parent,
 * this interface is empty.
 * This interface only exists to distinct {@link net.dv8tion.jda.core.entities.Channel Channels} into
 * VoiceChannels and {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}.
 */
public interface VoiceChannel extends Channel, AudioChannel, Comparable<VoiceChannel>
{
    /**
     * The maximum amount of {@link net.dv8tion.jda.core.entities.Member Members} that can be in this
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} at once.
     * <br>0 - No limit
     *
     * @return The maximum amount of members allowed in this channel at once.
     */
    int getUserLimit();

    /**
     * The audio bitrate of the voice audio that is transmitted in this channel. While higher bitrates can be sent to
     * this channel, it will be scaled down by the client.
     * <br>Default and recommended value is 64000
     *
     * @return The audio bitrate of this voice channel.
     */
    int getBitrate();
}

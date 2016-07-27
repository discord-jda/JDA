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
package net.dv8tion.jda.entities;

/**
 * Represents a Discord Voice Channel.
 * As a VoiceChannel has no extra functionality as its {@link net.dv8tion.jda.entities.Channel Channel} parent,
 * this interface is empty.
 * This interface only exists to distinct {@link net.dv8tion.jda.entities.Channel Channels} into
 * VoiceChannels and {@link net.dv8tion.jda.entities.TextChannel TextChannels}.
 */
public interface VoiceChannel extends Channel, Comparable<VoiceChannel>
{
    /**
     * The maximum amount of {@link net.dv8tion.jda.entities.User Users} that can be in this
     * {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} at one time.
     * <br>
     * 0 - No limit <br>
     *
     * @return
     *      The maximum amount of users allowed in this channel at one time.
     */
    int getUserLimit();

    /**
     * The audio bitrate of the voice audio that is played in this channel. While higher bitrates can be sent to
     * this channel, it will be scaled down by the client.
     * <br>
     * Default and recommended value is 64000
     *
     * @return
     *      The audio bitrate of this voice channel.
     */
    int getBitrate();
}

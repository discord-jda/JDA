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
package net.dv8tion.jda.api.entities.channel.middleman

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import javax.annotation.Nonnull

/**
 * Represents a Guild Channel that is capable of handling audio.
 * <br></br>This is a [StandardGuildChannel] that contains additional methods present for audio channels
 *
 * @see VoiceChannel
 *
 * @see StageChannel
 *
 *
 * @see Guild.getVoiceChannelCache
 * @see Guild.getVoiceChannels
 * @see Guild.getVoiceChannelsByName
 * @see Guild.getVoiceChannelById
 * @see Guild.getStageChannelCache
 * @see Guild.getStageChannels
 * @see Guild.getStageChannelsByName
 * @see Guild.getStageChannelById
 * @see JDA.getVoiceChannelById
 * @see JDA.getStageChannelById
 */
interface AudioChannel : StandardGuildChannel {
    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?

    /**
     * The audio bitrate of the voice audio that is transmitted in this channel. While higher bitrates can be sent to
     * this channel, it will be scaled down by the client.
     * <br></br>Default and recommended value is 64000
     *
     * @return The audio bitrate of this audio channel.
     */
    @JvmField
    val bitrate: Int

    /**
     * The maximum amount of [Members][net.dv8tion.jda.api.entities.Member] that be in an audio connection within this channel concurrently.
     * <br></br>Returns 0 if there is no limit.
     *
     *
     * Moderators with the [VOICE_MOVE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MOVE_OTHERS] permission can bypass this limit.
     *
     * @return The maximum connections allowed in this channel concurrently
     */
    @JvmField
    val userLimit: Int

    @get:Nonnull
    val region: Region?
        /**
         * The [Region] of this channel.
         * <br></br>This will return [Region.AUTOMATIC] if the region of this channel is set to Automatic.
         *
         * @return the [Region] of this channel.
         */
        get() = if (regionRaw == null) Region.AUTOMATIC else Region.fromKey(
            regionRaw
        )

    /**
     * The raw region name for this channel.
     * <br></br>This will return null if the region is set to Automatic.
     *
     * @return Raw region name, or `null` if the region is set to automatic.
     */
    @JvmField
    val regionRaw: String?
}

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
package net.dv8tion.jda.internal.audio

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.internal.utils.EntityString

class ConnectionRequest {
    val guildIdLong: Long
    @JvmField
    var nextAttemptEpoch: Long = 0
    @JvmField
    var stage: ConnectionStage
    var channelId: Long = 0
        protected set

    constructor(guild: Guild) {
        stage = ConnectionStage.DISCONNECT
        guildIdLong = guild.idLong
    }

    constructor(channel: AudioChannel, stage: ConnectionStage) {
        channelId = channel.idLong
        guildIdLong = channel.guild.getIdLong()
        this.stage = stage
        nextAttemptEpoch = System.currentTimeMillis()
    }

    fun setChannel(channel: AudioChannel) {
        channelId = channel.idLong
    }

    fun getChannel(api: JDA): AudioChannel? {
        return api.getGuildChannelById(channelId) as AudioChannel?
    }

    public override fun toString(): String {
        return EntityString(this)
            .setType(stage)
            .addMetadata("guildId", java.lang.Long.toUnsignedString(guildIdLong))
            .addMetadata("channelId", java.lang.Long.toUnsignedString(channelId))
            .toString()
    }
}

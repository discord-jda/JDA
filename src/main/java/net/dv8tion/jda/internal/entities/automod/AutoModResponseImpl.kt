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
package net.dv8tion.jda.internal.entities.automod

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.automod.AutoModResponse
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.DataObject.Companion.empty
import java.time.Duration
import javax.annotation.Nonnull

class AutoModResponseImpl : AutoModResponse {
    @get:Nonnull
    override val type: AutoModResponse.Type
    override val channel: GuildMessageChannel?
    override val customMessage: String?
    private override val timeoutDuration: Long

    constructor(type: AutoModResponse.Type) {
        this.type = type
        channel = null
        customMessage = null
        timeoutDuration = 0
    }

    constructor(type: AutoModResponse.Type, channel: GuildMessageChannel?) {
        this.type = type
        this.channel = channel
        customMessage = null
        timeoutDuration = 0
    }

    constructor(type: AutoModResponse.Type, customMessage: String?) {
        this.type = type
        this.customMessage = customMessage
        channel = null
        timeoutDuration = 0
    }

    constructor(type: AutoModResponse.Type, duration: Duration) {
        this.type = type
        timeoutDuration = duration.seconds
        customMessage = null
        channel = null
    }

    constructor(guild: Guild, json: DataObject) {
        type = AutoModResponse.Type.fromKey(json.getInt("type", -1))
        channel = guild.getChannelById(GuildMessageChannel::class.java, json.getUnsignedLong("channel_id", 0L))
        customMessage = json.getString("custom_message", null)
        timeoutDuration = json.getUnsignedLong("duration_seconds", 0L)
    }

    override fun getTimeoutDuration(): Duration? {
        return if (timeoutDuration == 0L) null else Duration.ofSeconds(timeoutDuration)
    }

    @Nonnull
    override fun toData(): DataObject {
        val action = empty()
        action.put("type", type.key)
        if (type == AutoModResponse.Type.BLOCK_MESSAGE && customMessage == null) return action
        val metadata = empty()
        if (customMessage != null) metadata.put("custom_message", customMessage)
        if (channel != null) metadata.put("channel_id", channel.id)
        if (timeoutDuration > 0) metadata.put("duration_seconds", timeoutDuration)
        action.put("metadata", metadata)
        return action
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj !is AutoModResponseImpl) return false
        return type == obj.type
    }
}

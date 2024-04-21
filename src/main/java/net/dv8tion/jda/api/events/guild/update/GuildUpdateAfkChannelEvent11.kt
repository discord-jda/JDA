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
package net.dv8tion.jda.api.events.guild.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import javax.annotation.Nonnull

/**
 * Indicates that the afk-channel of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when an afk channel changes and retrieve the old one
 *
 *
 * Identifier: `afk_channel`
 */
class GuildUpdateAfkChannelEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull guild: Guild,
    oldAfkChannel: VoiceChannel?
) : GenericGuildUpdateEvent<VoiceChannel?>(api, responseNumber, guild, oldAfkChannel, guild.afkChannel, IDENTIFIER) {
    val oldAfkChannel: VoiceChannel?
        /**
         * The old afk channel
         *
         * @return The old afk channel, or null
         */
        get() = oldValue
    val newAfkChannel: VoiceChannel?
        /**
         * The new afk channel
         *
         * @return The new afk channel, or null
         */
        get() = newValue

    companion object {
        const val IDENTIFIER = "afk_channel"
    }
}

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
package net.dv8tion.jda.api.events.guild.voice

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.events.UpdateEvent
import javax.annotation.Nonnull

/**
 * Indicates that a [Member][net.dv8tion.jda.api.entities.Member] joined or left an [AudioChannel].
 *
 * Can be used to detect when a Member leaves/joins an AudioChannel.
 *
 *
 * **Example**<br></br>
 * <pre>`AudioChannelUnion joinedChannel = event.getChannelJoined();
 * AudioChannelUnion leftChannel = event.getChannelLeft();
 *
 * if (joinedChannel != null) {
 * // the member joined an audio channel
 * }
 * if (leftChannel != null) {
 * // the member left an audio channel
 * }
 * if (joinedChannel != null && leftChannel != null) {
 * // the member moved between two audio channels in the same guild
 * }
`</pre> *
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [VOICE_STATE][net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE] CacheFlag to be enabled, which requires
 * the [GUILD_VOICE_STATES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES] intent.
 *
 * <br></br>[createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disables that CacheFlag by default!
 *
 *
 * Additionally, this event requires the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 *
 *
 * Identifier: `audio-channel`
 */
class GuildVoiceUpdateEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull member: Member,
    override val oldValue: AudioChannel?
) : GenericGuildVoiceEvent(api, responseNumber, member), UpdateEvent<Member, AudioChannel?> {
    override val newValue: AudioChannel?

    init {
        newValue = member.voiceState!!.channel
    }

    val channelLeft: AudioChannelUnion?
        /**
         * The [AudioChannelUnion] that the [Member] is moved from
         *
         * @return The [AudioChannelUnion], or `null` if the member was not connected to a channel before
         */
        get() = oldValue as AudioChannelUnion?
    val channelJoined: AudioChannelUnion?
        /**
         * The [AudioChannelUnion] that was joined
         *
         * @return The [AudioChannelUnion], or `null` if the member has disconnected
         */
        get() = newValue as AudioChannelUnion?

    @get:Nonnull
    override val entity: E
        get() = member

    companion object {
        @get:Nonnull
        val propertyIdentifier = "audio-channel"
            get() = Companion.field
    }
}

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
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.requests.RestAction
import java.time.OffsetDateTime
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Indicates that a guild member has updated their [Request-to-Speak][GuildVoiceState.getRequestToSpeakTimestamp].
 *
 *
 * If [.getNewTime] is non-null, this means the member has *raised their hand* and wants to speak.
 * You can use [.approveSpeaker] or [.declineSpeaker] to handle this request if you have [Permission.VOICE_MUTE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MUTE_OTHERS].
 *
 *
 * **Requirements**<br></br>
 *
 *
 * These events require the [VOICE_STATE][net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE] CacheFlag to be enabled, which requires
 * the [GUILD_VOICE_STATES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES] intent.
 *
 * <br></br>[createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disables that CacheFlag by default!
 *
 *
 * Additionally, these events require the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire specific events like these we
 * need to have the old member cached to compare against.
 */
class GuildVoiceRequestToSpeakEvent(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull member: Member,
    /**
     * The old [GuildVoiceState.getRequestToSpeakTimestamp]
     *
     * @return The old timestamp, or null if this member did not request to speak before
     */
    val oldTime: OffsetDateTime?,
    /**
     * The new [GuildVoiceState.getRequestToSpeakTimestamp]
     *
     * @return The new timestamp, or null if the request to speak was declined or cancelled
     */
    val newTime: OffsetDateTime?
) : GenericGuildVoiceEvent(api, responseNumber, member) {

    /**
     * Promote the member to speaker.
     *
     * This requires a non-null [.getNewTime].
     * You can use [GuildVoiceState.inviteSpeaker] to invite the member to become a speaker if they haven't requested to speak.
     *
     *
     * This does nothing if the member is not connected to a [StageChannel].
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.VOICE_MUTE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MUTE_OTHERS]
     * in the associated [StageChannel]
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun approveSpeaker(): RestAction<Void?>? {
        return voiceState.approveSpeaker()
    }

    /**
     * Reject this members [request to speak][GuildVoiceState.getRequestToSpeakTimestamp].
     *
     * This requires a non-null [.getNewTime].
     * The member will have to request to speak again.
     *
     *
     * This does nothing if the member is not connected to a [StageChannel].
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.VOICE_MUTE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MUTE_OTHERS]
     * in the associated [StageChannel]
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun declineSpeaker(): RestAction<Void?>? {
        return voiceState.declineSpeaker()
    }
}

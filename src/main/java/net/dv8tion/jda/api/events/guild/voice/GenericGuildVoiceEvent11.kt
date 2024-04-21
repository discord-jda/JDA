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
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import javax.annotation.Nonnull

/**
 * Indicates that a [Guild][net.dv8tion.jda.api.entities.Guild] voice event is fired.
 * <br></br>Every GuildVoiceEvent is an instance of this event and can be casted.
 *
 *
 * Can be used to detect any GuildVoiceEvent.
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
abstract class GenericGuildVoiceEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The affected [Member][net.dv8tion.jda.api.entities.Member]
     *
     * @return The affected Member
     */
    @get:Nonnull
    @param:Nonnull val member: Member
) : GenericGuildEvent(api, responseNumber, member.guild) {

    @get:Nonnull
    val voiceState: GuildVoiceState?
        /**
         * The [GuildVoiceState][net.dv8tion.jda.api.entities.GuildVoiceState] of the member
         * <br></br>Shortcut for `getMember().getVoiceState()`
         *
         * @return The [GuildVoiceState][net.dv8tion.jda.api.entities.GuildVoiceState] of the member
         */
        get() = member.voiceState
}

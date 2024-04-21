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
import net.dv8tion.jda.api.entities.Member
import javax.annotation.Nonnull

/**
 * Indicates that a [Member][net.dv8tion.jda.api.entities.Member] (un-)deafened itself.
 *
 *
 * Can be used to detect when a member deafens or un-deafens itself.
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
 */
class GuildVoiceSelfDeafenEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull member: Member) :
    GenericGuildVoiceEvent(api, responseNumber, member) {
    /**
     * Whether the member deafened itself in this event
     *
     * @return True, if the member deafened itself,
     * <br></br>False, if the member un-deafened itself
     */
    val isSelfDeafened: Boolean

    init {
        isSelfDeafened = member.voiceState!!.isSelfDeafened
    }
}

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
package net.dv8tion.jda.api.events.guild.member.update

import net.dv8tion.jda.annotations.Incubating
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import javax.annotation.Nonnull

/**
 * Indicates that a [Member] has agreed to Membership Screening requirements.
 *
 *
 * Can be used to retrieve members who have agreed to Membership Screening requirements and the triggering guild.
 *
 *
 * Identifier: `pending`
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 *
 *
 * Additionally, this event also requires the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 *
 * @incubating Discord is still trying to figure this out
 *
 * @since  4.2.1
 */
@Incubating
class GuildMemberUpdatePendingEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull member: Member,
    previous: Boolean
) : GenericGuildMemberUpdateEvent<Boolean?>(api, responseNumber, member, previous, member.isPending, IDENTIFIER) {
    val oldPending: Boolean
        /**
         * The old pending status
         *
         * @return The old pending status
         */
        get() = oldValue
    val newPending: Boolean
        /**
         * The new pending status
         *
         * @return The new pending status
         */
        get() = newValue

    companion object {
        const val IDENTIFIER = "pending"
    }
}

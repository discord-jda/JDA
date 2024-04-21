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
package net.dv8tion.jda.api.events.guild.member

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import javax.annotation.Nonnull

/**
 * Indicates that a [Guild][net.dv8tion.jda.api.entities.Guild] member event is fired.
 * <br></br>Every GuildMemberEvent is an instance of this event and can be casted.
 *
 *
 * Most of these events require the [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 *
 *
 * Can be used to detect any GuildMemberEvent.
 */
abstract class GenericGuildMemberEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The [Member][net.dv8tion.jda.api.entities.Member] instance
     *
     * @return The Member instance
     */
    @get:Nonnull
    @param:Nonnull val member: Member
) : GenericGuildEvent(api, responseNumber, member.guild) {

    @get:Nonnull
    val user: User
        /**
         * The [User][net.dv8tion.jda.api.entities.User] instance
         * <br></br>Shortcut for `getMember().getUser()`
         *
         * @return The User instance
         */
        get() = member.user
}

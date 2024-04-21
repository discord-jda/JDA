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
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import javax.annotation.Nonnull

/**
 * Indicates that a user was removed from a [Guild]. This includes kicks, bans, and leaves respectively.
 * <br></br>This can be fired for uncached members and cached members alike.
 * If the member was not cached by JDA, due to the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * or disabled member chunking, then [.getMember] will return `null`.
 *
 *
 * Can be used to detect when a member is removed from a guild, either by leaving or being kicked/banned.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 */
class GuildMemberRemoveEvent(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild?,
    /**
     * The corresponding user who was removed from the guild.
     *
     * @return The user who was removed
     */
    @get:Nonnull
    @param:Nonnull val user: User,
    /**
     * The member instance for this user, if it was cached at the time.
     * <br></br>Discord does not provide the member meta-data when a remove event is dispatched.
     *
     * @return Possibly-null member
     */
    val member: Member?
) : GenericGuildEvent(api, responseNumber, guild)

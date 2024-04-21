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
import javax.annotation.Nonnull

/**
 * Fired for every [Member] update, regardless of cache.
 *
 *
 * This is a watered-down version of the [GenericGuildMemberUpdateEvent][net.dv8tion.jda.api.events.guild.member.update.GenericGuildMemberUpdateEvent]
 * which only provides the updated member instance. This is useful when JDA cannot fire specific update events when the member is uncached.
 *
 *
 * You can use this to do stateless checks on member instances to update database entries or check for special roles.
 * Note that the member might not be in cache when this event is fired due to the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy].
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 *
 * @since  4.2.1
 */
class GuildMemberUpdateEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull member: Member) :
    GenericGuildMemberEvent(api, responseNumber, member)

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
package net.dv8tion.jda.api.events.guild

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import javax.annotation.Nonnull

/**
 * Indicates that a [User][net.dv8tion.jda.api.entities.User] was unbanned from a [Guild][net.dv8tion.jda.api.entities.Guild].
 *
 *
 * Can be used to retrieve the user who was unbanned (if available) and the guild which they were unbanned from.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_MODERATION][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MODERATION] intent to be enabled.
 */
class GuildUnbanEvent(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild?,
    /**
     * The [User][net.dv8tion.jda.api.entities.User] who was unbanned
     *
     * @return The unbanned user
     */
    @get:Nonnull
    @param:Nonnull val user: User
) : GenericGuildEvent(api, responseNumber, guild)

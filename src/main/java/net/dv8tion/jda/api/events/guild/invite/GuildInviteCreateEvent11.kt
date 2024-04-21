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
package net.dv8tion.jda.api.events.guild.invite

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import javax.annotation.Nonnull

/**
 * Indicates that an [Invite] was created in a [Guild][net.dv8tion.jda.api.entities.Invite.Guild].
 *
 *
 * Can be used to track invites for moderation purposes.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_INVITES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_INVITES] intent to be enabled.
 * <br></br>This event will only fire for invites created in channels where you can [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL].
 */
class GuildInviteCreateEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The invite which was created.
     *
     * @return [Invite]
     */
    @get:Nonnull
    @param:Nonnull val invite: Invite, @Nonnull channel: GuildChannel
) : GenericGuildInviteEvent(api, responseNumber, invite.code, channel)

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
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import javax.annotation.Nonnull

/**
 * Indicates that an [Invite] was created or deleted in a [Guild].
 * <br></br>Every GuildInviteEvent is derived from this event and can be casted.
 *
 *
 * Can be used to detect any GuildInviteEvent.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * These events require the [GUILD_INVITES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_INVITES] intent to be enabled.
 * <br></br>These events will only fire for invite events that occur in channels where you can [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL].
 */
open class GenericGuildInviteEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The invite code.
     * <br></br>This can be converted to a url with `discord.gg/<code>`.
     *
     * @return The invite code
     */
    @get:Nonnull
    @param:Nonnull val code: String, @param:Nonnull private val channel: GuildChannel
) : GenericGuildEvent(api, responseNumber, channel.guild) {
    @get:Nonnull
    val url: String
        /**
         * The invite url.
         * <br></br>This uses the `https://discord.gg/<code>` format.
         *
         * @return The invite url
         */
        get() = "https://discord.gg/$code"

    /**
     * The [GuildChannel] this invite points to.
     *
     * @return [GuildChannel]
     */
    @Nonnull
    fun getChannel(): GuildChannelUnion {
        return channel as GuildChannelUnion
    }

    @get:Nonnull
    val channelType: ChannelType?
        /**
         * The [ChannelType] for of the [channel][.getChannel] this invite points to.
         *
         * @return [ChannelType]
         */
        get() = channel.type
}

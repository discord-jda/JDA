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
package net.dv8tion.jda.api.events.user

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import java.time.OffsetDateTime
import javax.annotation.Nonnull

/**
 * Indicates that a [User][net.dv8tion.jda.api.entities.User] started typing. (Similar to the typing indicator in the Discord client)
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_MESSAGE_TYPING][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_TYPING] intent to be enabled to fire
 * for guild channels, and [DIRECT_MESSAGE_TYPING][net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGE_TYPING] to fire for private channels.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable these by default!
 *
 *
 * Can be used to retrieve the User who started typing and when and in which MessageChannel they started typing.
 */
class UserTypingEvent(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull user: User?, @param:Nonnull private val channel: MessageChannel,
    /**
     * The time when the user started typing
     *
     * @return The time when the typing started
     */
    @get:Nonnull
    @param:Nonnull val timestamp: OffsetDateTime,
    /**
     * [Member][net.dv8tion.jda.api.entities.Member] instance for the User, or null if this was not in a Guild.
     *
     * @return Possibly-null [Member][net.dv8tion.jda.api.entities.Member]
     */
    val member: Member?
) : GenericUserEvent(api, responseNumber, user) {

    /**
     * The channel where the typing was started
     *
     * @return The channel
     */
    @Nonnull
    fun getChannel(): MessageChannelUnion {
        return channel as MessageChannelUnion
    }

    /**
     * Whether the user started typing in a channel of the specified type.
     *
     * @param  type
     * [ChannelType]
     *
     * @return True, if the user started typing in a channel of the specified type
     */
    fun isFromType(@Nonnull type: ChannelType): Boolean {
        return channel.type === type
    }

    @get:Nonnull
    val type: ChannelType?
        /**
         * The [ChannelType]
         *
         * @return The [ChannelType]
         */
        get() = channel.type
    val guild: Guild?
        /**
         * [Guild][net.dv8tion.jda.api.entities.Guild] in which this users started typing,
         * or `null` if this was not in a Guild.
         *
         * @return Possibly-null [Guild][net.dv8tion.jda.api.entities.Guild]
         */
        get() = if (type!!.isGuild) member!!.guild else null
}

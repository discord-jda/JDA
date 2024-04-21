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
package net.dv8tion.jda.api.exceptions

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Indicates that the user is missing a [Permission] for some action.
 *
 * @see net.dv8tion.jda.api.entities.IPermissionHolder.hasPermission
 * @see net.dv8tion.jda.api.entities.IPermissionHolder.hasPermission
 */
open class InsufficientPermissionException : PermissionException {
    /**
     * The id for the responsible [net.dv8tion.jda.api.entities.Guild] instance.
     *
     * @return The ID as a long
     *
     * @since  4.0.0
     *
     * @see net.dv8tion.jda.api.JDA.getGuildById
     */
    val guildId: Long

    /**
     * The id for the responsible [GuildChannel] instance.
     *
     * @return The ID as a long or 0
     *
     * @since  4.0.0
     *
     * @see .getChannel
     */
    val channelId: Long

    /**
     * The [ChannelType] for the [channel id][.getChannelId].
     *
     * @return The channel type or [ChannelType.UNKNOWN].
     *
     * @since  4.0.0
     */
    @get:Nonnull
    val channelType: ChannelType

    constructor(@Nonnull guild: Guild, @Nonnull permission: Permission) : this(guild, null, permission)
    constructor(@Nonnull guild: Guild, @Nonnull permission: Permission, @Nonnull reason: String) : this(
        guild,
        null,
        permission,
        reason
    )

    constructor(@Nonnull channel: GuildChannel, @Nonnull permission: Permission) : this(
        channel.guild,
        channel,
        permission
    )

    constructor(
        @Nonnull channel: GuildChannel,
        @Nonnull permission: Permission,
        @Nonnull reason: String
    ) : this(channel.guild, channel, permission, reason)

    private constructor(@Nonnull guild: Guild, channel: GuildChannel?, @Nonnull permission: Permission) : super(
        permission,
        "Cannot perform action due to a lack of Permission. Missing permission: $permission"
    ) {
        guildId = guild.idLong
        channelId = channel?.idLong ?: 0
        channelType = if (channel == null) ChannelType.UNKNOWN else channel.type!!
    }

    private constructor(
        @Nonnull guild: Guild,
        channel: GuildChannel?,
        @Nonnull permission: Permission,
        @Nonnull reason: String
    ) : super(permission, reason) {
        guildId = guild.idLong
        channelId = channel?.idLong ?: 0
        channelType = if (channel == null) ChannelType.UNKNOWN else channel.type!!
    }

    /**
     * The [net.dv8tion.jda.api.entities.Guild] instance for the [guild id][.getGuildId].
     *
     * @param  api
     * The shard to perform the lookup in
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided JDA instance is null
     *
     * @since  4.0.0
     *
     * @return The Guild instance or null
     */
    fun getGuild(@Nonnull api: JDA): Guild? {
        Checks.notNull(api, "JDA")
        return api.getGuildById(guildId)
    }

    /**
     * The [GuildChannel] instance for the [channel id][.getChannelId].
     *
     * @param  api
     * The shard to perform the lookup in
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided JDA instance is null
     *
     * @since  4.0.0
     *
     * @return The GuildChannel instance or null
     */
    fun getChannel(@Nonnull api: JDA): GuildChannel? {
        Checks.notNull(api, "JDA")
        return api.getGuildChannelById(channelType, channelId)
    }
}

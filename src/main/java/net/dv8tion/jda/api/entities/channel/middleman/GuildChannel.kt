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
package net.dv8tion.jda.api.entities.channel.middleman

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.internal.utils.Helpers
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a [Guild][net.dv8tion.jda.api.entities.Guild] channel.
 *
 * @see Guild.getGuildChannelById
 * @see Guild.getGuildChannelById
 * @see JDA.getGuildChannelById
 * @see JDA.getGuildChannelById
 */
interface GuildChannel : Channel, Comparable<GuildChannel?> {
    @JvmField
    @get:Nonnull
    val guild: Guild

    @JvmField
    @get:Nonnull
    val manager: ChannelManager<*, *>?

    /**
     * Deletes this GuildChannel.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If this channel was already deleted
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The send request was attempted after the account lost
     * [Permission.MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] in the channel.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>If we were removed from the Guild
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * if the currently logged in account doesn't have [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL]
     * for the channel.
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    override fun delete(): AuditableRestAction<Void?>?

    @JvmField
    @get:Nonnull
    val permissionContainer: IPermissionContainer?

    @get:Nonnull
    val jumpUrl: String?
        /**
         * Returns the jump-to URL for this channel. Clicking this URL in the Discord client will cause the client to
         * jump to the specified channel.
         *
         * @return A String representing the jump-to URL for the channel.
         */
        get() = Helpers.format(JUMP_URL, guild.id, id)

    companion object {
        /** Template for [.getJumpUrl]. */
        const val JUMP_URL = "https://discord.com/channels/%s/%s"
    }
}

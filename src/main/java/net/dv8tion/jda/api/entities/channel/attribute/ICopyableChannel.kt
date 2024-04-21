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
package net.dv8tion.jda.api.entities.channel.attribute

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a GuildChannel that is capable of being copied.
 *
 *
 * Please see [ICopyableChannel.createCopy] for information on what is copied.
 */
interface ICopyableChannel : GuildChannel {
    /**
     * Creates a copy of the specified [GuildChannel]
     * in the specified [Guild][net.dv8tion.jda.api.entities.Guild].
     * <br></br>If the provided target guild is not the same Guild this channel is in then
     * the parent category and permissions will not be copied due to technical difficulty and ambiguity.
     *
     *
     * This copies the following elements:
     *
     *  1. Name
     *  1. Parent Category (if present)
     *  1. Voice Elements (Bitrate, Userlimit)
     *  1. Text Elements (Topic, NSFW, Slowmode)
     *  1. All permission overrides for Members/Roles
     *
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission was removed
     *
     *
     * @param  guild
     * The [Guild][net.dv8tion.jda.api.entities.Guild] to create the channel in
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided guild is `null`
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     * If the currently logged in account does not have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission
     *
     * @return A specific [ChannelAction]
     * <br></br>This action allows to set fields for the new GuildChannel before creating it!
     */
    @Nonnull
    @CheckReturnValue
    fun createCopy(@Nonnull guild: Guild?): ChannelAction<out ICopyableChannel?>?

    /**
     * Creates a copy of the specified [GuildChannel].
     *
     *
     * This copies the following elements:
     *
     *  1. Name
     *  1. Parent Category (if present)
     *  1. Voice Elements (Bitrate, Userlimit)
     *  1. Text Elements (Topic, NSFW, Slowmode)
     *  1. All permission overrides for Members/Roles
     *
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission was removed
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     * If the currently logged in account does not have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission
     *
     * @return A specific [ChannelAction]
     * <br></br>This action allows to set fields for the new GuildChannel before creating it!
     */
    @Nonnull
    @CheckReturnValue
    fun createCopy(): ChannelAction<out ICopyableChannel?>?
}

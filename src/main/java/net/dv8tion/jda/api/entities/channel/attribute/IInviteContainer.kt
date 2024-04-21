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

import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.InviteAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a [GuildChannel] that can be the target of a Guild's invite.
 *
 *
 * Invites have to be targeted at exactly one [IInviteContainer], which will open when the invite is used (unless restricted by permissions).
 */
interface IInviteContainer : GuildChannel {
    /**
     * Creates a new [InviteAction] which can be used to create a
     * new [Invite][net.dv8tion.jda.api.entities.Invite].
     * <br></br>Requires [CREATE_INSTANT_INVITE][net.dv8tion.jda.api.Permission.CREATE_INSTANT_INVITE] in this channel.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the account does not have [CREATE_INSTANT_INVITE][net.dv8tion.jda.api.Permission.CREATE_INSTANT_INVITE] in this channel
     * @throws java.lang.IllegalArgumentException
     * If this is an instance of a [Category]
     *
     * @return A new [InviteAction]
     *
     * @see InviteAction
     */
    @Nonnull
    @CheckReturnValue
    fun createInvite(): InviteAction?

    /**
     * Returns all invites for this channel.
     * <br></br>Requires [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] in this channel.
     * Will throw an [InsufficientPermissionException][net.dv8tion.jda.api.exceptions.InsufficientPermissionException] otherwise.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * if the account does not have [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] in this channel
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: List&lt;[Invite][net.dv8tion.jda.api.entities.Invite]&gt;
     * <br></br>The list of expanded Invite objects
     *
     * @see net.dv8tion.jda.api.entities.Guild.retrieveInvites
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveInvites(): RestAction<List<Invite?>?>?
}

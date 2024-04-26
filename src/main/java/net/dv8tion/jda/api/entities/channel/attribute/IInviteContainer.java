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

package net.dv8tion.jda.api.entities.channel.attribute;

import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.InviteAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Represents a {@link GuildChannel GuildChannel} that can be the target of a Guild's invite.
 *
 * <p>Invites have to be targeted at exactly one {@link IInviteContainer}, which will open when the invite is used (unless restricted by permissions).
 */
public interface IInviteContainer extends GuildChannel
{
    /**
     * Creates a new {@link InviteAction InviteAction} which can be used to create a
     * new {@link net.dv8tion.jda.api.entities.Invite Invite}.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#CREATE_INSTANT_INVITE CREATE_INSTANT_INVITE} in this channel.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have {@link net.dv8tion.jda.api.Permission#CREATE_INSTANT_INVITE CREATE_INSTANT_INVITE} in this channel
     * @throws java.lang.IllegalArgumentException
     *         If this is an instance of a {@link Category Category}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
     *
     * @return A new {@link InviteAction InviteAction}
     *
     * @see    InviteAction
     */
    @Nonnull
    @CheckReturnValue
    InviteAction createInvite();

    /**
     * Returns all invites for this channel.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in this channel.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in this channel
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.api.entities.Invite Invite}{@literal >}
     *         <br>The list of expanded Invite objects
     *
     * @see    net.dv8tion.jda.api.entities.Guild#retrieveInvites()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<Invite>> retrieveInvites();
}

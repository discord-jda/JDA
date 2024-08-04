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

package net.dv8tion.jda.api.entities.channel.middleman;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Represents a {@link net.dv8tion.jda.api.entities.Guild Guild} channel.
 *
 * @see Guild#getGuildChannelById(long)
 * @see Guild#getGuildChannelById(ChannelType, long)
 *
 * @see JDA#getGuildChannelById(long)
 * @see JDA#getGuildChannelById(ChannelType, long)
 */
public interface GuildChannel extends Channel, Comparable<GuildChannel>
{
    /** Template for {@link #getJumpUrl()}.*/
    String JUMP_URL = "https://discord.com/channels/%s/%s";

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Guild Guild} that this GuildChannel is part of.
     *
     * @return Never-null {@link net.dv8tion.jda.api.entities.Guild Guild} that this GuildChannel is part of.
     */
    @Nonnull
    Guild getGuild();

    /**
     * Returns the {@link ChannelManager ChannelManager} for this GuildChannel.
     * <br>In the ChannelManager, you can modify the name, topic and position of this GuildChannel.
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return The ChannelManager of this GuildChannel
     */
    @Nonnull
    ChannelManager<?, ?> getManager();

    /**
     * Deletes this GuildChannel.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL} in the channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the currently logged in account doesn't have {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     *         for the channel.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Override
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * The channel containing the permissions relevant to this channel.
     *
     * <p>This is usually the same channel, but for threads the parent channel is used instead.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return The permission container
     */
    @Nonnull
    IPermissionContainer getPermissionContainer();

    /**
     * Returns the jump-to URL for this channel. Clicking this URL in the Discord client will cause the client to
     * jump to the specified channel.
     *
     * @return A String representing the jump-to URL for the channel.
     */
    @Nonnull
    default String getJumpUrl()
    {
        return Helpers.format(JUMP_URL, getGuild().getId(), getId());
    }
}

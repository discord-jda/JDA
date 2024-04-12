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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;


/**
 * Represents a GuildChannel that is capable of being copied.
 *
 * <p>Please see {@link ICopyableChannel#createCopy()} for information on what is copied.
 */
public interface ICopyableChannel extends GuildChannel
{
    /**
     * Creates a copy of the specified {@link GuildChannel GuildChannel}
     * in the specified {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>If the provided target guild is not the same Guild this channel is in then
     * the parent category and permissions will not be copied due to technical difficulty and ambiguity.
     *
     * <p>This copies the following elements:
     * <ol>
     *     <li>Name</li>
     *     <li>Parent Category (if present)</li>
     *     <li>Voice Elements (Bitrate, Userlimit)</li>
     *     <li>Text Elements (Topic, NSFW, Slowmode)</li>
     *     <li>All permission overrides for Members/Roles</li>
     * </ol>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     * </ul>
     *
     * @param  guild
     *         The {@link net.dv8tion.jda.api.entities.Guild Guild} to create the channel in
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided guild is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If the currently logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild the channel should be copied to.
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new GuildChannel before creating it!
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<? extends ICopyableChannel> createCopy(@Nonnull Guild guild);

    /**
     * Creates a copy of the specified {@link GuildChannel GuildChannel}.
     *
     * <p>This copies the following elements:
     * <ol>
     *     <li>Name</li>
     *     <li>Parent Category (if present)</li>
     *     <li>Voice Elements (Bitrate, Userlimit)</li>
     *     <li>Text Elements (Topic, NSFW, Slowmode)</li>
     *     <li>All permission overrides for Members/Roles</li>
     * </ol>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If the currently logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new GuildChannel before creating it!
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<? extends ICopyableChannel> createCopy();
}

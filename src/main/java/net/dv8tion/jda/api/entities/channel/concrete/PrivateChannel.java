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
package net.dv8tion.jda.api.entities.channel.concrete;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the connection used for direct messaging.
 *
 * @see User#openPrivateChannel()
 */
public interface PrivateChannel extends MessageChannel
{
    /**
     * The {@link net.dv8tion.jda.api.entities.User User} that this {@link PrivateChannel PrivateChannel} communicates with.
     *
     * <p>This user is only null if this channel is currently uncached, and one the following occur:
     * <ul>
     *     <li>A reaction is removed</li>
     *     <li>A reaction is added</li>
     *     <li>A message is deleted</li>
     *     <li>This account sends a message to a user from another shard (not shard 0)</li>
     *     <li>This account receives an interaction response, happens when using an user-installed interaction</li>
     *     <li>This channel represents a DM channel between friends, happens when using an user-installed interaction</li>
     * </ul>
     * The consequence of this is that for any message this bot receives from a guild or from other users, the user will not be null.
     *
     * <br>In order to retrieve a user that is null, use {@link #retrieveUser()}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.User User}.
     *
     * @see #retrieveUser()
     */
    @Nullable
    User getUser();

    /**
     * Retrieves the {@link User User} that this {@link PrivateChannel PrivateChannel} communicates with.
     *
     * <br>This method fetches the channel from the API and retrieves the User from that.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if this channel is {@link Guild#isDetached() detached}, representing a friend DMs.
     *
     * @return A {@link RestAction RestAction} to retrieve the {@link User User} that this {@link PrivateChannel PrivateChannel} communicates with.
     */
    @Nonnull
    @CheckReturnValue
    RestAction<User> retrieveUser();

    /**
     * The human-readable name of this channel.
     *
     * <p>If getUser returns null, this method will return an empty String.
     * This happens when JDA does not have enough information to populate the channel name.
     *
     * <p>This will occur only when {@link #getUser()} is null, and the reasons are given in {@link #getUser()}
     *
     * <p>If the channel name is important, {@link #retrieveUser()} should be used, instead.
     *
     * @return The name of this channel
     *
     * @see #retrieveUser()
     * @see #getUser()
     */
    @Nonnull
    @Override
    String getName();
}

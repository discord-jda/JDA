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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Represents the connection used for direct messaging.
 *
 * @see User#openPrivateChannel()
 */
public interface PrivateChannel extends MessageChannel, IFakeable
{
    /**
     * The {@link net.dv8tion.jda.api.entities.User User} that this {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} communicates with.
     *
     * @return A non-null {@link net.dv8tion.jda.api.entities.User User}.
     */
    @Nonnull
    User getUser();

    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this PrivateChannel
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Closes a PrivateChannel. After being closed successfully the PrivateChannel is removed from the JDA mapping.
     * <br>As a note, this does not remove the history of the PrivateChannel. If the channel is reopened the history will
     * still be present.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Void
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> close();
}

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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.RestAction RestAction} specifically
 * designed to create a {@link ThreadChannel ThreadChannel}.
 * This extension allows setting properties before executing the action.
 *
 * @see    Message#createThreadChannel(String)
 * @see    net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer#createThreadChannel(String)
 * @see    net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer#createThreadChannel(String, boolean)
 * @see    net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer#createThreadChannel(String, long)
 * @see    net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer#createThreadChannel(String, String)
 */
public interface ThreadChannelAction extends AbstractThreadCreateAction<ThreadChannel, ThreadChannelAction>, FluentAuditableRestAction<ThreadChannel, ThreadChannelAction>
{
    /**
     * Sets whether this channel allows all members to add new members.
     * <br>When set to false, only moderators and the thread owner can add new members.
     *
     * @param  isInvitable
     *         True, if all members should be allowed to add new members
     *
     * @return The current ThreadChannelAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelAction setInvitable(boolean isInvitable);
}

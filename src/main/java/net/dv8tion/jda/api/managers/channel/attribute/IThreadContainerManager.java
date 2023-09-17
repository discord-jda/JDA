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

package net.dv8tion.jda.api.managers.channel.attribute;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.managers.channel.ChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Manager abstraction to configure settings related to thread channel containers, such as {@link ForumChannel}.
 *
 * @param <T> The channel type
 * @param <M> The manager type
 */
public interface IThreadContainerManager<T extends IThreadContainer, M extends IThreadContainerManager<T, M>> extends ChannelManager<T, M>
{
    /**
     * Sets the <b><u>default thread slowmode</u></b> of the selected channel.
     * This is applied to newly created threads by default.
     * <br>Provide {@code 0} to disable slowmode.
     *
     * <p>A channel default thread slowmode <b>must not</b> be negative nor greater than {@link ISlowmodeChannel#MAX_SLOWMODE}!
     *
     * <p>Note: Bots are unaffected by this.
     * <br>Having {@link Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @param  slowmode
     *         The new default thread slowmode (in seconds)
     *
     * @throws IllegalArgumentException
     *         If the provided slowmode is negative or greater than {@value ISlowmodeChannel#MAX_SLOWMODE}
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    IThreadContainer#getDefaultThreadSlowmode()
     */
    @Nonnull
    @CheckReturnValue
    M setDefaultThreadSlowmode(int slowmode);
}

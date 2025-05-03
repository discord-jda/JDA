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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Common features of all {@link RestAction RestActions} that create a new thread.
 *
 * @param <T>
 *        The success type given to the {@link #queue(Consumer, Consumer)} success consumer
 * @param <R>
 *        The common return type of setters, allowing for fluid interface design
 */
public interface AbstractThreadCreateAction<T, R extends AbstractThreadCreateAction<T, R>> extends RestAction<T>
{
    /**
     * The guild to create this {@link GuildChannel} for.
     *
     * @return The guild
     */
    @Nonnull
    Guild getGuild();

    /**
     * The {@link ChannelType} for the resulting channel.
     *
     * @return The channel type
     */
    @Nonnull
    ChannelType getType();

    /**
     * Sets the name for the new GuildChannel.
     *
     * @param  name
     *         The not-null name for the new GuildChannel (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the provided name is null, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return The current action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    R setName(@Nonnull String name);

    /**
     * Sets the {@link ThreadChannel.AutoArchiveDuration} for the new thread.
     * <br>This is primarily used to <em>hide</em> threads after the provided time of inactivity.
     * Threads are automatically archived after 7 days of inactivity regardless.
     *
     * @param  autoArchiveDuration
     *         The new archive inactivity duration (which hides the thread)
     *
     * @throws IllegalArgumentException
     *         If the provided duration is null
     *
     * @return The current action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    R setAutoArchiveDuration(@Nonnull ThreadChannel.AutoArchiveDuration autoArchiveDuration);

    /**
     * Sets the <b><u>slowmode</u></b> for the new thread.
     *
     * <p>A channel slowmode <b>must not</b> be negative nor greater than {@link ISlowmodeChannel#MAX_SLOWMODE}!
     *
     * <p>Note: Bots are unaffected by this.
     * <br>Having {@link Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @param  slowmode
     *         The new slowmode
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the bot does not have {@link Permission#MANAGE_THREADS} in the parent channel
     * @throws IllegalArgumentException
     *         If the provided slowmode is negative or greater than {@value ISlowmodeChannel#MAX_SLOWMODE}
     *
     * @return The current action, for chaining convenience
     *
     * @see net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel#getSlowmode()
     */
    @Nonnull
    @CheckReturnValue
    R setSlowmode(int slowmode);
}

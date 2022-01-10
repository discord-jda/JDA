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

package net.dv8tion.jda.api.managers.channel.concrete;

import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.managers.channel.ChannelManager;

/**
 * Manager providing functionality common for all {@link ThreadChannel ThreadChannels}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setSlowmode(10)
 *        .queue();
 * manager.reset(ChannelManager.SLOWMODE | ChannelManager.NAME)
 *        .setName("Javascript is to Java as car is to carpet")
 *        .setSlowmode(120)
 *        .queue();
 * }</pre>
 *
 * @see ThreadChannel#getManager()
 * @see ThreadChannel
 */
public interface ThreadChannelManager extends ChannelManager<ThreadChannel, ThreadChannelManager>
{

    /**
     * Sets the <b><u>slowmode</u></b> of the selected {@link ThreadChannel}.
     * <br>Provide {@code 0} to reset the slowmode of the {@link ThreadChannel}
     *
     * <p>A channel slowmode <b>must not</b> be negative nor greater than {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}!
     *
     * <p>Note: Bots are unaffected by this.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @see ThreadChannel#getSlowmode()
     *
     * @param  slowmode
     *         The new slowmode for the selected {@link ThreadChannel}
     *
     * @throws IllegalArgumentException
     *         If the provided slowmode is negative or greater than {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}
     *
     * @return this ThreadChannelManager for chaining convenience
     */
    ThreadChannelManager setSlowmode(int slowmode);

    /**
     * Sets the duration of the autoarchiving of this ThreadChannel.
     *
     * This is limited to the choices offered in {@link ThreadChannel.AutoArchiveDuration}
     *
     * @see ThreadChannel#getAutoArchiveDuration()
     *
     * @param  autoArchiveDuration
     *         The new duration before an inactive channel will be autoarchived.
     *
     * @throws IllegalArgumentException
     *         If the provided duration is not supported by this guild due to the guild boost requirements for higher durations.
     *
     * @return this ThreadChannelManager for chaining convenience.
     */
    ThreadChannelManager setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration autoArchiveDuration);

    /**
     * Sets the archived state of this ThreadChannel.
     *
     * @see ThreadChannel#isArchived()
     *
     * @param  archived
     *         The new archived state for the selected {@link ThreadChannel}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account is not the thread owner or does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the thread is locked (archived by a moderator) and the current account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *
     * @return this ThreadChannelManager for chaining convenience
     */
    ThreadChannelManager setArchived(boolean archived);

    /**
     * Sets the locked state of this ThreadChannel.
     *
     * This is the equivalent of archiving as a moderator.
     *
     * @see ThreadChannel#isLocked()
     *
     * @param  locked
     *         The new locked state for the selected {@link ThreadChannel}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account is not the thread owner or does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *
     * @return this ThreadChannelManager for chaining convenience.
     */
    ThreadChannelManager setLocked(boolean locked);

    /**
     * Sets the invitable state of this ThreadChannel.
     *
     * <br>This property can only be set on private ThreadChannels.
     *
     * @see ThreadChannel#isInvitable()
     *
     * @param  invitable
     *         The new invitable state for the selected {@link ThreadChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link ThreadChannel} is not a private ThreadChannel
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account is not the thread owner or does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *
     * @return this ThreadChannelManager for chaining convenience.
     */
    ThreadChannelManager setInvitable(boolean invitable);
}

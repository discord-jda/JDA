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

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.managers.channel.ChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Manager providing functionality common for all {@link ThreadChannel ThreadChannels}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setSlowmode(10)
 *        .setArchived(false)
 *        .queue();
 * manager.reset(ChannelManager.SLOWMODE | ChannelManager.NAME)
 *        .setName("Java is to Javascript as car is to carpet")
 *        .setLocked(false)
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
     * <br>Provide {@code 0} to reset the slowmode of the {@link ThreadChannel}.
     *
     * <p>A channel slowmode <b>must not</b> be negative nor greater than {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}!
     *
     * <p>Note: Bots are unaffected by this.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @param  slowmode
     *         The new slowmode, in seconds, for the selected {@link ThreadChannel}
     *
     * @return this ThreadChannelManager for chaining convenience
     *
     * @throws IllegalArgumentException
     *         If the provided slowmode is negative or greater than {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}
     *
     * @see ThreadChannel#getSlowmode()
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelManager setSlowmode(int slowmode);

    /**
     * Sets the inactive time before autoarchiving of this ThreadChannel.
     *
     * This is limited to the choices offered in {@link ThreadChannel.AutoArchiveDuration}
     *
     * @param  autoArchiveDuration
     *         The new duration before an inactive channel will be autoarchived.
     *
     * @return this ThreadChannelManager for chaining convenience.
     *
     * @see ThreadChannel#getAutoArchiveDuration()
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelManager setAutoArchiveDuration(@Nonnull ThreadChannel.AutoArchiveDuration autoArchiveDuration);

    /**
     * Sets the archived state of this ThreadChannel.
     *
     * @param  archived
     *         The new archived state for the selected {@link ThreadChannel}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account is not the thread owner or does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *         Or if the thread is locked (archived by a moderator) and the current account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *
     * @return this ThreadChannelManager for chaining convenience
     *
     * @see ThreadChannel#isArchived()
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelManager setArchived(boolean archived);

    /**
     * Sets the locked state of this ThreadChannel.
     *
     * This is the equivalent of archiving as a moderator.
     *
     * @param  locked
     *         The new locked state for the selected {@link ThreadChannel}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account is not the thread owner or does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *
     * @return this ThreadChannelManager for chaining convenience.
     *
     * @see ThreadChannel#isLocked()
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelManager setLocked(boolean locked);

    /**
     * Sets the invitable state of this ThreadChannel.
     *
     * <br>This property can only be set on private ThreadChannels.
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
     *
     * @see ThreadChannel#isInvitable()
     * @see ThreadChannel#isPublic()
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelManager setInvitable(boolean invitable);

    /**
     * Sets the applied {@link net.dv8tion.jda.api.entities.channel.forums.ForumTag ForumTags} for this forum post thread.
     * <br>This is only applicable to public threads inside forum channels. The tags must be from the forum channel.
     * You can get the list of available tags with {@link ForumChannel#getAvailableTags()}.
     *
     * @param  tags
     *         The new tags for the thread
     *
     * @throws IllegalStateException
     *         If the thread is not a forum post
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If null is provided</li>
     *             <li>If more than {@value ForumChannel#MAX_POST_TAGS} tags are provided</li>
     *             <li>If at least one tag is {@link ForumChannel#isRequireTag() required} and none were provided</li>
     *         </ul>
     *
     * @return this ThreadChannelManager for chaining convenience.
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelManager setAppliedTags(@Nonnull Collection<? extends ForumTagSnowflake> tags);
}

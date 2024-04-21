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
package net.dv8tion.jda.api.managers.channel.concrete

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel.AutoArchiveDuration
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.managers.channel.attribute.ISlowmodeChannelManager
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality common for all [ThreadChannels][ThreadChannel].
 *
 *
 * **Example**
 * <pre>`manager.setSlowmode(10)
 * .setArchived(false)
 * .queue();
 * manager.reset(ChannelManager.SLOWMODE | ChannelManager.NAME)
 * .setName("Java is to Javascript as car is to carpet")
 * .setLocked(false)
 * .setSlowmode(120)
 * .queue();
`</pre> *
 *
 * @see ThreadChannel.getManager
 * @see ThreadChannel
 */
interface ThreadChannelManager : ChannelManager<ThreadChannel?, ThreadChannelManager?>,
    ISlowmodeChannelManager<ThreadChannel?, ThreadChannelManager?> {
    /**
     * Sets the inactive time before autoarchiving of this ThreadChannel.
     *
     *
     * This is limited to the choices offered in [ThreadChannel.AutoArchiveDuration]
     *
     * @param  autoArchiveDuration
     * The new duration before an inactive channel will be autoarchived.
     *
     * @return this ThreadChannelManager for chaining convenience.
     *
     * @see ThreadChannel.getAutoArchiveDuration
     */
    @Nonnull
    @CheckReturnValue
    fun setAutoArchiveDuration(@Nonnull autoArchiveDuration: AutoArchiveDuration?): ThreadChannelManager?

    /**
     * Sets the archived state of this ThreadChannel.
     *
     * @param  archived
     * The new archived state for the selected [ThreadChannel]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account is not the thread owner or does not have the [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission.
     * Or if the thread is locked (archived by a moderator) and the current account does not have the [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission.
     *
     * @return this ThreadChannelManager for chaining convenience
     *
     * @see ThreadChannel.isArchived
     */
    @Nonnull
    @CheckReturnValue
    fun setArchived(archived: Boolean): ThreadChannelManager?

    /**
     * Sets the locked state of this ThreadChannel.
     *
     *
     * This is the equivalent of archiving as a moderator.
     *
     * @param  locked
     * The new locked state for the selected [ThreadChannel]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account is not the thread owner or does not have the [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission.
     *
     * @return this ThreadChannelManager for chaining convenience.
     *
     * @see ThreadChannel.isLocked
     */
    @Nonnull
    @CheckReturnValue
    fun setLocked(locked: Boolean): ThreadChannelManager?

    /**
     * Sets the invitable state of this ThreadChannel.
     *
     *
     * This property can only be set on private ThreadChannels.
     *
     * @param  invitable
     * The new invitable state for the selected [ThreadChannel]
     *
     * @throws IllegalStateException
     * If the selected [ThreadChannel] is not a private ThreadChannel
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account is not the thread owner or does not have the [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission.
     *
     * @return this ThreadChannelManager for chaining convenience.
     *
     * @see ThreadChannel.isInvitable
     * @see ThreadChannel.isPublic
     */
    @Nonnull
    @CheckReturnValue
    fun setInvitable(invitable: Boolean): ThreadChannelManager?

    /**
     * Sets the pinned state of this ThreadChannel.
     *
     *
     * This property can only be set on forum post threads.
     *
     * @param  pinned
     * The new pinned state for the selected [ThreadChannel]
     *
     * @throws IllegalStateException
     * If the selected [ThreadChannel] is not a forum post thread
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account is not the thread owner or does not have the [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission.
     *
     * @return this ThreadChannelManager for chaining convenience.
     *
     * @see ThreadChannel.isPinned
     */
    @Nonnull
    @CheckReturnValue
    fun setPinned(pinned: Boolean): ThreadChannelManager?

    /**
     * Sets the applied [ForumTags][net.dv8tion.jda.api.entities.channel.forums.ForumTag] for this forum post thread.
     * <br></br>This is only applicable to public threads inside forum channels. The tags must be from the forum channel.
     * You can get the list of available tags with [ForumChannel.getAvailableTags].
     *
     * @param  tags
     * The new tags for the thread
     *
     * @throws IllegalStateException
     * If the thread is not a forum post
     * @throws IllegalArgumentException
     *
     *  * If null is provided
     *  * If more than {@value ForumChannel#MAX_POST_TAGS} tags are provided
     *  * If at least one tag is [required][ForumChannel.isTagRequired] and none were provided
     *
     *
     * @return this ThreadChannelManager for chaining convenience.
     */
    @Nonnull
    @CheckReturnValue
    fun setAppliedTags(@Nonnull tags: Collection<ForumTagSnowflake?>?): ThreadChannelManager?

    /**
     * Sets the applied [ForumTags][net.dv8tion.jda.api.entities.channel.forums.ForumTag] for this forum post thread.
     * <br></br>This is only applicable to public threads inside forum channels. The tags must be from the forum channel.
     * You can get the list of available tags with [ForumChannel.getAvailableTags].
     *
     * @param  tags
     * The new tags for the thread
     *
     * @throws IllegalStateException
     * If the thread is not a forum post
     * @throws IllegalArgumentException
     *
     *  * If null is provided
     *  * If more than {@value ForumChannel#MAX_POST_TAGS} tags are provided
     *  * If at least one tag is [required][ForumChannel.isTagRequired] and none were provided
     *
     *
     * @return this ThreadChannelManager for chaining convenience.
     */
    @Nonnull
    @CheckReturnValue
    fun setAppliedTags(@Nonnull vararg tags: ForumTagSnowflake?): ThreadChannelManager? {
        Checks.noneNull(tags, "Tags")
        return setAppliedTags(Arrays.asList(*tags))
    }
}

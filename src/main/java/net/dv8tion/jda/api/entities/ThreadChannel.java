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

import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

//TODO-v5: document all
public interface ThreadChannel extends GuildMessageChannel, IMemberContainer
{
    //TODO fields that need to be researched:
    // - rate_limit_per_user
    // - last_pin_timestamp (do we even use this for Text/News channels?)

    /**
     * Whether this thread is public or not.
     *
     * Public threads can be read and joined by anyone with read access to its {@link IThreadContainer parent channel}.
     *
     * @return true if this thread is public, false otherwise.
     */
    default boolean isPublic()
    {
        ChannelType type = getType();
        return type == ChannelType.GUILD_PUBLIC_THREAD || type == ChannelType.GUILD_NEWS_THREAD;
    }

    /**
     * Gets the current number of messages present in this thread.
     * <br>
     * Threads started from seed messages in the {@link IThreadContainer parent channel} will not count that seed message.
     * <br>
     * This will be capped at 50, regardless of actual count.
     *
     * @return The number of messages sent in this channel, capping at 50.
     */
    int getMessageCount();

    /**
     * Gets the current number of members that have joined this thread.
     * <br/>
     * This is capped at 50, meaning any additional members will not affect this count.
     *
     * @return The number of members that have joined this thread, capping at 50.
     */
    int getMemberCount();

    //TODO | This name is bad. Looking for alternatives.
    //how about isParticipant? isThreadMember
    /**
     * Whether the currently logged in member has joined this thread.
     *
     * @return true if the self member has joined this thread, false otherwise.
     */
    default boolean isJoined()
    {
        return getSelfThreadMember() != null;
    }

    /**
     * Whether this thread is locked or not.
     *
     * Locked threads cannot have new messages posted to them, or members join or leave them.
     *
     * Threads can only be locked and unlocked by moderators.
     * @return true if this thread is locked, false otherwise.
     */
    boolean isLocked();

    /**
     * Whether this thread is invitable.
     * <br>
     * A thread that is invitable can have non-moderators invite other non-moderators to it.
     * A thread that is not invitable can only have moderators invite others to it.
     *
     * <p>This property is exclusive to private threads.
     *
     * @throws UnsupportedOperationException
     *         If this thread is not a private thread.
     *
     * @return true if this thread is invitable, false otherwise.
     */
    boolean isInvitable();

    /**
     * Gets the {@link IThreadContainer parent channel} of this thread.
     *
     * @see IThreadContainer#getThreadChannels()
     *
     * @return The parent channel of this thread.
     */
    @Nonnull
    IThreadContainer getParentChannel();

    /**
     * Gets the {@link GuildMessageChannel parent channel} of this thread, if it is a {@link TextChannel} or {@link NewsChannel}.
     * <br>
     * This is a convenience method that will perform the cast if possible, throwing otherwise.
     *
     * @throws UnsupportedOperationException
     *         If the parent channel is not a {@link GuildMessageChannel}.
     *
     * @return The parent channel of this thread, as a {@link GuildMessageChannel}.
     */
    @Nonnull
    default GuildMessageChannel getParentMessageChannel()
    {
        if (getParentChannel() instanceof GuildMessageChannel) {
            return (GuildMessageChannel) getParentChannel();
        }

        throw new UnsupportedOperationException("Parent of this thread is not a MessageChannel. Parent is type: " + getParentChannel().getType().getId());
    }

    /**
     * Gets the self member, as a member of this thread.
     *
     * <br>If the current account is not a member of this thread, this will return null.
     *
     * @return The self member of this thread, null if the current account is not a member of this thread.
     */
    @Nullable
    default ThreadMember getSelfThreadMember()
    {
        return getThreadMember(getJDA().getSelfUser());
    }


    /**
     * Gets a List of all {@link ThreadMember members} of this thread.
     * <br>
     * <br><b>Requirements</b>:
     * <ul>
     *     <li>the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS} intent must be enabled.</li>
     * </ul>
     *
     * @return a List of all {@link ThreadMember members} of this thread. This list may be empty, but not null.
     */
    @Nonnull
    List<ThreadMember> getThreadMembers();

    /**
     * Gets a {@link ThreadMember} of this thread by their {@link Member}.
     *
     * @param member
     *        The member to get the {@link ThreadMember} for.
     *
     * @return The {@link ThreadMember} of this thread for the given member.
     */
    //TODO-v5: docs - how much of this relies on the GUILD_MEMBERS intent. Current impl is unfinished and so cannot document.
    @Nullable
    default ThreadMember getThreadMember(Member member)
    {
        return getThreadMemberById(member.getId());
    }

    //TODO-v5: docs - how much of this relies on the GUILD_MEMBERS intent. Current impl is unfinished and so cannot document.
    @Nullable
    default ThreadMember getThreadMember(User user)
    {
        return getThreadMemberById(user.getId());
    }

    //TODO-v5: docs - how much of this relies on the GUILD_MEMBERS intent. Current impl is unfinished and so cannot document.
    @Nullable
    default ThreadMember getThreadMemberById(String id)
    {
        return getThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    //TODO-v5: docs - how much of this relies on the GUILD_MEMBERS intent. Current impl is unfinished and so cannot document.
    @Nullable
    ThreadMember getThreadMemberById(long id);

    /**
     * Retrieves the {@link ThreadMember ThreadMembers} of this thread.
     *
     * @return a RestAction that resolves into a List of {@link ThreadMember ThreadMembers} of this thread.
     */
    //TODO-v5: docs - documentation depends on implementation cleanup.
    @CheckReturnValue
    RestAction<List<ThreadMember>> retrieveThreadMembers();

    /**
     * Whether the current account is the owner of this thread.
     *
     * @return true if the self account is the owner of this thread, false otherwise.
     */
    default boolean isOwner()
    {
        return getJDA().getSelfUser().getIdLong() == getOwnerIdLong();
    }

    /**
     * Gets the ID of the owner of this thread as a long.
     *
     * @return the ID of the member who created this thread as a long.
     */
    long getOwnerIdLong();

    /**
     * Gets the {@link User} of the owner of this thread as a String.
     *
     * @return The {@link User} of the member who created this thread as a String.
     */
    @Nullable
    default String getOwnerId()
    {
        return Long.toUnsignedString(getOwnerIdLong());
    }

    /**
     * Gets the {@link User} of the owner of this thread.
     * <br>
     * This will be null if the member is not cached
     *
     * @see #getThreadMemberById(long)
     *
     * @return The {@link User} of the member who created this thread.
     */
    @Nullable
    default Member getOwner()
    {
        return getGuild().getMemberById(getOwnerIdLong());
    }

    /**
     * Gets the owner of this thread as a {@link ThreadMember}.
     * <br>
     * This will be null if the member is not cached.
     *
     * @see #getThreadMemberById(long)
     *
     * @return The owner of this thread as a {@link ThreadMember}.
     */
    @Nullable
    default ThreadMember getOwnerThreadMember()
    {
        return getThreadMemberById(getOwnerIdLong());
    }

    /**
     * Whether this thread has been archived.
     *
     * This method will consider locked channels to also be archived.
     *
     * @see #isLocked()
     * @return true if this thread has been archived, false otherwise.
     */
    boolean isArchived();

    /**
     * The last updated time of the archive info of this thread.
     *
     * <p>This timestamp will be updated when any of the following happen:
     * <ul>
     *     <li>The channel is archived</li>
     *     <li>The channel is unarchived</li>
     *     <li>The AUTO_ARCHIVE_DURATION is changed.</li>
     * </ul>
     *
     * @see ChannelField#ARCHIVED_TIMESTAMP
     *
     * @return the time of the last archive info update.
     */
    OffsetDateTime getTimeArchiveInfoLastModified();

    /**
     * The inactivity timeout of this thread.
     *
     * If a message is not sent within this amount of time, the thread will be automatically archived.
     *
     * A thread archived this way can be unarchived by any member.
     *
     * @return the time before which a thread will automatically be archived.
     */
    @Nonnull
    AutoArchiveDuration getAutoArchiveDuration();




    /**
     * The slowmode time of this thread. This determines the time each non-moderator must wait before sending another message.
     *
     * @see net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager#setSlowmode(int)
     *
     * @return The time between a member sending two messages.
     */
    int getSlowmode();

    /**
     * Joins this thread, adding the current account to the member list of this thread.
     *
     * Note that joining threads is not a requirement of getting events about the thread.
     *
     * <br>This will have no effect if the current account is already a member of this thread.
     *
     * @throws IllegalStateException
     *         If this thread is locked or archived.
     *
     * @return {@link RestAction}
     */
    @CheckReturnValue
    RestAction<Void> join();

    /**
     * Leaves this thread, removing the current account from the member list of this thread.
     *
     * <br>This will have no effect if the current account is not a member of this thread.
     *
     * @throws IllegalStateException
     *         If this thread is locked or archived.
     *
     * @return {@link RestAction}
     */
    @CheckReturnValue
    RestAction<Void> leave();

    //TODO-hex: do the permission requirements differ for private threads?
    //this is probably also affected by private threads that are not invitable
    /**
     * Adds a member to this thread.
     *
     * <br>This will have no effect if the member is already a member of this thread.
     *
     * @throws IllegalStateException
     *         If this thread is locked or archived.
     *
     * @param id
     *        The id of the member to add.
     * @return {@link RestAction}
     */
    @CheckReturnValue
    RestAction<Void> addThreadMemberById(long id);

    //TODO-hex: see above
    @CheckReturnValue
    default RestAction<Void> addThreadMemberById(@Nonnull String id)
    {
        return addThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    //TODO-hex: see above
    @CheckReturnValue
    default RestAction<Void> addThreadMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return addThreadMemberById(user.getIdLong());
    }

    //TODO-hex: see above
    @CheckReturnValue
    default RestAction<Void> addThreadMember(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return addThreadMemberById(member.getIdLong());
    }

    /**
     * Removes a member from this thread.
     *
     * <p>Removing members from public threads or private threads this account does not own <b>requires the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission</b>.
     *
     * @param id
     *        The id of the member to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission, unless.
     *
     * @return {@link RestAction}
     */
    @CheckReturnValue
    RestAction<Void> removeThreadMemberById(long id);

    /**
     * Removes a member from this thread.
     *
     * <p>Removing members from public threads or private threads this account does not own <b>requires the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission</b>.
     *
     * @param id
     *        The id of the member to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission, unless.
     *
     * @return {@link RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> removeThreadMemberById(@Nonnull String id)
    {
        return removeThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Removes a member from this thread.
     *
     * <p>Removing members from public threads or private threads this account does not own <b>requires the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission</b>.
     *
     * @param user
     *        The user to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission, unless.
     *
     * @return {@link RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> removeThreadMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return removeThreadMemberById(user.getId());
    }

    /**
     * Removes a member from this thread.
     *
     * <p>Removing members from public threads or private threads this account does not own <b>requires the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission</b>.
     *
     * @param member
     *        The member to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission, unless.
     *
     * @return {@link RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> removeThreadMember(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return removeThreadMemberById(member.getIdLong());
    }

    @Override
    @Nonnull
    ThreadChannelManager getManager();

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        String out;

        if (alt)
            out = "#" + (upper ? getName().toUpperCase(formatter.locale()) : getName());
        else
            out = getAsMention();

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }

    //////////////////////////

    /**
     * The values permitted for the auto archive duration of a {@link ThreadChannel}.
     *
     * This is the time before an idle thread will be automatically archived.
     *
     * Sending a message to the thread will reset the timer.
     *
     * @see ChannelField#AUTO_ARCHIVE_DURATION
     */
    enum AutoArchiveDuration {
        //TODO: I dislike this naming scheme. Need to come up with something better.
        TIME_1_HOUR(60),
        TIME_24_HOURS(1440),
        TIME_3_DAYS(4320),
        TIME_1_WEEK(10080);

        private final int minutes;

        AutoArchiveDuration(int minutes)
        {
            this.minutes = minutes;
        }

        public int getMinutes()
        {
            return minutes;
        }

        @Nonnull
        public static AutoArchiveDuration fromKey(int minutes)
        {
            for (AutoArchiveDuration duration : values())
            {
                if (duration.getMinutes() == minutes)
                    return duration;
            }
            throw new IllegalArgumentException("Provided key was not recognized. Minutes: " + minutes);
        }
    }
}

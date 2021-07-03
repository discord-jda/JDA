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

import net.dv8tion.jda.api.utils.MiscUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

//TODO Cant actually use GuildChannel because of the significant differences between real channels and threads.
//TODO  Just using it to get something working first.
public interface GuildThread extends GuildChannel, MessageChannel
{
    //Stuff needed from GuildChannel
    // - Interface: Mentionable
    // - getJDA
    // - getName
    // - getGuild
    // - getChannelType
    // - getManager //TODO will need to be ThreadManager, not ChannelManager
    // - getMembers //TODO might not need this as we'll have getThreadMembers()

    //TODO pick a better name (or use getParent once we break from GuildChannel interface?)
    GuildChannel getParentChannel();

    /**
     * The message count for this Thread
     *
     * <p><b>The message count stops after 50 messages, after this the variable won't change.</b>
     *
     * @return The expected message count for this guild
     */
    int getMessageCount();

    /**
     * The member count for this Thread
     *
     * <p><b>The member count stops after 50 members, after this the variable won't change.</b>
     *
     * @return The expected member count for this guild
     */
    int getMemberCount();

    //TODO | This name is bad. Looking for alternatives.
    boolean isSubscribedToThread();

    /**
     * Gets the {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} object of the currently logged in account in this thread.
     * <br>This is basically {@link net.dv8tion.jda.api.JDA#getSelfUser() JDA.getSelfUser()} being provided to {@link #getThreadMember(User) getThreadMember(User)}.
     *
     * @return The Member object of the currently logged in account.
     */
    @Nullable
    GuildThreadMember getSelfThreadMember();

    /**
     *  Collects all the {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMembers} of this thread into a list
     *
     * <p><b>This requires the privileged {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} to be enabled!</b>
     *
     * @return All the cached {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMembers} of this thread.
     */
    List<GuildThreadMember> getThreadMembers();

    /**
     * Gets the Thread specified {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} object for the provided {@link net.dv8tion.jda.api.entities.Member Member}
     * <br>If the member is not in this thread, null is returned.
     *
     * <p>This will only check cached members!
     * @param member
     *        The {@link net.dv8tion.jda.api.entities.Member Member} which to get a related GuildThreadMember object for
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} for the related {@link net.dv8tion.jda.api.entities.Member Member}
     */
    default GuildThreadMember getThreadMember(Member member)
    {
        return getThreadMemberById(member.getId());
    }

    /**
     * Gets the Thread specified {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} object for the provided {@link net.dv8tion.jda.api.entities.User User}
     * <br>If the user is not in this thread, null is returned.
     *
     * <p>This will only check cached members!
     * @param user
     *        The {@link net.dv8tion.jda.api.entities.User User} which to get a related GuildThreadMember object for
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} for the related {@link net.dv8tion.jda.api.entities.User User}
     */
    default GuildThreadMember getThreadMember(User user)
    {
        return getThreadMemberById(user.getId());
    }

    /**
     * Gets the Thread specified {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} object for the provided id}
     * <br>If the user is not in this thread, null is returned.
     *
     * <p>This will only check cached members!
     * @param id
     *        The id which to get a related {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} object for
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} for the related id
     */
    default GuildThreadMember getThreadMemberById(String id)
    {
        return getThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Gets the Thread specified {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} object for the provided id}
     * <br>If the user is not in this thread, null is returned.
     *
     * <p>This will only check cached members!
     * @param id
     *        The id which to get a related {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} object for
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.GuildThreadMember GuildThreadMember} for the related id
     */
    GuildThreadMember getThreadMemberById(long id);

    //Should we provide a getter for the ThreadMember of the thread owner?

    /**
     * The {@link net.dv8tion.jda.api.entities.Member Member} object for the owner of this thread.
     * <p>This is null when the owner is no longer in this guild or not yet loaded (lazy loading).
     * Sometimes owners of guilds delete their account or get banned by Discord.
     *
     *
     * <p>This only works when the member was added to cache.
     * See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * @return Possibly-null Member object for the Guild owner.
     *
     * @see    #getOwnerId()
     * @see    #getOwnerIdLong()
     */
    Member getOwner();

    /**
     * The ID for the current owner of this thread.
     * <br>This is useful for debugging purposes or as a shortcut.
     *
     * @return The ID for the current owner
     *
     * @see    #getOwner()
     * @see    #getOwnerIdLong()
     */
    String getOwnerId();

    /**
     * The ID for the current owner of this guild.
     * <br>This is useful for debugging purposes or as a shortcut.
     *
     * @return The ID for the current owner
     *
     * @see    #getOwner()
     * @see    #getOwnerId()
     */
    long getOwnerIdLong();

    /**
     * Whenever this thread is archived
     * @return True if the thread is archived
     *
     * @see #getTimeArchive()
     * @see #getArchivingMember()
     */
    boolean isArchived();

    /**
     * The timestamp of when this thread got archived
     *
     * <p>This is null when the thread hasn't been archived.
     * check using {@link #isArchived() isArchived()} whenever the channel has been archived or not.
     *
     * @return Possibly-null {@link java.time.OffsetDateTime} object representing the timestamp thread got archived
     *
     * @see #isArchived()
     */
    //TODO This name sucks.
    OffsetDateTime getTimeArchive();

    /**
     * The {@link net.dv8tion.jda.api.entities.Member Member} object for the owner of this thread.
     *
     * <p>Check using {@link #isArchived() isArchived()} whenever the channel has been archived.
     *
     * <p>This is null when the thread hasn't been archived, the archiver is no longer in this guild or not yet loaded (lazy loading).
     *
     * <p>This only works when the member was added to cache.
     * See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Member Member} object for the Thread archiver.
     *
     * @see    #isArchived()
     */
    @Nullable
    Member getArchivingMember();

    /**
     * The member that archived this thread's ID
     *
     * <p>Check using {@link #isArchived() isArchived()} whenever the channel has been archived.
     *
     * <p>This is null when the thread hasn't been archived.
     *
     * @return the id of the member that archived this thread
     *
     * @see    #isArchived()
     */
    @Nullable
    String getArchivingMemberId();

    /**
     * The member that archived this thread's ID
     *
     * <p>Check using {@link #isArchived() isArchived()} whenever the channel has been archived.
     *
     * <p>This is null when the thread hasn't been archived..
     *
     * @return the id of the member that archived this thread
     *
     * @see    #isArchived()
     */
    long getArchivingMemberIdLong();

    //TODO We should consider making this an enum. The only allowed values are: 60(1 hour), 1440(24 hours), 4320(3 days), 10080(1 week)
    AutoArchiveDuration getAutoArchiveDuration();

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

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

public interface ThreadChannel extends GuildMessageChannel, IMemberContainer
{
    //TODO fields that need to be researched:
    // - rate_limit_per_user
    // - last_pin_timestamp (do we even use this for Text/News channels?)

    default boolean isPublic()
    {
        ChannelType type = getType();
        return type == ChannelType.GUILD_PUBLIC_THREAD || type == ChannelType.GUILD_NEWS_THREAD;
    }

    //TODO docs | Max returned amount is capped at 50 regardless of actual count
    int getMessageCount();

    //TODO docs | Max returned amount is capped at 50 regardless of actual count
    int getMemberCount();

    //TODO | This name is bad. Looking for alternatives.
    default boolean isJoined()
    {
        return getSelfThreadMember() != null;
    }

    boolean isLocked();

    boolean isInvitable();

    @Nonnull
    IThreadContainer getParentChannel();

    @Nonnull
    default GuildMessageChannel getParentMessageChannel()
    {
        if (getParentChannel() instanceof GuildMessageChannel) {
            return (GuildMessageChannel) getParentChannel();
        }

        throw new UnsupportedOperationException("Parent of this thread is not a MessageChannel. Parent is type: " + getParentChannel().getType().getId());
    }

    @Nullable
    default ThreadMember getSelfThreadMember()
    {
        return getThreadMember(getJDA().getSelfUser());
    }

    //Only have access to this with GUILD_MEMBERS
    @Nonnull
    List<ThreadMember> getThreadMembers();

    @Nullable
    default ThreadMember getThreadMember(Member member)
    {
        return getThreadMemberById(member.getId());
    }

    @Nullable
    default ThreadMember getThreadMember(User user)
    {
        return getThreadMemberById(user.getId());
    }

    @Nullable
    default ThreadMember getThreadMemberById(String id)
    {
        return getThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    @Nullable
    ThreadMember getThreadMemberById(long id);

    @CheckReturnValue
    RestAction<List<ThreadMember>> retrieveThreadMembers();

    default boolean isOwner()
    {
        return getJDA().getSelfUser().getIdLong() == getOwnerIdLong();
    }

    long getOwnerIdLong();

    @Nullable
    default String getOwnerId()
    {
        return Long.toUnsignedString(getOwnerIdLong());
    }

    @Nullable
    default Member getOwner()
    {
        return getGuild().getMemberById(getOwnerIdLong());
    }

    @Nullable
    default ThreadMember getOwnerThreadMember()
    {
        return getThreadMemberById(getOwnerIdLong());
    }

    boolean isArchived();

    OffsetDateTime getTimeArchiveInfoLastModified();

    @Nonnull
    AutoArchiveDuration getAutoArchiveDuration();

    int getSlowmode();

    @CheckReturnValue
    RestAction<Void> join();

    @CheckReturnValue
    RestAction<Void> leave();

    @CheckReturnValue
    RestAction<Void> addThreadMemberById(long id);

    @CheckReturnValue
    default RestAction<Void> addThreadMemberById(@Nonnull String id)
    {
        return addThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    @CheckReturnValue
    default RestAction<Void> addThreadMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return addThreadMemberById(user.getIdLong());
    }

    @CheckReturnValue
    default RestAction<Void> addThreadMember(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return addThreadMemberById(member.getIdLong());
    }

    @CheckReturnValue
    RestAction<Void> removeThreadMemberById(long id);

    @CheckReturnValue
    default RestAction<Void> removeThreadMemberById(@Nonnull String id)
    {
        return removeThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    @CheckReturnValue
    default RestAction<Void> removeThreadMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return removeThreadMemberById(user.getId());
    }

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

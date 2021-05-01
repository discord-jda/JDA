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

    //TODO docs | Max returned amount is capped at 50 regardless of actual count
    int getMessageCount();

    //TODO docs | Max returned amount is capped at 50 regardless of actual count
    int getMemberCount();

    //TODO | This name is bad. Looking for alternatives.
    boolean isSubscribedToThread();

    @Nullable
    GuildThreadMember getSelfThreadMember();

    //Only have access to this with GUILD_MEMBERS
    List<GuildThreadMember> getThreadMembers();

    default GuildThreadMember getThreadMember(Member member)
    {
        return getThreadMemberById(member.getId());
    }

    default GuildThreadMember getThreadMember(User user)
    {
        return getThreadMemberById(user.getId());
    }

    default GuildThreadMember getThreadMemberById(String id)
    {
        return getThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    GuildThreadMember getThreadMemberById(long id);

    //Should we provide a getter for the ThreadMember of the thread owner?
    Member getOwner();

    String getOwnerId();

    long getOwnerIdLong();

    boolean isArchived();

    //TODO This name sucks.
    OffsetDateTime getTimeArchive();

    @Nullable
    Member getArchivingMember();

    @Nullable
    String getArchivingMemberId();

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

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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ThreadMember;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.internal.entities.channel.concrete.ThreadChannelImpl;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

public class ThreadMemberImpl implements ThreadMember
{
    private final JDA api;
    private final ThreadChannelImpl thread;
    
    private Member member;
    private long joinedTimestamp;

    public ThreadMemberImpl(Member member, ThreadChannelImpl thread)
    {
        this.api = member.jDA;
        this.member = member;
        this.thread = thread;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }
    
    @Nonnull
    @Override
    public Guild getGuild()
    {
        return thread.getGuild();
    }
    
    @Nonnull
    @Override
    public ThreadChannel getThread()
    {
        return this.thread;
    }

    @Nonnull
    @Override
    public User getUser()
    {
        return member.user;
    }

    @Nonnull
    @Override
    public Member getMember()
    {
        return member;
    }

    @Nonnull
    @Override
    public OffsetDateTime getTimeJoined()
    {
        return Helpers.toOffset(joinedTimestamp);
    }

    @Nonnull
    @Override
    public String getAsMention()
    {
        return member.asMention;
    }

    @Override
    public long getIdLong()
    {
        return member.idLong;
    }

    // ===== Setters =======

    public ThreadMemberImpl setJoinedTimestamp(long joinedTimestamp)
    {
        this.joinedTimestamp = joinedTimestamp;
        return this;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("member", getMember())
                .toString();
    }
}

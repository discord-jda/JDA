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

package net.dv8tion.jda.api.events.thread.member;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.entities.ThreadMember;
import net.dv8tion.jda.api.events.thread.GenericThreadEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//TODO-v5: Docs
public class GenericThreadMemberEvent extends GenericThreadEvent
{
    protected final long threadMemberId;
    protected final ThreadMember threadMember;

    public GenericThreadMemberEvent(@Nonnull JDA api, long responseNumber, ThreadChannel thread, long threadMemberId, ThreadMember threadMember)
    {
        super(api, responseNumber, thread);

        this.threadMemberId = threadMemberId;
        this.threadMember = threadMember;
    }

    public String getThreadMemberId()
    {
        return Long.toUnsignedString(getThreadMemberIdLong());
    }

    public long getThreadMemberIdLong()
    {
        return threadMemberId;
    }

    @Nullable
    public ThreadMember getThreadMember()
    {
        return threadMember;
    }

    @Nullable
    public Member getMember()
    {
        return getGuild().getMemberById(threadMemberId);
    }
}

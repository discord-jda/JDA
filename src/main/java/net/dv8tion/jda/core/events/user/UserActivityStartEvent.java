/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.events.user;

import net.dv8tion.jda.client.entities.Friend;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Activity;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.user.update.GenericUserPresenceEvent;

public class UserActivityStartEvent extends GenericUserEvent implements GenericUserPresenceEvent //TODO: Docs, client support?
{
    private final Activity newActivity;
    private final Member member;

    public UserActivityStartEvent(JDA api, long responseNumber, Member member, Activity newActivity)
    {
        super(api, responseNumber, member.getUser());
        this.newActivity = newActivity;
        this.member = member;
    }

    public Activity getNewActivity()
    {
        return newActivity;
    }

    @Override
    public Guild getGuild()
    {
        return member.getGuild();
    }

    @Override
    public Member getMember()
    {
        return member;
    }

    @Override
    public Friend getFriend()
    {
        return null;
    }
}

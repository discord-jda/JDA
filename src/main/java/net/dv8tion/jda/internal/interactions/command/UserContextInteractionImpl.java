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

package net.dv8tion.jda.internal.interactions.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;

public class UserContextInteractionImpl extends ContextInteractionImpl<User> implements UserContextInteraction
{
    private final MemberImpl member;

    public UserContextInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data, (resolved) -> parse(jda, resolved));
        DataObject resolved = data.getObject("data").getObject("resolved");
        if (!resolved.isNull("members"))
        {
            DataObject members = resolved.getObject("members");
            DataObject member = members.getObject(members.keys().iterator().next());
            this.member = jda.getEntityBuilder().createMember((GuildImpl) guild, member);
            jda.getEntityBuilder().updateMemberCache(this.member);
        }
        else
        {
            this.member = null;
        }
    }

    private static User parse(JDAImpl api, DataObject resolved)
    {
        DataObject users = resolved.getObject("users");
        DataObject user = users.getObject(users.keys().iterator().next());
        return api.getEntityBuilder().createUser(user);
    }

    @Override
    public Member getTargetMember()
    {
        return member;
    }
}

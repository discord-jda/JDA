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

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.UserCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import org.jetbrains.annotations.NotNull;

public class UserCommandInteractionImpl extends CommandInteractionImpl implements UserCommandInteraction
{
    private final long userId;
    private final User user;
    private final Member member;

    public UserCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);

        DataObject resolved = data.getObject("data").getObject("resolved");
        DataObject users = resolved.getObject("users");

        EntityBuilder entityBuilder = jda.getEntityBuilder();

        this.userId = data.getObject("data").getUnsignedLong("target_id");
        String userId = Long.toUnsignedString(this.userId);

        if (guild != null)
        {
            DataObject members = resolved.getObject("members");
            DataObject userJson = users.getObject(userId);
            DataObject memberJson = members.getObject(userId);
            memberJson.put("user", userJson);
            MemberImpl member = entityBuilder.createMember((GuildImpl) guild, memberJson);
            entityBuilder.updateMemberCache(member);
            this.member = member;
        }
        else
        {
            member = null;
        }

        DataObject userJson = users.getObject(userId);
        this.user = entityBuilder.createUser(userJson);
    }

    @Override
    public long getInteractedIdLong()
    {
        return userId;
    }

    @NotNull
    @Override
    public User getInteractedUser()
    {
        return user;
    }

    @Override
    public Member getInteractedMember()
    {
        return member;
    }
}

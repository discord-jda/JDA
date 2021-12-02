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

package net.dv8tion.jda.internal.interactions.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.interactions.UserCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

public class UserCommandInteractionImpl extends CommandInteractionImpl implements UserCommandInteraction
{
    protected final long targetID;
    protected final User targetUser;
    protected final Member targetMember;

    public UserCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        DataObject commandData = data.getObject("data");

        this.targetID = commandData.getLong("target_id");

        Optional<Object> memberOptional =
                Arrays.stream(resolved.values())
                        .filter(value -> value instanceof Member)
                        .findFirst();

        targetMember = (Member) memberOptional.orElse(null);

        // Since the user is upgraded to a member, it won't exist
        if(targetMember != null) 
        {
            targetUser = targetMember.getUser();
            return;
        }

        Optional<Object> userOptional =
                Arrays.stream(resolved.values())
                        .filter(value -> value instanceof User)
                        .findFirst();

        //Assigning null would break @Nonnull in #getTargetUser, this should be a discord bug if there's no target user
        targetUser = (User) userOptional.orElseThrow(() -> new NullPointerException("Target user does not exist"));
    }


    @Override
    public long getTargetIdLong()
    {
        return targetID;
    }

    @Override
    @Nonnull
    public User getTargetUser()
    {
        return targetUser;
    }

    @Override
    @Nullable
    public Member getTargetMember()
    {
        return targetMember;
    }
}

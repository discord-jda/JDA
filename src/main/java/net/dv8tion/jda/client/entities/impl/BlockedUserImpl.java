/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.client.entities.impl;

import net.dv8tion.jda.client.entities.BlockedUser;
import net.dv8tion.jda.client.entities.RelationshipType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;

public class BlockedUserImpl implements BlockedUser
{
    private final User user;

    public BlockedUserImpl(User user)
    {
        this.user = user;
    }

    @Override
    public RelationshipType getType()
    {
        return RelationshipType.BLOCKED;
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public RestAction unblockUser()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return "BlockedUser:" + user.getName() + "(" + user.getIdLong() + ")";
    }

    @Override
    public int hashCode()
    {
        return ("BlockedUser " + user.getId()).hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof BlockedUser))
            return false;

        BlockedUser oBU = (BlockedUser) o;
        return this.user.equals(oBU.getUser());
    }
}

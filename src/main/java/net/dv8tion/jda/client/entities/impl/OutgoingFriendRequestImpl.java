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

import net.dv8tion.jda.client.entities.OutgoingFriendRequest;
import net.dv8tion.jda.client.entities.RelationshipType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;

public class OutgoingFriendRequestImpl implements OutgoingFriendRequest
{
    private final User user;

    public OutgoingFriendRequestImpl(User user)
    {
        this.user = user;
    }

    @Override
    public RelationshipType getType()
    {
        return RelationshipType.OUTGOING_FRIEND_REQUEST;
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public RestAction cancel()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return "OFR(" + user.toString() + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof OutgoingFriendRequest))
            return false;

        OutgoingFriendRequest oOFR = (OutgoingFriendRequest) o;
        return user.equals(oOFR.getUser());
    }

    @Override
    public int hashCode()
    {
        return ("OFR " + user.getId()).hashCode();
    }
}

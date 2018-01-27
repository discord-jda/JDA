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

import net.dv8tion.jda.client.entities.Friend;
import net.dv8tion.jda.client.entities.RelationshipType;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;

import java.time.OffsetDateTime;

public class FriendImpl implements Friend
{
    private final User user;

    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;
    private OffsetDateTime lastModifiedTime;
    private Game game;

    public FriendImpl(User user)
    {
        this.user = user;
    }

    @Override
    public RelationshipType getType()
    {
        return RelationshipType.FRIEND;
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public OnlineStatus getOnlineStatus()
    {
        return onlineStatus;
    }

    @Override
    public OffsetDateTime getOnlineStatusModifiedTime()
    {
        return lastModifiedTime;
    }

    @Override
    public RestAction removeFriend()
    {
        return null;
    }

    @Override
    public Game getGame()
    {
        return game;
    }

    @Override
    public String toString()
    {
        return "Friend(" + user.toString() + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Friend))
            return false;

        Friend oFriend = (Friend) o;
        return user.equals(oFriend.getUser());
    }

    @Override
    public int hashCode()
    {
        return ("Friend " + user.getId()).hashCode();
    }

    public FriendImpl setOnlineStatus(OnlineStatus onlineStatus)
    {
        this.onlineStatus = onlineStatus;
        return this;
    }

    public FriendImpl setGame(Game game)
    {
        this.game = game;
        return this;
    }

    public FriendImpl setOnlineStatusModifiedTime(OffsetDateTime lastModifiedTime)
    {
        this.lastModifiedTime = lastModifiedTime;
        return this;
    }
}

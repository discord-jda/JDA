/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;

public class UserImpl implements User
{
    private final String id;
    private String username;
    private String discriminator;
    private String avatarId;
    private int gameId = -1;
    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;
    private PrivateChannel privateChannel = null;

    public UserImpl(String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public String getDiscriminator()
    {
        return discriminator;
    }

    @Override
    public String getAvatarId()
    {
        return avatarId;
    }

    @Override
    public String getAvatarUrl()
    {
        return "https://cdn.discordapp.com/avatars/" + getId() + "/" + getAvatarId() + ".jpg";
    }

    @Override
    public int getCurrentGameId()
    {
        return gameId;
    }

    @Override
    public String getCurrentGameName()
    {
        throw new UnsupportedOperationException("Yet to implement games.json parsing");
    }

    @Override
    public OnlineStatus getOnlineStatus()
    {
        return onlineStatus;
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        if (privateChannel != null)
            return privateChannel;
        throw new UnsupportedOperationException("Currently no support for starting a NEW direct message session.");
    }

    public UserImpl setUserName(String username)
    {
        this.username = username;
        return this;
    }

    public UserImpl setDiscriminator(String discriminator)
    {
        this.discriminator = discriminator;
        return this;
    }

    public UserImpl setAvatarId(String avatarId)
    {
        this.avatarId = avatarId;
        return this;
    }

    public UserImpl setCurrentGameId(int gameId)
    {
        this.gameId = gameId;
        return this;
    }

    public UserImpl setOnlineStatus(OnlineStatus onlineStatus)
    {
        this.onlineStatus = onlineStatus;
        return this;
    }

    public UserImpl setPrivateChannel(PrivateChannel channel)
    {
        this.privateChannel = channel;
        return this;
    }

    public boolean hasPrivateChannel()
    {
        return privateChannel != null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof User))
            return false;
        User oUser = (User) o;
        return this == oUser || this.getId().equals(oUser.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }
}

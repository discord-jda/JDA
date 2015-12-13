package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.User;

public class UserImpl implements User
{
    private final String id;
    private String username;
    private String discriminator;
    private String avatarId;
    private int gameId = -1;
    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;

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

    /**
     * Returns true if one of the following is true:
     *    A) The provided object is the same User instance as this object
     *    B) The provided object is a User object with the same id as this object.
     *    C) The provided object is a String that is equal to our id.
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof User)
        {
            User oUser = (User) o;
            return this == oUser || this.getId().equals(oUser.getId());
        }
        else if (o instanceof String)
        {
            String oString = (String) o;
            return this.getId().equals(oString);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }
}

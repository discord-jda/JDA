package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.entities.User;

public class UserImpl implements User
{
    private String id;
    private String username;
    private String discriminator;
    private String avatarId;
    private int gameId;

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

    public UserImpl setId(String id)
    {
        this.id = id;
        return this;
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
}

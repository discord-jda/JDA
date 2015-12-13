package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;

public class PrivateChannelImpl implements PrivateChannel
{
    private final String id;
    private final User user;

    public PrivateChannelImpl(String id, User user)
    {
        this.id = id;
        this.user = user;
    }
    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public User getUser()
    {
        return user;
    }
}

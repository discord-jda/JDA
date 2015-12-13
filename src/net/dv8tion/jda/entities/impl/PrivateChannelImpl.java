package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;

public class PrivateChannelImpl extends ChannelImpl implements PrivateChannel
{
    private User user;

    @Override
    public User getUser()
    {
        return user;
    }

    public PrivateChannelImpl setUser(User user)
    {
        this.user = user;
        return this;
    }
}

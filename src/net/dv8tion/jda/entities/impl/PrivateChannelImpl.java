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

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof PrivateChannel))
            return false;
        PrivateChannel oPChannel = (PrivateChannel) o;
        return this == oPChannel || this.getId().equals(oPChannel.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }
}

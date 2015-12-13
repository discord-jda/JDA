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

    /**
     * Returns true if one of the following is true:
     *    A) The provided object is the same PrivateChannel instance as this object
     *    B) The provided object is a PrivateChannel object with the same id as this object.
     *    C) The provided object is a String that is equal to our id.
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof PrivateChannel)
        {
            PrivateChannel oPChannel = (PrivateChannel) o;
            return this == oPChannel || this.getId().equals(oPChannel.getId());
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

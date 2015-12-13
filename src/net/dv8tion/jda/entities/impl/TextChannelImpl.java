package net.dv8tion.jda.entities.impl;

import java.util.List;

import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;

public class TextChannelImpl implements TextChannel
{
    private final String id;
    private final Guild guild;
    private String name;
    private String topic;

    public TextChannelImpl(String id, Guild guild)
    {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getTopic()
    {
        return topic;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public List<User> getUsers()
    {
        throw new UnsupportedOperationException("Until permissions is finished, getting the Users in a Channel is not supported");
    }

    public TextChannelImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public TextChannelImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    /**
     * Returns true if one of the following is true:
     *    A) The provided object is the same TextChannel instance as this object
     *    B) The provided object is a TextChannel object with the same id as this object.
     *    C) The provided object is a String that is equal to our id.
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof TextChannel)
        {
            TextChannel oTChannel = (TextChannel) o;
            return this == oTChannel || this.getId().equals(oTChannel.getId());
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

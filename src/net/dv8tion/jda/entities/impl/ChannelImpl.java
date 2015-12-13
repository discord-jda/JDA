package net.dv8tion.jda.entities.impl;

import java.util.List;

import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;

public class ChannelImpl implements Channel
{
    private String id;
    private String name;
    private String topic;
    private Guild guild;

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

    public ChannelImpl setId(String id)
    {
        this.id = id;
        return this;
    }

    public ChannelImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public ChannelImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    public ChannelImpl setGuild(Guild guild)
    {
        this.guild = guild;
        return this;
    }
}

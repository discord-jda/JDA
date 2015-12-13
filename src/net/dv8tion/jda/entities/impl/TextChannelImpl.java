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
}

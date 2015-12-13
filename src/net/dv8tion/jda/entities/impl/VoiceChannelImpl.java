package net.dv8tion.jda.entities.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;

public class VoiceChannelImpl implements VoiceChannel
{
    private String id;
    private String name;
    private Guild guild;
    private List<User> connectedUsers = new ArrayList<>();

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
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public List<User> getUsers()
    {
        return Collections.unmodifiableList(connectedUsers);
    }

    public VoiceChannelImpl setId(String id)
    {
        this.id = id;
        return this;
    }

    public VoiceChannelImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public VoiceChannelImpl setGuild(Guild guild)
    {
        this.guild = guild;
        return this;
    }

    public VoiceChannelImpl setUsers(List<User> connectedUsers)
    {
        this.connectedUsers = connectedUsers;
        return this;
    }

    public List<User> getUsersModifiable()
    {
        return connectedUsers;
    }
}

package net.dv8tion.jda.entities.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.Region;
import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.VoiceChannel;

public class GuildImpl implements Guild
{
    private String id;
    private String name;
    private String iconId;
    private String afkChannelId;
    private String ownerId;
    private int afkTimeout;
    private Region region;
    private List<Channel> textChannels = new ArrayList<>();
    private List<VoiceChannel> voiceChannels = new ArrayList<>();

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
    public String getIconId()
    {
        return iconId;
    }

    @Override
    public String getIconUrl()
    {
        return "https://cdn.discordapp.com/icons/" + getId() + "/" + getIconId() + ".jpg";
    }

    @Override
    public String getAfkChannelId()
    {
        return afkChannelId;
    }

    @Override
    public String getOwnerId()
    {
        return ownerId;
    }

    @Override
    public int getAfkTimeout()
    {
        return afkTimeout;
    }

    @Override
    public Region getRegion()
    {
        return region;
    }


    @Override
    public List<Channel> getTextChannels()
    {
        return Collections.unmodifiableList(textChannels);
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        return Collections.unmodifiableList(voiceChannels);
    }

    public GuildImpl setId(String id)
    {
        this.id = id;
        return this;
    }

    public GuildImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public GuildImpl setIconId(String iconId)
    {
        this.iconId = iconId;
        return this;
    }

    public GuildImpl setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    public GuildImpl setAfkTimeout(int afkTimeout)
    {
        this.afkTimeout = afkTimeout;
        return this;
    }

    public GuildImpl setRegion(Region region)
    {
        this.region = region;
        return this;
    }

    public GuildImpl setTextChannels(List<Channel> textChannels)
    {
        this.textChannels = textChannels;
        return this;
    }

    public GuildImpl setVoiceChannels(List<VoiceChannel> voiceChannels)
    {
        this.voiceChannels = voiceChannels;
        return this;
    }

    public List<Channel> getTextChannelsModifiable()
    {
        return textChannels;
    }

    public List<VoiceChannel> getVoiceChannelsModifiable()
    {
        return voiceChannels;
    }
}

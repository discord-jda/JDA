package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.Region;
import net.dv8tion.jda.entities.*;

import java.util.*;

public class GuildImpl implements Guild
{
    private String id;
    private String name;
    private String iconId;
    private String afkChannelId;
    private String ownerId;
    private int afkTimeout;
    private Region region;
    private List<TextChannel> textChannels = new ArrayList<>();
    private List<VoiceChannel> voiceChannels = new ArrayList<>();
    private List<Role> roles = new ArrayList<>();
    private Map<User, List<Role>> userRoles = new HashMap<>();

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
    public List<TextChannel> getTextChannels()
    {
        return Collections.unmodifiableList(textChannels);
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        return Collections.unmodifiableList(voiceChannels);
    }

    @Override
    public List<Role> getRoles()
    {
        return Collections.unmodifiableList(roles);
    }

    public List<Role> getRolesModifiable()
    {
        return roles;
    }

    @Override
    public List<Role> getRolesForUser(User user)
    {
        return userRoles.get(user) == null ? new LinkedList<>() : Collections.unmodifiableList(userRoles.get(user));
    }

    public Map<User, List<Role>> getRolesMap()
    {
        return userRoles;
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

    public GuildImpl setAfkChannelId(String channelId)
    {
        this.afkChannelId = channelId;
        return this;
    }

    public GuildImpl setRegion(Region region)
    {
        this.region = region;
        return this;
    }

    public GuildImpl setTextChannels(List<TextChannel> textChannels)
    {
        this.textChannels = textChannels;
        return this;
    }

    public GuildImpl setVoiceChannels(List<VoiceChannel> voiceChannels)
    {
        this.voiceChannels = voiceChannels;
        return this;
    }

    public List<TextChannel> getTextChannelsModifiable()
    {
        return textChannels;
    }

    public List<VoiceChannel> getVoiceChannelsModifiable()
    {
        return voiceChannels;
    }
}

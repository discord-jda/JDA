package net.dv8tion.jda.entities.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.TextChannel;

public class SelfInfoImpl extends UserImpl implements SelfInfo
{
    private final String email;
    private List<TextChannel> mutedChannels = new ArrayList<>();
    private boolean verified;

    public SelfInfoImpl(String id, String email)
    {
        super(id);
        this.email = email;
    }

    @Override
    public String getEmail()
    {
        return email;
    }

    @Override
    public List<TextChannel> getMutedChannels()
    {
        return Collections.unmodifiableList(mutedChannels);
    }

    @Override
    public boolean isVerified()
    {
        return verified;
    }

    public SelfInfoImpl setMutedChannels(List<TextChannel> mutedChannels)
    {
        this.mutedChannels = mutedChannels;
        return this;
    }

    public SelfInfoImpl setVerified(boolean verified)
    {
        this.verified = verified;
        return this;
    }

    public List<TextChannel> getMutedChannelsModifiable()
    {
        return mutedChannels;
    }

    /**
     * Returns true if one of the following is true:
     *    A) The provided object is the same SelfInfo instance as this object
     *    B) The provided object is a SelfInfo object with the same id as this object.
     *    C) The provided object is a String that is equal to our id.
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof SelfInfo)
        {
            SelfInfo oSelfInfo = (SelfInfo) o;
            return this == oSelfInfo || this.getId().equals(oSelfInfo.getId());
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

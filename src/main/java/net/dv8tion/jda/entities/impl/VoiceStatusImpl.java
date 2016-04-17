/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.VoiceStatus;

public class VoiceStatusImpl implements VoiceStatus
{
    private final User user;
    private final Guild guild;
    private VoiceChannel channel = null;
    private boolean mute = false, serverMute = false;
    private boolean deaf = false, serverDeaf = false;
    private boolean suppressed = false;
    private String sessionId = null;

    public VoiceStatusImpl(User user, Guild guild)
    {
        this.user = user;
        this.guild = guild;
    }

    @Override
    public boolean isMuted()
    {
        return mute;
    }

    @Override
    public boolean isServerMuted()
    {
        return serverMute;
    }

    @Override
    public boolean isDeaf()
    {
        return deaf;
    }

    @Override
    public boolean isServerDeaf()
    {
        return serverDeaf;
    }

    @Override
    public boolean isSuppressed()
    {
        return suppressed;
    }

    @Override
    public VoiceChannel getChannel()
    {
        return channel;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public String getSessionId()
    {
        return sessionId;
    }

    @Override
    public boolean inVoiceChannel()
    {
        return getChannel() != null;
    }

    public VoiceStatusImpl setMute(boolean mute)
    {
        this.mute = mute;
        return this;
    }

    public VoiceStatusImpl setServerMute(boolean serverMute)
    {
        this.serverMute = serverMute;
        return this;
    }

    public VoiceStatusImpl setDeaf(boolean deaf)
    {
        this.deaf = deaf;
        return this;
    }

    public VoiceStatusImpl setServerDeaf(boolean serverDeaf)
    {
        this.serverDeaf = serverDeaf;
        return this;
    }

    public VoiceStatusImpl setChannel(VoiceChannel channel)
    {
        this.channel = channel;
        return this;
    }

    public VoiceStatusImpl setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    public VoiceStatusImpl setSuppressed(boolean suppressed)
    {
        this.suppressed = suppressed;
        return this;
    }

    @Override
    public int hashCode()
    {
        return guild.getId().hashCode() | user.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof VoiceStatus))
        {
            return false;
        }
        VoiceStatus oStatus = (VoiceStatus) obj;
        return this == oStatus || (this.user.equals(oStatus.getUser()) && this.guild.equals(oStatus.getGuild()));
    }

    @Override
    public String toString()
    {
        return "VS:" + guild.getName() + ':' + user.getUsername();
    }
}

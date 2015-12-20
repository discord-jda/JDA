/**
 * Copyright 2015 Austin Keener & Michael Ritter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
    private boolean mute = false, serverMute = false;
    private boolean deaf = false, serverDeaf = false;
    private final User user;
    private VoiceChannel channel;
    private Guild guild;

    public VoiceStatusImpl(User user)
    {
        this.user = user;
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

    public void setMute(boolean mute)
    {
        this.mute = mute;
    }

    public void setServerMute(boolean serverMute)
    {
        this.serverMute = serverMute;
    }

    public void setDeaf(boolean deaf)
    {
        this.deaf = deaf;
    }

    public void setServerDeaf(boolean serverDeaf)
    {
        this.serverDeaf = serverDeaf;
    }

    public void setChannel(VoiceChannel channel)
    {
        this.channel = channel;
    }

    public void setGuild(Guild guild)
    {
        this.guild = guild;
    }
}

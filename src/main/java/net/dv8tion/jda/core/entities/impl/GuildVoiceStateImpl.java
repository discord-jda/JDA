/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.entities.*;

public class GuildVoiceStateImpl implements GuildVoiceState
{
    private final GuildImpl guild;
    private final Member member;

    private VoiceChannel connectedChannel;
    private String sessionId;
    private boolean selfMuted = false;
    private boolean selfDeafened = false;
    private boolean guildMuted = false;
    private boolean guildDeafened = false;
    private boolean suppressed = false;

    public GuildVoiceStateImpl(GuildImpl guild, Member member)
    {
        this.guild = guild;
        this.member = member;
    }

    @Override
    public boolean isSelfMuted()
    {
        return selfMuted;
    }

    @Override
    public boolean isSelfDeafened()
    {
        return selfDeafened;
    }

    @Override
    public JDAImpl getJDA()
    {
        return guild.getJDA();
    }

    @Override
    public AudioChannel getAudioChannel()
    {
        return connectedChannel;
    }

    @Override
    public String getSessionId()
    {
        return sessionId;
    }

    @Override
    public boolean isMuted()
    {
        return isSelfMuted() || isGuildMuted();
    }

    @Override
    public boolean isDeafened()
    {
        return isSelfDeafened() || isGuildDeafened();
    }

    @Override
    public boolean isGuildMuted()
    {
        return guildMuted;
    }

    @Override
    public boolean isGuildDeafened()
    {
        return guildDeafened;
    }

    @Override
    public boolean isSuppressed()
    {
        return suppressed;
    }

    @Override
    public VoiceChannel getChannel()
    {
        return connectedChannel;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public Member getMember()
    {
        return member;
    }

    @Override
    public boolean inVoiceChannel()
    {
        return getChannel() != null;
    }

    @Override
    public int hashCode()
    {
        return member.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof GuildVoiceState))
        {
            return false;
        }
        GuildVoiceState oStatus = (GuildVoiceState) obj;
        return this == oStatus || (this.member.equals(oStatus.getMember()) && this.guild.equals(oStatus.getGuild()));
    }

    @Override
    public String toString()
    {
        return "VS:" + guild.getName() + ':' + member.getEffectiveName();
    }

    // -- Setters --

    public GuildVoiceStateImpl setConnectedChannel(VoiceChannel connectedChannel)
    {
        this.connectedChannel = connectedChannel;
        return this;
    }

    public GuildVoiceStateImpl setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    public GuildVoiceStateImpl setSelfMuted(boolean selfMuted)
    {
        this.selfMuted = selfMuted;
        return this;
    }

    public GuildVoiceStateImpl setSelfDeafened(boolean selfDeafened)
    {
        this.selfDeafened = selfDeafened;
        return this;
    }

    public GuildVoiceStateImpl setGuildMuted(boolean guildMuted)
    {
        this.guildMuted = guildMuted;
        return this;
    }

    public GuildVoiceStateImpl setGuildDeafened(boolean guildDeafened)
    {
        this.guildDeafened = guildDeafened;
        return this;
    }

    public GuildVoiceStateImpl setSuppressed(boolean suppressed)
    {
        this.suppressed = suppressed;
        return this;
    }
}

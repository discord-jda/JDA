/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.entities.proxy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

public class GuildVoiceStateProxy implements GuildVoiceState, ProxyEntity
{
    private final MemberProxy member;

    public GuildVoiceStateProxy(Member member)
    {
        this.member = member.getProxy();
    }

    @Override
    public GuildVoiceState getSubject()
    {
        return getMember().getVoiceState();
    }

    @Override
    public GuildVoiceStateProxy getProxy()
    {
        return this;
    }

    @Override
    public boolean isMuted()
    {
        return getSubject().isMuted();
    }

    @Override
    public boolean isDeafened()
    {
        return getSubject().isDeafened();
    }

    @Override
    public boolean isGuildMuted()
    {
        return getSubject().isGuildMuted();
    }

    @Override
    public boolean isGuildDeafened()
    {
        return getSubject().isGuildDeafened();
    }

    @Override
    public boolean isSuppressed()
    {
        return getSubject().isSuppressed();
    }

    @Override
    public VoiceChannel getChannel()
    {
        return getSubject().getChannel();
    }

    @Override
    public Guild getGuild()
    {
        return member.getGuild();
    }

    @Override
    public Member getMember()
    {
        return member.getSubject();
    }

    @Override
    public boolean inVoiceChannel()
    {
        return getSubject().inVoiceChannel();
    }

    @Override
    public boolean isSelfMuted()
    {
        return getSubject().isSelfMuted();
    }

    @Override
    public boolean isSelfDeafened()
    {
        return getSubject().isSelfDeafened();
    }

    @Override
    public JDA getJDA()
    {
        return getMember().getJDA();
    }

    @Override
    public AudioChannel getAudioChannel()
    {
        return getSubject().getAudioChannel();
    }

    @Override
    public String getSessionId()
    {
        return getSubject().getSessionId();
    }

    @Override
    public int hashCode()
    {
        return getSubject().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj == this || getSubject().equals(obj);
    }

    @Override
    public String toString()
    {
        return getSubject().toString();
    }
}

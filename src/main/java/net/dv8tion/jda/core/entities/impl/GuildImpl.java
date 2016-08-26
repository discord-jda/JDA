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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceStatus;

import java.util.HashMap;
import java.util.List;

public class GuildImpl implements Guild
{
    private final String id;
    private final JDAImpl api;
    private HashMap<String, Member> memberMap = new HashMap<>();

    private String name;
    private String iconId;
    private Member owner;

    public GuildImpl(JDAImpl api, String id)
    {
        this.api = api;
        this.id = id;
    }

    @Override
    public String getId()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public String getIconId()
    {
        return null;
    }

    @Override
    public String getIconUrl()
    {
        return null;
    }

    @Override
    public String getAfkChannelId()
    {
        return null;
    }

    @Override
    public Member getOwner()
    {
        return null;
    }

    @Override
    public int getAfkTimeout()
    {
        return 0;
    }

    @Override
    public Region getRegion()
    {
        return null;
    }

    @Override
    public boolean isMember(User user)
    {
        return false;
    }

    @Override
    public Member getMemberById(String userId)
    {
        return null;
    }

    @Override
    public Member getMember(User user)
    {
        return null;
    }

    @Override
    public List<Member> getMembers()
    {
        return null;
    }

    @Override
    public JDA getJDA()
    {
        return null;
    }

    @Override
    public List<VoiceStatus> getVoiceStatuses()
    {
        return null;
    }

    @Override
    public VerificationLevel getVerificationLevel()
    {
        return null;
    }

    @Override
    public boolean checkVerification()
    {
        return false;
    }

    @Override
    public boolean isAvailable()
    {
        return false;
    }
}

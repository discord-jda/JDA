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

package net.dv8tion.jda.client.entities.impl;

import net.dv8tion.jda.client.entities.*;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.AudioChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class CallVoiceStateImpl implements CallVoiceState
{
    private final CallUser cUser;

    private String sessionId;
    private boolean selfMuted = false;
    private boolean selfDeafened = false;
    private boolean inCall = false;

    public CallVoiceStateImpl(CallUser cUser)
    {
        this.cUser = cUser;
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
    public JDA getJDA()
    {
        return cUser.getUser().getJDA();
    }

    @Override
    public AudioChannel getAudioChannel()
    {
        return getCall();
    }

    @Override
    public String getSessionId()
    {
        return sessionId;
    }

    @Override
    public User getUser()
    {
        return cUser.getUser();
    }

    @Override
    public Call getCall()
    {
        return cUser.getCall();
    }

    @Override
    public CallUser getCallUser()
    {
        return cUser;
    }

    @Override
    public boolean isInCall()
    {
        return inCall;
    }

    @Override
    public boolean isGroupCall()
    {
        return getCall().isGroupCall();
    }

    @Override
    public CallableChannel getCallableChannel()
    {
        return getCall().getCallableChannel();
    }

    @Override
    public Group getGroup()
    {
        return getCall().getGroup();
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        return getCall().getPrivateChannel();
    }

    @Override
    public String toString()
    {
        return "CallVS(" + cUser.toString() + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof CallVoiceState))
            return false;

        CallVoiceState oCVS = (CallVoiceState) o;
        return cUser.equals(oCVS.getCallUser());
    }

    @Override
    public int hashCode()
    {
        return ("CallVS " + cUser.toString()).hashCode();
    }

    public CallVoiceStateImpl setSelfMuted(boolean selfMuted)
    {
        this.selfMuted = selfMuted;
        return this;
    }

    public CallVoiceStateImpl setSelfDeafened(boolean selfDeafened)
    {
        this.selfDeafened = selfDeafened;
        return this;
    }

    public CallVoiceStateImpl setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    public CallVoiceStateImpl setInCall(boolean inCall)
    {
        this.inCall = inCall;
        return this;
    }
}

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

package net.dv8tion.jda.client.entities.impl;

import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.client.entities.CallVoiceState;
import net.dv8tion.jda.client.entities.CallableChannel;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.AudioChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class CallVoiceStateImpl implements CallVoiceState
{
    private final Call call;
    private final User user;

    private String sessionId;
    private boolean selfMuted = false;
    private boolean selfDeafened = false;
    private boolean inCall = false;

    public CallVoiceStateImpl(Call call, User user)
    {
        this.call = call;
        this.user = user;
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
        return user.getJDA();
    }

    @Override
    public AudioChannel getAudioChannel()
    {
        return call;
    }

    @Override
    public String getSessionId()
    {
        return sessionId;
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public Call getCall()
    {
        return call;
    }

    @Override
    public boolean isInCall()
    {
        return inCall;
    }

    @Override
    public boolean isGroupCall()
    {
        return call.isGroupCall();
    }

    @Override
    public CallableChannel getCallableChannel()
    {
        return call.getCallableChannel();
    }

    @Override
    public Group getGroup()
    {
        return call.getGroup();
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        return call.getPrivateChannel();
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

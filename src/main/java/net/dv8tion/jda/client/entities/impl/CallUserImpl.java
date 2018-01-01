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

import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.client.entities.CallUser;
import net.dv8tion.jda.client.entities.CallVoiceState;
import net.dv8tion.jda.core.entities.User;

public class CallUserImpl implements CallUser
{
    protected final Call call;
    protected final User user;
    protected final CallVoiceState voiceState;

    protected boolean ringing;

    public CallUserImpl(Call call, User user)
    {
        this.call = call;
        this.user = user;
        this.voiceState = new CallVoiceStateImpl(this);
    }

    @Override
    public Call getCall()
    {
        return call;
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public CallVoiceState getVoiceState()
    {
        return voiceState;
    }

    @Override
    public boolean isRinging()
    {
        return ringing;
    }

    @Override
    public String toString()
    {
        return "CallUser:(" + user.toString() + " | " + call.toString() + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof CallUser))
            return false;

        CallUser oCU = (CallUser) o;

        return user.equals(oCU.getUser()) && call.equals(oCU.getCall());
    }

    @Override
    public int hashCode()
    {
        return ("CallUser " + user.getId() + call.getId()).hashCode();
    }

    public CallUserImpl setRinging(boolean ringing)
    {
        this.ringing = ringing;
        return this;
    }
}

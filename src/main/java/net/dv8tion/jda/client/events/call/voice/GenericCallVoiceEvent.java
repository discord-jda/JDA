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

package net.dv8tion.jda.client.events.call.voice;

import net.dv8tion.jda.client.entities.CallUser;
import net.dv8tion.jda.client.entities.CallVoiceState;
import net.dv8tion.jda.client.events.call.GenericCallEvent;
import net.dv8tion.jda.core.JDA;

public abstract class GenericCallVoiceEvent extends GenericCallEvent
{
    protected final CallUser cUser;

    public GenericCallVoiceEvent(JDA api, long responseNumber, CallUser cUser)
    {
        super(api, responseNumber, cUser.getCall());
        this.cUser = cUser;
    }

    public CallUser getCallUser()
    {
        return cUser;
    }

    public CallVoiceState getVoiceState()
    {
        return cUser.getVoiceState();
    }
}

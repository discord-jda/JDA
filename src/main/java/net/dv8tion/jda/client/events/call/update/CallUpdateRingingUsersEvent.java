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

package net.dv8tion.jda.client.events.call.update;

import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.client.entities.CallUser;
import net.dv8tion.jda.core.JDA;

import java.util.Collections;
import java.util.List;

public class CallUpdateRingingUsersEvent extends GenericCallUpdateEvent
{
    protected final List<CallUser> usersStoppedRinging;
    protected final List<CallUser> usersStartedRinging;

    public CallUpdateRingingUsersEvent(JDA api, long responseNumber, Call call, List<CallUser> usersStoppedRinging, List<CallUser> usersStartedRinging)
    {
        super(api, responseNumber, call);
        this.usersStoppedRinging = Collections.unmodifiableList(usersStoppedRinging);
        this.usersStartedRinging = Collections.unmodifiableList(usersStartedRinging);
    }

    public List<CallUser> getUsersStoppedRinging()
    {
        return usersStoppedRinging;
    }

    public List<CallUser> getUsersStartedRinging()
    {
        return usersStartedRinging;
    }
}

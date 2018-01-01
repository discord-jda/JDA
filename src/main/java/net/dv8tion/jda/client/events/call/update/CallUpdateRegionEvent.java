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
import net.dv8tion.jda.client.events.call.GenericCallEvent;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;

public class CallUpdateRegionEvent extends GenericCallEvent
{
    protected final Region oldRegion;

    public CallUpdateRegionEvent(JDA api, long responseNumber, Call call, Region oldRegion)
    {
        super(api, responseNumber, call);
        this.oldRegion = oldRegion;
    }

    public Region getOldRegion()
    {
        return oldRegion;
    }
}

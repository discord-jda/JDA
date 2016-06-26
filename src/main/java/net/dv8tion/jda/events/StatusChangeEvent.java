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
package net.dv8tion.jda.events;

import net.dv8tion.jda.JDA;

/**
 * <b><u>StatusChangedEvent</u></b><br>
 * Fired if our {@link net.dv8tion.jda.JDA.Status Status} changed. (Example: SHUTTING_DOWN -> SHUTDOWN)<br>
 * <br>
 * Use: Detect internal status changes. Possibly to log or forward on user's end.
 */
public class StatusChangeEvent extends Event
{
    protected final JDA.Status newStatus;
    protected final JDA.Status oldStatus;

    public StatusChangeEvent(JDA api, JDA.Status newStatus, JDA.Status oldStatus)
    {
        super(api, -1);
        this.newStatus = newStatus;
        this.oldStatus = oldStatus;
    }

    public JDA.Status getStatus()
    {
        return newStatus;
    }

    public JDA.Status getOldStatus()
    {
        return oldStatus;
    }
}

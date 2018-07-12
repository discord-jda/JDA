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
package net.dv8tion.jda.core.events;

import net.dv8tion.jda.core.JDA;

/**
 * Indicates that our {@link net.dv8tion.jda.core.JDA.Status Status} changed. (Example: SHUTTING_DOWN {@literal ->} SHUTDOWN)
 *
 * <br>Can be used to detect internal status changes. Possibly to log or forward on user's end.
 *
 * <p>Identifier: {@code status}
 */
public class StatusChangeEvent extends Event implements UpdateEvent<JDA, JDA.Status>
{
    public static final String IDENTIFIER = "status";

    protected final JDA.Status newStatus;
    protected final JDA.Status oldStatus;

    public StatusChangeEvent(JDA api, JDA.Status newStatus, JDA.Status oldStatus)
    {
        super(api);
        this.newStatus = newStatus;
        this.oldStatus = oldStatus;
    }

    /**
     * The status that we changed to
     *
     * @return The new status
     */
    public JDA.Status getNewStatus()
    {
        return newStatus;
    }

    /**
     * The previous status
     *
     * @return The previous status
     */
    public JDA.Status getOldStatus()
    {
        return oldStatus;
    }

    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Override
    public JDA getEntity()
    {
        return getJDA();
    }

    @Override
    public JDA.Status getOldValue()
    {
        return oldStatus;
    }

    @Override
    public JDA.Status getNewValue()
    {
        return newStatus;
    }

    @Override
    public String toString()
    {
        return "StatusUpdate(" + getOldStatus() + "->" + getNewStatus() + ')';
    }
}

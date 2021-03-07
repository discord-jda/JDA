/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.events;

import net.dv8tion.jda.api.JDA;

import javax.annotation.Nonnull;

/**
 * Indicates that our {@link net.dv8tion.jda.api.JDA.Status Status} changed. (Example: SHUTTING_DOWN {@literal ->} SHUTDOWN)
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

    public StatusChangeEvent(@Nonnull JDA api, @Nonnull JDA.Status newStatus, @Nonnull JDA.Status oldStatus)
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
    @Nonnull
    public JDA.Status getNewStatus()
    {
        return newStatus;
    }

    /**
     * The previous status
     *
     * @return The previous status
     */
    @Nonnull
    public JDA.Status getOldStatus()
    {
        return oldStatus;
    }

    @Nonnull
    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Nonnull
    @Override
    public JDA getEntity()
    {
        return getJDA();
    }

    @Nonnull
    @Override
    public JDA.Status getOldValue()
    {
        return oldStatus;
    }

    @Nonnull
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

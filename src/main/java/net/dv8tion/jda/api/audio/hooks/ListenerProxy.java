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

package net.dv8tion.jda.api.audio.hooks;

import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Internal implementation of {@link ConnectionListener}, to handle possible exceptions thrown by user code.
 */
public class ListenerProxy implements ConnectionListener
{
    private static final Logger log = LoggerFactory.getLogger(ListenerProxy.class);
    private volatile ConnectionListener listener = null;

    @Override
    public void onPing(long ping)
    {
        if (listener == null)
            return;
        ConnectionListener listener = this.listener;
        try
        {
            if (listener != null)
                listener.onPing(ping);
        }
        catch (Throwable t)
        {
            log.error("The ConnectionListener encountered and uncaught exception", t);
            if (t instanceof Error)
                throw (Error) t;
        }
    }

    @Override
    public void onStatusChange(@Nonnull ConnectionStatus status)
    {
        if (listener == null)
            return;
        ConnectionListener listener = this.listener;
        try
        {
            if (listener != null)
                listener.onStatusChange(status);
        }
        catch (Throwable t)
        {
            log.error("The ConnectionListener encountered and uncaught exception", t);
            if (t instanceof Error)
                throw (Error) t;
        }
    }

    @Override
    public void onUserSpeakingModeUpdate(@Nonnull UserSnowflake user, @Nonnull EnumSet<SpeakingMode> modes)
    {
        if (listener == null)
            return;
        ConnectionListener listener = this.listener;
        try
        {
            if (listener != null)
            {
                listener.onUserSpeakingModeUpdate(user, modes);
                if (user instanceof User)
                    listener.onUserSpeakingModeUpdate((User) user, modes);
            }
        }
        catch (Throwable t)
        {
            log.error("The ConnectionListener encountered and uncaught exception", t);
            if (t instanceof Error)
                throw (Error) t;
        }
    }

    public void setListener(@Nullable ConnectionListener listener)
    {
        this.listener = listener;
    }

    public ConnectionListener getListener()
    {
        return listener;
    }
}

/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.audio.hooks;

import net.dv8tion.jda.core.entities.User;

public class ListenerProxy implements ConnectionListener
{
    private final Object listenerLock = new Object();
    private ConnectionListener listener = null;

    @Override
    public void onPing(long ping)
    {
        synchronized (listenerLock)
        {
            if (listener == null)
                return;

            try
            {
                listener.onPing(ping);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void onStatusChange(ConnectionStatus status)
    {
        synchronized (listenerLock)
        {
            if (listener == null)
                return;

            try
            {
                listener.onStatusChange(status);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void onUserSpeaking(User user, boolean speaking)
    {
        synchronized (listenerLock)
        {
            if (listener == null)
                return;

            try
            {
                listener.onUserSpeaking(user, speaking);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

    public void setListener(ConnectionListener listener)
    {
        synchronized (listenerLock)
        {
            this.listener = listener;
        }
    }

    public ConnectionListener getListener()
    {
        return listener;
    }
}

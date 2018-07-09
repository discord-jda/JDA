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

package net.dv8tion.jda.core.audio.hooks;

import net.dv8tion.jda.core.audio.SpeakingMode;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

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
        }
    }

    @Override
    public void onStatusChange(ConnectionStatus status)
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
        }
    }

    @Override
    public void onUserSpeaking(User user, EnumSet<SpeakingMode> modes)
    {
        if (listener == null)
            return;
        ConnectionListener listener = this.listener;
        try
        {
            if (listener != null)
            {
                listener.onUserSpeaking(user, modes);
                listener.onUserSpeaking(user, modes.contains(SpeakingMode.VOICE));
                listener.onUserSpeaking(user, modes.contains(SpeakingMode.VOICE), modes.contains(SpeakingMode.SOUNDSHARE));
            }
        }
        catch (Throwable t)
        {
            log.error("The ConnectionListener encountered and uncaught exception", t);
        }
    }

    @Override
    public void onUserSpeaking(User user, boolean speaking) {}

    public void setListener(ConnectionListener listener)
    {
        this.listener = listener;
    }

    public ConnectionListener getListener()
    {
        return listener;
    }
}

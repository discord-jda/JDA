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
package net.dv8tion.jda.events.audio;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.VoiceChannel;

/**
 * <b><u>AudioTimeoutEvent</u></b><br>
 * Fired if an attempt to connect to a {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} failed due to a timeout.<br>
 * <br>
 * Use: Retrieve {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} which caused this event to fire.
 */
public class AudioTimeoutEvent extends GenericAudioEvent
{
    protected final VoiceChannel attemptedConnectChannel;
    protected final long timeout;

    public AudioTimeoutEvent(JDA api, VoiceChannel attemptedConnectChannel, long timeout)
    {
        super(api, -1);
        this.attemptedConnectChannel = attemptedConnectChannel;
        this.timeout = timeout;
    }

    public VoiceChannel getAttemptedConnectChannel()
    {
        return attemptedConnectChannel;
    }

    public long getTimeout()
    {
        return timeout;
    }
}

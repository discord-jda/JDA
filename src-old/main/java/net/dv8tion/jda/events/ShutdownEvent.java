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
import net.dv8tion.jda.entities.VoiceChannel;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <b><u>ShutdownEvent</u></b><br>
 * Fired if JDA successfully finished shutting down.<br>
 *<br>
 * Use: Confirmation of JDA#shutdown(boolean).
 */
public class ShutdownEvent extends Event
{
    protected final OffsetDateTime shutdownTime;
    protected final List<VoiceChannel> dcAudioConnections;

    public ShutdownEvent(JDA api, OffsetDateTime shutdownTime, List<VoiceChannel> dcAudioConnections)
    {
        super(api, -1);
        this.shutdownTime = shutdownTime;
        this.dcAudioConnections = Collections.unmodifiableList(new LinkedList<>(dcAudioConnections));
    }

    public OffsetDateTime getShutdownTime()
    {
        return shutdownTime;
    }

    public List<VoiceChannel> getDisconnectedAudioConnections()
    {
        return dcAudioConnections;
    }
}

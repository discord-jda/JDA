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
package net.dv8tion.jda.events.voice;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceStatus;
import net.dv8tion.jda.events.Event;

/**
 * <b><u>GenericVoiceEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.entities.VoiceStatus VoiceStatus} of a {@link net.dv8tion.jda.entities.User User} changes. (like mute/deaf/leave)<br>
 * Every VoiceEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any VoiceEvent. <i>(No real use for the JDA user)</i>
 */
public abstract class GenericVoiceEvent extends Event
{
    protected final VoiceStatus voiceStatus;

    public GenericVoiceEvent(JDA api, int responseNumber, VoiceStatus voiceStatus)
    {
        super(api, responseNumber);
        this.voiceStatus = voiceStatus;
    }

    public User getUser()
    {
        return voiceStatus.getUser();
    }

    public Guild getGuild()
    {
        return voiceStatus.getGuild();
    }

    public VoiceStatus getVoiceStatus()
    {
        return voiceStatus;
    }
}

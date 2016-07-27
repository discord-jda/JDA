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
import net.dv8tion.jda.entities.VoiceStatus;

/**
 * <b><u>VoiceDeafEvent</u></b><br/>
 * Fired if we are (un-)deafened. <br/>
 * This can indicate both deafen and un-deafen and can be caused by both us or the server.<br/>
 * {@link net.dv8tion.jda.events.voice.VoiceSelfDeafEvent} and {@link net.dv8tion.jda.events.voice.VoiceServerDeafEvent} are specifications of this event.
 */
public class VoiceDeafEvent extends GenericVoiceEvent
{
    public VoiceDeafEvent(JDA api, int responseNumber, VoiceStatus voiceStatus)
    {
        super(api, responseNumber, voiceStatus);
    }

    boolean isDeaf()
    {
        return isSelfDeaf() || isServerDeaf();
    }

    boolean isSelfDeaf()
    {
        return voiceStatus.isDeaf();
    }

    boolean isServerDeaf()
    {
        return voiceStatus.isServerDeaf();
    }
}

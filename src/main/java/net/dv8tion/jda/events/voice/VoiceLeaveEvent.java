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
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.VoiceStatus;

/**
 * <b><u>VoiceLeaveEvent</u></b><br/>
 * Fired if we successfully left a VoiceChannel.<br/>
 * <br/>
 * Use: Retrieve previous VoiceChannel.
 */
public class VoiceLeaveEvent extends GenericVoiceEvent
{
    protected final VoiceChannel channel;

    public VoiceLeaveEvent(JDA api, int responseNumber, VoiceStatus voiceStatus, VoiceChannel channel)
    {
        super(api, responseNumber, voiceStatus);
        this.channel = channel;
    }

    public VoiceChannel getOldChannel()
    {
        return channel;
    }
}

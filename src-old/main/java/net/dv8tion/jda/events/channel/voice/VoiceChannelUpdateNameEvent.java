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
package net.dv8tion.jda.events.channel.voice;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.VoiceChannel;

/**
 * <b><u>VoiceChannelUpdateNameEvent</u></b><br/>
 * Fired if a {@link VoiceChannel VoiceChannel}'s name changes.<br/>
 * <br/>
 * Use: Get affected VoiceChannel, affected Guild and previous name.
 */
public class VoiceChannelUpdateNameEvent extends GenericVoiceChannelUpdateEvent
{
    private final String oldName;

    public VoiceChannelUpdateNameEvent(JDA api, int responseNumber, VoiceChannel channel, String oldName)
    {
        super(api, responseNumber, channel);
        this.oldName = oldName;
    }

    public String getOldName()
    {
        return oldName;
    }
}

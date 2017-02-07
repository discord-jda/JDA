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
package net.dv8tion.jda.core.events.channel.voice;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;

/**
 * <b><u>GenericVoiceChannelEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} event is fired.<br>
 * Every VoiceChannelEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any VoiceChannelEvent.
 */
public abstract class GenericVoiceChannelEvent extends Event
{
    private final VoiceChannel channel;

    public GenericVoiceChannelEvent(JDA api, long responseNumber, VoiceChannel channel)
    {
        super(api, responseNumber);
        this.channel = channel;
    }

    public VoiceChannel getChannel()
    {
        return channel;
    }

    public Guild getGuild()
    {
        return channel.getGuild();
    }
}

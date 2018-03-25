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

package net.dv8tion.jda.core.events.guild.voice;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.Member Member} connected to a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
 *
 * <p><b>When the {@link net.dv8tion.jda.core.entities.Member Member} is moved a {@link net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent GuildVoiceMoveEvent} is fired instead</b>
 *
 * <p>Can be used to detect when a member joins a voice channel for the first time.
 */
public class GuildVoiceJoinEvent extends GenericGuildVoiceEvent
{
    protected final VoiceChannel channelJoined;

    public GuildVoiceJoinEvent(JDA api, long responseNumber, Member member)
    {
        super(api, responseNumber, member);
        this.channelJoined = member.getVoiceState().getChannel();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that was joined
     *
     * @return The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     */
    public VoiceChannel getChannelJoined()
    {
        return channelJoined;
    }
}

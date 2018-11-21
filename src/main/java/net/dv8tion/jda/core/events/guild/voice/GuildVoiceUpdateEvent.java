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

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.UpdateEvent;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.Member Member} joined or left a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
 * <br>Generic event that combines
 * {@link net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent GuildVoiceLeaveEvent},
 * {@link net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent GuildVoiceJoinEvent}, and
 * {@link net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent GuildVoiceMoveEvent} for convenience.
 *
 * <p>Can be used to detect when a Member leaves/joins a channel
 *
 * <p>Identifier: {@code voice-channel}
 */
public interface GuildVoiceUpdateEvent extends UpdateEvent<Member, VoiceChannel>
{
    String IDENTIFIER = "voice-channel";

    /**
     * The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that the {@link net.dv8tion.jda.core.entities.Member Member} is moved from
     *
     * @return The {@link net.dv8tion.jda.core.entities.VoiceChannel}
     */
    VoiceChannel getChannelLeft();

    /**
     * The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that was joined
     *
     * @return The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     */
    VoiceChannel getChannelJoined();
}

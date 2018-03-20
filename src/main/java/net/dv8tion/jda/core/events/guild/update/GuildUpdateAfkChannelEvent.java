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

package net.dv8tion.jda.core.events.guild.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * Indicates that the afk-channel of a {@link net.dv8tion.jda.core.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when an afk channel changes and retrieve the old one
 *
 * <p>Identifier: {@code afk_channel}
 */
public class GuildUpdateAfkChannelEvent extends GenericGuildUpdateEvent<VoiceChannel>
{
    public static final String IDENTIFIER = "afk_channel";

    public GuildUpdateAfkChannelEvent(JDA api, long responseNumber, Guild guild, VoiceChannel oldAfkChannel)
    {
        super(api, responseNumber, guild, oldAfkChannel, guild.getAfkChannel(), IDENTIFIER);
    }

    /**
     * The old afk channel
     *
     * @return The old afk channel, or null
     */
    public VoiceChannel getOldAfkChannel()
    {
        return getOldValue();
    }

    /**
     * The new afk channel
     *
     * @return The new afk channel, or null
     */
    public VoiceChannel getNewAfkChannel()
    {
        return getNewValue();
    }
}

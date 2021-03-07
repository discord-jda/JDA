/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.events.guild.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the afk-channel of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when an afk channel changes and retrieve the old one
 *
 * <p>Identifier: {@code afk_channel}
 */
public class GuildUpdateAfkChannelEvent extends GenericGuildUpdateEvent<VoiceChannel>
{
    public static final String IDENTIFIER = "afk_channel";

    public GuildUpdateAfkChannelEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nullable VoiceChannel oldAfkChannel)
    {
        super(api, responseNumber, guild, oldAfkChannel, guild.getAfkChannel(), IDENTIFIER);
    }

    /**
     * The old afk channel
     *
     * @return The old afk channel, or null
     */
    @Nullable
    public VoiceChannel getOldAfkChannel()
    {
        return getOldValue();
    }

    /**
     * The new afk channel
     *
     * @return The new afk channel, or null
     */
    @Nullable
    public VoiceChannel getNewAfkChannel()
    {
        return getNewValue();
    }
}

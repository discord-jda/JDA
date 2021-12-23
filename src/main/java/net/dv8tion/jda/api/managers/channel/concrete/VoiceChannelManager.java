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

package net.dv8tion.jda.api.managers.channel.concrete;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.attribute.ICategorizableChannelManager;
import net.dv8tion.jda.api.managers.channel.attribute.IPositionableChannelManager;
import net.dv8tion.jda.api.managers.channel.middleman.AudioChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface VoiceChannelManager extends
        AudioChannelManager<VoiceChannel, VoiceChannelManager>,
        ICategorizableChannelManager<VoiceChannel, VoiceChannelManager>,
        IPositionableChannelManager<VoiceChannel, VoiceChannelManager>
{
    /**
     * Sets the <b><u>user-limit</u></b> of the selected {@link VoiceChannel VoiceChannel}.
     * <br>Provide {@code 0} to reset the user-limit of the {@link VoiceChannel VoiceChannel}
     *
     * <p>A channel user-limit <b>must not</b> be negative nor greater than {@code 99}!
     * <br><b>This is only available to {@link VoiceChannel VoiceChannels}</b>
     *
     * @param  userLimit
     *         The new user-limit for the selected {@link VoiceChannel VoiceChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link GuildChannel GuildChannel}'s type is not {@link ChannelType#VOICE VOICE}
     * @throws IllegalArgumentException
     *         If the provided user-limit is negative or greater than {@code 99}
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    VoiceChannelManager setUserLimit(int userLimit);
}

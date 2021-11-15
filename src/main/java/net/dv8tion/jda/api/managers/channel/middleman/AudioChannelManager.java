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

package net.dv8tion.jda.api.managers.channel.middleman;

import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.channel.ChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface AudioChannelManager<T extends AudioChannel, M extends AudioChannelManager<T, M>> extends ChannelManager<T, M>
{
    /**
     * Sets the <b><u>bitrate</u></b> of the selected {@link VoiceChannel VoiceChannel}.
     * <br>The default value is {@code 64000}
     *
     * <p>A channel bitrate <b>must not</b> be less than {@code 8000} nor greater than {@link Guild#getMaxBitrate()}!
     * <br><b>This is only available to {@link VoiceChannel VoiceChannels}</b>
     *
     * @param  bitrate
     *         The new bitrate for the selected {@link VoiceChannel VoiceChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link GuildChannel GuildChannel}'s type is not {@link ChannelType#VOICE VOICE}
     * @throws IllegalArgumentException
     *         If the provided bitrate is less than 8000 or greater than {@link Guild#getMaxBitrate()}.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    Guild#getFeatures()
     */
    @Nonnull
    @CheckReturnValue
    M setBitrate(int bitrate);

    /**
     * Sets the {@link Region Region} of the selected {@link VoiceChannel VoiceChannel}.
     * <br>The default value is {@link Region#AUTOMATIC}
     *
     * Possible values are:
     * <ul>
     *     <li>{@link Region#AUTOMATIC}</li>
     *     <li>{@link Region#US_WEST}</li>
     *     <li>{@link Region#US_EAST}</li>
     *     <li>{@link Region#US_CENTRAL}</li>
     *     <li>{@link Region#US_SOUTH}</li>
     *     <li>{@link Region#SINGAPORE}</li>
     *     <li>{@link Region#SOUTH_AFRICA}</li>
     *     <li>{@link Region#SYDNEY}</li>
     *     <li>{@link Region#EUROPE}</li>
     *     <li>{@link Region#INDIA}</li>
     *     <li>{@link Region#SOUTH_KOREA}</li>
     *     <li>{@link Region#BRAZIL}</li>
     *     <li>{@link Region#JAPAN}</li>
     *     <li>{@link Region#RUSSIA}</li>
     * </ul>
     *
     * <br><b>This is only available to {@link VoiceChannel VoiceChannels}!</b>
     *
     * @param region
     *        The new {@link Region Region}
     * @throws IllegalStateException
     *         If the selected {@link GuildChannel GuildChannel}'s type is not {@link ChannelType#VOICE VOICE}
     * @throws IllegalArgumentException
     *         If the provided Region is not in the list of usable values
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M setRegion(Region region);
}

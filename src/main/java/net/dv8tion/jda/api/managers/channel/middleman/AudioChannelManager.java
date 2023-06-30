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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Manager providing functionality common for all {@link net.dv8tion.jda.api.entities.channel.middleman.AudioChannel AudioChannels}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setBitrate(48000)
 *        .setRegion(Region.AUTOMATIC)
 *        .queue();
 * manager.reset(ChannelManager.REGION | ChannelManager.BITRATE)
 *        .setRegion(Region.BRAZIL)
 *        .queue();
 * }</pre>
 *
 * @param <T> The channel type
 * @param <M> The manager type
 *
 * @see net.dv8tion.jda.api.entities.channel.middleman.AudioChannel#getManager()
 */
public interface AudioChannelManager<T extends AudioChannel, M extends AudioChannelManager<T, M>> extends StandardGuildChannelManager<T, M>
{
    /**
     * Sets the <b><u>bitrate</u></b> of the selected {@link AudioChannel}.
     * <br>The default value is {@code 64000}
     *
     * <p>A channel bitrate <b>must not</b> be less than {@code 8000} nor greater than {@link Guild#getMaxBitrate()}!
     * <br><b>This is only available to {@link AudioChannel AudioChannels}</b>
     *
     * @param  bitrate
     *         The new bitrate for the selected {@link AudioChannel}
     *
     * @throws IllegalStateException
     *         If the selected channel is not an {@link AudioChannel}
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
     * Sets the <b><u>user-limit</u></b> of the selected {@link AudioChannel}.
     * <br>Provide {@code 0} to reset the user-limit of the {@link AudioChannel}
     *
     * <p>A channel user-limit <b>must not</b> be negative nor greater than {@value VoiceChannel#MAX_USERLIMIT} for {@link VoiceChannel}
     * and not greater than {@value StageChannel#MAX_USERLIMIT} for {@link StageChannel}!
     * <br><b>This is only available to {@link AudioChannel AudioChannels}</b>
     *
     * @param  userLimit
     *         The new user-limit for the selected {@link AudioChannel}
     *
     * @throws IllegalStateException
     *         If the selected channel is not an {@link AudioChannel}
     * @throws IllegalArgumentException
     *         If the provided user-limit is negative or greater than the permitted maximum
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M setUserLimit(int userLimit);

    /**
     * Sets the {@link Region Region} of the selected {@link AudioChannel}.
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
     *     <li>{@link Region#INDIA}</li>
     *     <li>{@link Region#SOUTH_KOREA}</li>
     *     <li>{@link Region#BRAZIL}</li>
     *     <li>{@link Region#JAPAN}</li>
     *     <li>{@link Region#RUSSIA}</li>
     * </ul>
     *
     * <br><b>This is only available to {@link AudioChannel AudioChannels}!</b>
     *
     * @param region
     *        The new {@link Region Region}
     *
     * @throws IllegalStateException
     *         If the selected channel is not an {@link AudioChannel}
     * @throws IllegalArgumentException
     *         If the provided Region is not in the list of usable values
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M setRegion(@Nonnull Region region);
}

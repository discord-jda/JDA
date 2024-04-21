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
package net.dv8tion.jda.api.managers.channel.middleman

import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality common for all [AudioChannels][net.dv8tion.jda.api.entities.channel.middleman.AudioChannel].
 *
 *
 * **Example**
 * <pre>`manager.setBitrate(48000)
 * .setRegion(Region.AUTOMATIC)
 * .queue();
 * manager.reset(ChannelManager.REGION | ChannelManager.BITRATE)
 * .setRegion(Region.BRAZIL)
 * .queue();
`</pre> *
 *
 * @param <T> The channel type
 * @param <M> The manager type
 *
 * @see net.dv8tion.jda.api.entities.channel.middleman.AudioChannel.getManager
</M></T> */
interface AudioChannelManager<T : AudioChannel?, M : AudioChannelManager<T, M>?> : StandardGuildChannelManager<T, M> {
    /**
     * Sets the **<u>bitrate</u>** of the selected [AudioChannel].
     * <br></br>The default value is `64000`
     *
     *
     * A channel bitrate **must not** be less than `8000` nor greater than [Guild.getMaxBitrate]!
     * <br></br>**This is only available to [AudioChannels][AudioChannel]**
     *
     * @param  bitrate
     * The new bitrate for the selected [AudioChannel]
     *
     * @throws IllegalStateException
     * If the selected channel is not an [AudioChannel]
     * @throws IllegalArgumentException
     * If the provided bitrate is less than 8000 or greater than [Guild.getMaxBitrate].
     *
     * @return ChannelManager for chaining convenience
     *
     * @see Guild.getFeatures
     */
    @Nonnull
    @CheckReturnValue
    fun setBitrate(bitrate: Int): M

    /**
     * Sets the **<u>user-limit</u>** of the selected [AudioChannel].
     * <br></br>Provide `0` to reset the user-limit of the [AudioChannel]
     *
     *
     * A channel user-limit **must not** be negative nor greater than {@value VoiceChannel#MAX_USERLIMIT} for [VoiceChannel]
     * and not greater than {@value StageChannel#MAX_USERLIMIT} for [StageChannel]!
     * <br></br>**This is only available to [AudioChannels][AudioChannel]**
     *
     * @param  userLimit
     * The new user-limit for the selected [AudioChannel]
     *
     * @throws IllegalStateException
     * If the selected channel is not an [AudioChannel]
     * @throws IllegalArgumentException
     * If the provided user-limit is negative or greater than the permitted maximum
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setUserLimit(userLimit: Int): M

    /**
     * Sets the [Region] of the selected [AudioChannel].
     * <br></br>The default value is [Region.AUTOMATIC]
     *
     * Possible values are:
     *
     *  * [Region.AUTOMATIC]
     *  * [Region.US_WEST]
     *  * [Region.US_EAST]
     *  * [Region.US_CENTRAL]
     *  * [Region.US_SOUTH]
     *  * [Region.SINGAPORE]
     *  * [Region.SOUTH_AFRICA]
     *  * [Region.SYDNEY]
     *  * [Region.INDIA]
     *  * [Region.SOUTH_KOREA]
     *  * [Region.BRAZIL]
     *  * [Region.JAPAN]
     *  * [Region.RUSSIA]
     *
     *
     * <br></br>**This is only available to [AudioChannels][AudioChannel]!**
     *
     * @param region
     * The new [Region]
     *
     * @throws IllegalStateException
     * If the selected channel is not an [AudioChannel]
     * @throws IllegalArgumentException
     * If the provided Region is not in the list of usable values
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setRegion(@Nonnull region: Region?): M
}

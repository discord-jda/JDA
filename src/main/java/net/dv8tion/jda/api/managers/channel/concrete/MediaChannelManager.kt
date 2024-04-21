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
package net.dv8tion.jda.api.managers.channel.concrete

import net.dv8tion.jda.api.entities.channel.concrete.MediaChannel
import net.dv8tion.jda.api.managers.channel.attribute.IAgeRestrictedChannelManager
import net.dv8tion.jda.api.managers.channel.attribute.IPostContainerManager
import net.dv8tion.jda.api.managers.channel.attribute.ISlowmodeChannelManager
import net.dv8tion.jda.api.managers.channel.middleman.StandardGuildChannelManager
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to modify a [MediaChannel].
 *
 *
 * **Example**
 * <pre>`manager.setName("Art Showcase")
 * .setSlowmode(10)
 * .setTopic("Showcase your art creations here.")
 * .queue();
 * manager.reset(ChannelManager.NSFW | ChannelManager.NAME)
 * .setName("NSFW Art Showcase")
 * .setNSFW(true)
 * .queue();
`</pre> *
 */
interface MediaChannelManager : StandardGuildChannelManager<MediaChannel?, MediaChannelManager?>,
    IPostContainerManager<MediaChannel?, MediaChannelManager?>,
    IAgeRestrictedChannelManager<MediaChannel?, MediaChannelManager?>,
    ISlowmodeChannelManager<MediaChannel?, MediaChannelManager?> {
    /**
     * Sets whether to hide the download media option on this channel.
     *
     * @param  hideOption
     * Whether to hide the download option
     *
     * @return ChannelManager for chaining convenience.
     *
     * @see MediaChannel.isMediaDownloadHidden
     */
    @Nonnull
    @CheckReturnValue
    fun setHideMediaDownloadOption(hideOption: Boolean): MediaChannelManager?
}

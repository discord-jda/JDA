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

import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.dv8tion.jda.api.managers.channel.attribute.IAgeRestrictedChannelManager
import net.dv8tion.jda.api.managers.channel.attribute.IThreadContainerManager
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality common for all [StandardGuildMessageChannels][net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel].
 *
 *
 * **Example**
 * <pre>`manager.setName("help")
 * .setTopic("Java is to Javascript as ham is to hamster")
 * .queue();
 * manager.reset(ChannelManager.PARENT | ChannelManager.NAME)
 * .setTopic("nsfw-commits")
 * .setNSFW(true)
 * .queue();
`</pre> *
 *
 * @see StandardGuildMessageChannel.getManager
 */
interface StandardGuildMessageChannelManager<T : StandardGuildMessageChannel?, M : StandardGuildMessageChannelManager<T, M>?> :
    StandardGuildChannelManager<T, M>, IAgeRestrictedChannelManager<T, M>, IThreadContainerManager<T, M> {
    /**
     * Sets the **<u>topic</u>** of the selected [channel][StandardGuildMessageChannel].
     *
     * @param  topic
     * The new topic for the selected channel,
     * `null` or empty String to reset
     *
     * @throws IllegalArgumentException
     * If the provided topic is greater than {@value StandardGuildMessageChannel#MAX_TOPIC_LENGTH} in length.
     * For [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel],
     * this limit is {@value net.dv8tion.jda.api.entities.channel.concrete.ForumChannel#MAX_FORUM_TOPIC_LENGTH} instead.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setTopic(topic: String?): M
}

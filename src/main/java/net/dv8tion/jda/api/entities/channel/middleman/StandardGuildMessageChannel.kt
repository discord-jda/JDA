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
package net.dv8tion.jda.api.entities.channel.middleman

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a standard [GuildMessageChannel] which are the "*normal*" message channels that are present in the channel sidebar.
 * They include all the functionality of [StandardGuildChannels][StandardGuildChannel] along with the extra functionality
 * expected of normal guild message channels like [messaging][GuildMessageChannel], [thread support][net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer], and [webhooks][net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer].
 *
 * <br></br>These are **not** [ThreadChannels][ThreadChannel].
 *
 * @see GuildMessageChannel
 *
 * @see TextChannel
 *
 * @see NewsChannel
 *
 * @see StandardGuildChannel
 */
interface StandardGuildMessageChannel : StandardGuildChannel, GuildMessageChannel, IThreadContainer, IWebhookContainer,
    IAgeRestrictedChannel {
    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?

    /**
     * The topic set for this channel.
     * <br></br>If no topic has been set, this returns null.
     *
     * @return Possibly-null String containing the topic of this channel.
     */
    @JvmField
    val topic: String?
    @Nonnull
    @CheckReturnValue
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<out StandardGuildMessageChannel?>?
    @Nonnull
    @CheckReturnValue
    override fun createCopy(): ChannelAction<out StandardGuildMessageChannel?>?

    companion object {
        /**
         * The maximum length a channel topic can be ({@value #MAX_TOPIC_LENGTH})
         * <br></br>Forum channels have a higher limit, defined by [ForumChannel.MAX_FORUM_TOPIC_LENGTH]
         */
        const val MAX_TOPIC_LENGTH = 1024
    }
}

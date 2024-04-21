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
package net.dv8tion.jda.api.entities.channel.concrete

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import net.dv8tion.jda.api.requests.restaction.ForumPostAction
import javax.annotation.Nonnull

/**
 * A Forum Channel which contains [Forum Posts][.createForumPost].
 * <br></br>Forum posts are simply [ThreadChannels][ThreadChannel] of type [ChannelType.GUILD_PUBLIC_THREAD].
 *
 *
 * The `CREATE POSTS` permission that is shown in the official Discord Client, is an alias for [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND].
 * [Permission.CREATE_PUBLIC_THREADS][net.dv8tion.jda.api.Permission.CREATE_PUBLIC_THREADS] is ignored for creating forum posts.
 *
 * @see Guild.createForumChannel
 * @see .createForumPost
 */
interface ForumChannel : StandardGuildChannel, IPostContainer, IWebhookContainer, IAgeRestrictedChannel,
    ISlowmodeChannel {
    @get:Nonnull
    override val type: ChannelType?
        get() = ChannelType.FORUM

    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?
    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<ForumChannel?>?
    @Nonnull
    override fun createCopy(): ChannelAction<ForumChannel?>? {
        return createCopy(getGuild())
    }

    @JvmField
    @get:Nonnull
    val defaultLayout: Layout?

    /**
     * The layout used to sort forum posts.
     */
    enum class Layout(
        /**
         * The underlying value as used by Discord.
         *
         * @return The raw order key
         */
        @JvmField val key: Int
    ) {
        /**
         * Displayed as default (not set).
         */
        DEFAULT_VIEW(0),

        /**
         * Displayed as a chronological list.
         */
        LIST_VIEW(1),

        /**
         * Displayed as a collection of tiles.
         */
        GALLERY_VIEW(2),

        /**
         * Placeholder for future layout types.
         */
        UNKNOWN(-1);

        companion object {
            /**
             * The [Layout] for the provided key.
             *
             * @param  key
             * The key to get the [Layout] for
             *
             * @return The [Layout] for the provided key, or [.UNKNOWN] if the key is not known
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): Layout {
                for (layout in entries) {
                    if (layout.key == key) return layout
                }
                return UNKNOWN
            }
        }
    }

    companion object {
        /**
         * The maximum length of a forum topic ({@value #MAX_FORUM_TOPIC_LENGTH})
         */
        @JvmField
        val MAX_FORUM_TOPIC_LENGTH: Int = IPostContainer.Companion.MAX_POST_CONTAINER_TOPIC_LENGTH

        /**
         * The maximum number of [tags][ForumPostAction.setTags] that can be applied to a forum post. ({@value #MAX_POST_TAGS})
         */
        @JvmField
        val MAX_POST_TAGS: Int = IPostContainer.Companion.MAX_POST_TAGS
    }
}

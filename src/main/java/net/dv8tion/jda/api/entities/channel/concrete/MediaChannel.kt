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

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelFlag
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import javax.annotation.Nonnull

/**
 * A Media Channel which contains [Forum Posts][.createForumPost].
 * <br></br>Forum posts are simply [ThreadChannels][ThreadChannel] of type [ChannelType.GUILD_PUBLIC_THREAD].
 *
 *
 * The `CREATE POSTS` permission that is shown in the official Discord Client, is an alias for [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND].
 * [Permission.CREATE_PUBLIC_THREADS][net.dv8tion.jda.api.Permission.CREATE_PUBLIC_THREADS] is ignored for creating forum posts.
 *
 * @see Guild.createMediaChannel
 * @see .createForumPost
 */
interface MediaChannel : StandardGuildChannel, IPostContainer, IWebhookContainer, IAgeRestrictedChannel,
    ISlowmodeChannel {
    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?
    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<MediaChannel?>?
    @Nonnull
    override fun createCopy(): ChannelAction<MediaChannel?>? {
        return createCopy(getGuild())
    }

    @get:Nonnull
    override val type: ChannelType?
        get() = ChannelType.MEDIA
    val isMediaDownloadHidden: Boolean
        /**
         * Whether this media channel hides the download option for embeds.
         *
         * @return True, if download option is hidden
         */
        get() = flags.contains(ChannelFlag.HIDE_MEDIA_DOWNLOAD_OPTIONS)
}

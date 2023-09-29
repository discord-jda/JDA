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

package net.dv8tion.jda.api.entities.channel.concrete;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelFlag;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.managers.channel.concrete.MediaChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import javax.annotation.Nonnull;

/**
 * A Media Channel which contains {@link #createForumPost(String, MessageCreateData) Forum Posts}.
 * <br>Forum posts are simply {@link ThreadChannel ThreadChannels} of type {@link ChannelType#GUILD_PUBLIC_THREAD}.
 *
 * <p>The {@code CREATE POSTS} permission that is shown in the official Discord Client, is an alias for {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}.
 * {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS Permission.CREATE_PUBLIC_THREADS} is ignored for creating forum posts.
 *
 * @see Guild#createMediaChannel(String, Category)
 * @see #createForumPost(String, MessageCreateData)
 */
public interface MediaChannel extends StandardGuildChannel, IPostContainer, IWebhookContainer, IAgeRestrictedChannel, ISlowmodeChannel
{
    @Nonnull
    @Override
    MediaChannelManager getManager();

    @Nonnull
    @Override
    ChannelAction<MediaChannel> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    default ChannelAction<MediaChannel> createCopy()
    {
        return createCopy(getGuild());
    }

    @Nonnull
    @Override
    default ChannelType getType()
    {
        return ChannelType.MEDIA;
    }

    /**
     * Whether this media channel hides the download option for embeds.
     *
     * @return True, if download option is hidden
     */
    default boolean isMediaDownloadHidden()
    {
        return getFlags().contains(ChannelFlag.HIDE_MEDIA_DOWNLOAD_OPTIONS);
    }
}

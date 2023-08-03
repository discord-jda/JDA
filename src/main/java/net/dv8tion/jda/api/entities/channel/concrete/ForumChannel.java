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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.managers.channel.concrete.ForumChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.ForumPostAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A Forum Channel which contains {@link #createForumPost(String, MessageCreateData) Forum Posts}.
 * <br>Forum posts are simply {@link ThreadChannel ThreadChannels} of type {@link ChannelType#GUILD_PUBLIC_THREAD}.
 *
 * <p>The {@code CREATE POSTS} permission that is shown in the official Discord Client, is an alias for {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}.
 * {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS Permission.CREATE_PUBLIC_THREADS} is ignored for creating forum posts.
 *
 * @see Guild#createForumChannel(String, Category)
 * @see #createForumPost(String, MessageCreateData)
 */
public interface ForumChannel extends StandardGuildChannel, IPostContainer, IWebhookContainer, IAgeRestrictedChannel, ISlowmodeChannel
{
    /**
     * The maximum length of a forum topic ({@value #MAX_FORUM_TOPIC_LENGTH})
     */
    int MAX_FORUM_TOPIC_LENGTH = IPostContainer.MAX_POST_CONTAINER_TOPIC_LENGTH;
    /**
     * The maximum number of {@link ForumPostAction#setTags(Collection) tags} that can be applied to a forum post. ({@value #MAX_POST_TAGS})
     */
    int MAX_POST_TAGS = IPostContainer.MAX_POST_TAGS;

    @Nonnull
    @Override
    default ChannelType getType()
    {
        return ChannelType.FORUM;
    }

    @Nonnull
    @Override
    ForumChannelManager getManager();

    @Nonnull
    @Override
    ChannelAction<ForumChannel> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    default ChannelAction<ForumChannel> createCopy()
    {
        return createCopy(getGuild());
    }

    /**
     * The default layout used to show threads.
     *
     * @return The default layout used to show threads.
     */
    @Nonnull
    Layout getDefaultLayout();

    /**
     * The layout used to sort forum posts.
     */
    enum Layout
    {
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

        private final int key;

        Layout(int key)
        {
            this.key = key;
        }

        /**
         * The underlying value as used by Discord.
         *
         * @return The raw order key
         */
        public int getKey()
        {
            return key;
        }

        /**
         * The {@link Layout} for the provided key.
         *
         * @param  key
         *         The key to get the {@link Layout} for
         *
         * @return The {@link Layout} for the provided key, or {@link #UNKNOWN} if the key is not known
         */
        @Nonnull
        public static Layout fromKey(int key)
        {
            for (Layout layout : values())
            {
                if (layout.key == key)
                    return layout;
            }

            return UNKNOWN;
        }
    }
}

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

import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.managers.channel.attribute.IAgeRestrictedChannelManager;
import net.dv8tion.jda.api.managers.channel.attribute.IThreadContainerManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manager providing functionality common for all {@link net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel StandardGuildMessageChannels}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("help")
 *        .setTopic("Java is to Javascript as ham is to hamster")
 *        .queue();
 * manager.reset(ChannelManager.PARENT | ChannelManager.NAME)
 *        .setTopic("nsfw-commits")
 *        .setNSFW(true)
 *        .queue();
 * }</pre>
 *
 * @see StandardGuildMessageChannel#getManager()
 */
public interface StandardGuildMessageChannelManager<T extends StandardGuildMessageChannel, M extends StandardGuildMessageChannelManager<T, M>>
        extends StandardGuildChannelManager<T, M>, IAgeRestrictedChannelManager<T, M>, IThreadContainerManager<T, M>
{
     /**
     * Sets the <b><u>topic</u></b> of the selected {@link StandardGuildMessageChannel channel}.
     *
     * @param  topic
     *         The new topic for the selected channel,
     *         {@code null} or empty String to reset
     *
     * @throws IllegalArgumentException
     *         If the provided topic is greater than {@value StandardGuildMessageChannel#MAX_TOPIC_LENGTH} in length.
     *         For {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels},
     *         this limit is {@value net.dv8tion.jda.api.entities.channel.concrete.ForumChannel#MAX_FORUM_TOPIC_LENGTH} instead.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M setTopic(@Nullable String topic);
}

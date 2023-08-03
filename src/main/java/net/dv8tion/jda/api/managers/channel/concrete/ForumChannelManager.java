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

package net.dv8tion.jda.api.managers.channel.concrete;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.managers.channel.attribute.IAgeRestrictedChannelManager;
import net.dv8tion.jda.api.managers.channel.attribute.IPostContainerManager;
import net.dv8tion.jda.api.managers.channel.attribute.ISlowmodeChannelManager;
import net.dv8tion.jda.api.managers.channel.middleman.StandardGuildChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Manager providing functionality to modify a {@link ForumChannel ForumChannel}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("gamer-forum")
 *  .setSlowmode(10)
 *  .setTopic("Welcome to the gamer forum!")
 *  .queue();
 * manager.reset(ChannelManager.NSFW | ChannelManager.NAME)
 *  .setName("gamer-forum-nsfw")
 *  .setNSFW(true)
 *  .queue();
 * }</pre>
 */
public interface ForumChannelManager extends
        StandardGuildChannelManager<ForumChannel, ForumChannelManager>,
        IPostContainerManager<ForumChannel, ForumChannelManager>,
        IAgeRestrictedChannelManager<ForumChannel, ForumChannelManager>,
        ISlowmodeChannelManager<ForumChannel, ForumChannelManager>
{
    /**
     * Sets the <b><u>default layout</u></b> of the selected {@link ForumChannel}.
     *
     * @param  layout
     *         The new default layout.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    ForumChannel#getDefaultLayout()
     */
    @Nonnull
    @CheckReturnValue
    ForumChannelManager setDefaultLayout(@Nonnull ForumChannel.Layout layout);
}

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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.managers.channel.attribute.IAgeRestrictedChannelManager;
import net.dv8tion.jda.api.managers.channel.middleman.StandardGuildChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

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
        IAgeRestrictedChannelManager<ForumChannel, ForumChannelManager>
{
    /**
     * Sets the tag requirement state of this {@link ForumChannel}.
     * <br>If true, all new posts must have at least one tag.
     *
     * @param  requireTag
     *         The new tag requirement state for the selected {@link ForumChannel}
     *
     * @return ChannelManager for chaining convenience.
     *
     * @see    ForumChannel#isRequireTag()
     */
    @Nonnull
    @CheckReturnValue
    ForumChannelManager setRequireTag(boolean requireTag);

    /**
     * Sets the <b><u>slowmode</u></b> of the selected {@link ForumChannel}.
     * <br>Provide {@code 0} to reset the slowmode of the {@link ForumChannel}
     *
     * <p>A channel slowmode <b>must not</b> be negative nor greater than {@link TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}!
     *
     * <p>Note: Bots are unaffected by this.
     * <br>Having {@link Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @param  slowmode
     *         The new slowmode for the selected {@link ForumChannel}
     *
     * @throws IllegalArgumentException
     *         If the provided slowmode is negative or greater than {@link TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    ForumChannel#getSlowmode()
     */
    @Nonnull
    @CheckReturnValue
    ForumChannelManager setSlowmode(int slowmode);

    /**
     * Sets the <b><u>available tags</u></b> of the selected {@link ForumChannel}.
     * <br>Tags will be ordered based on the provided list order.
     *
     * <p>This is a full replacement of the tags list, all missing tags will be removed.
     * You can use {@link ForumTagData} to create new tags or update existing ones.
     *
     * <p><b>Example</b>
     * <pre>{@code
     * List<BaseForumTag> tags = new ArrayList<>(channel.getAvailableTags());
     * tags.add(new ForumTagData("question").setModerated(true)); // add a new tag
     * tags.set(0, ForumTagData.from(tags.get(0)).setName("bug report")); // update an existing tag
     * // Update the tag list
     * channel.getManager().setAvailableTags(tags).queue();
     * }</pre>
     *
     * @param  tags
     *         The new available tags in the desired order.
     *
     * @throws IllegalArgumentException
     *         If the provided list is null or contains null elements
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    ForumChannel#getAvailableTags()
     */
    @Nonnull
    @CheckReturnValue
    ForumChannelManager setAvailableTags(@Nonnull List<? extends BaseForumTag> tags);
}

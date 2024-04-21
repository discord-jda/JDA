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
package net.dv8tion.jda.api.entities.channel.forums

import net.dv8tion.jda.api.entities.channel.forums.ForumTagData
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.managers.channel.concrete.ForumChannelManager
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Data class used to create or update existing forum tags.
 *
 * @see ForumChannelManager.setAvailableTags
 */
class ForumTagData(@Nonnull name: String?) : BaseForumTag {
    @get:Nonnull
    override var name: String? = null
        private set
    private override var emoji: Emoji? = null
    override var isModerated = false
        private set
    private var id: Long = 0

    /**
     * Create a new [ForumTagData] instance.
     *
     * @param name
     * The tag name (1-{@value ForumTag#MAX_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     * If the provided name is null or not between 1 and  {@value ForumTag#MAX_NAME_LENGTH} characters long
     */
    init {
        setName(name)
    }

    /**
     * Set the new tag name to use.
     *
     * @param  name
     * The new tag name (1-{@value ForumTag#MAX_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     * If the provided name is null or not between 1 and  {@value ForumTag#MAX_NAME_LENGTH} characters long
     *
     * @return The updated ForumTagData instance
     */
    @Nonnull
    fun setName(@Nonnull name: String?): ForumTagData {
        Checks.notEmpty(name, "Name")
        Checks.notLonger(name, ForumTag.Companion.MAX_NAME_LENGTH, "Name")
        this.name = name
        return this
    }

    /**
     * Set whether the tag can only be applied by forum moderators.
     *
     * @param  moderated
     * True, if the tag is restricted to moderators
     *
     * @return The updated ForumTagData instance
     *
     * @see .isModerated
     */
    @Nonnull
    fun setModerated(moderated: Boolean): ForumTagData {
        isModerated = moderated
        return this
    }

    /**
     * Set the emoji to use for this tag.
     * <br></br>This emoji is displayed as an icon attached to the tag.
     *
     * @param  emoji
     * The emoji icon of the tag
     *
     * @return The updated ForumTagData instance
     */
    @Nonnull
    fun setEmoji(emoji: Emoji?): ForumTagData {
        this.emoji = emoji
        return this
    }

    override fun getEmoji(): EmojiUnion? {
        return emoji as EmojiUnion?
    }

    @Nonnull
    override fun toData(): DataObject {
        val json = super.toData()
        if (id != 0L) json.put("id", java.lang.Long.toUnsignedString(id))
        return json
    }

    override fun toString(): String {
        return toData().toString()
    }

    companion object {
        /**
         * Creates a new [ForumTagData] instance based on the provided [BaseForumTag].
         * <br></br>This also binds to the id of the provided tag, if available.
         *
         * @param  tag
         * The base tag to use
         *
         * @throws IllegalArgumentException
         * If null is provided or the tag has an invalid name
         *
         * @return The new [ForumTagData] instance
         */
        @Nonnull
        fun from(@Nonnull tag: BaseForumTag): ForumTagData {
            Checks.notNull(tag, "Tag")
            val data = ForumTagData(tag.getName())
                .setEmoji(tag.getEmoji())
                .setModerated(tag.isModerated())
            if (tag is ForumTagSnowflake) data.id = (tag as ForumTagSnowflake).idLong
            return data
        }
    }
}

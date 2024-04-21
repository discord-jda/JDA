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

import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import javax.annotation.Nonnull

/**
 * Information describing a forum tag.
 * <br></br>This is an abstraction used to simplify managing tags.
 *
 * @see ForumTag
 *
 * @see ForumTagData
 *
 * @see ForumTagSnowflake
 */
interface BaseForumTag : SerializableData {
    @JvmField
    @get:Nonnull
    val name: String?

    /**
     * Whether this tag can only be applied by moderators with the [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission (aka Manage Posts).
     *
     * @return True, if this tag can only be applied by moderators with the required permission
     */
    @JvmField
    val isModerated: Boolean

    /**
     * The emoji used as the tag icon.
     * <br></br>**For custom emoji, this will have an empty name and [CustomEmoji.isAnimated] is always `false`, due to discord chicanery.**
     *
     * @return [EmojiUnion] representing the tag emoji, or null if no emoji is applied.
     */
    @JvmField
    val emoji: EmojiUnion?
    @Nonnull
    override fun toData(): DataObject {
        val json = DataObject.empty()
            .put("name", name)
            .put("moderated", isModerated)
        val emoji = emoji
        if (emoji is UnicodeEmoji) json.put(
            "emoji_name",
            emoji.name
        ) else if (emoji is CustomEmoji) json.put("emoji_id", (emoji as CustomEmoji).id)
        return json
    }
}

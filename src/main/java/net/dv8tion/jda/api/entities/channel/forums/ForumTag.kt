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

import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Represents a Discord Forum Tag.
 * <br></br>These tags can be applied to forum posts to help categorize them.
 */
interface ForumTag : ForumTagSnowflake, Comparable<ForumTag>, BaseForumTag {
    /**
     * The tag position, used for sorting.
     *
     * @return The tag position.
     */
    val position: Int
    override fun compareTo(@Nonnull o: ForumTag): Int {
        Checks.notNull(o, "ForumTag")
        return Integer.compare(position, o.position)
    }

    @Nonnull
    override fun toData(): DataObject {
        return super<BaseForumTag>.toData().put("id", id)
    }

    companion object {
        /**
         * The maximum length of a forum tag name ({@value #MAX_NAME_LENGTH})
         */
        const val MAX_NAME_LENGTH = 20
    }
}

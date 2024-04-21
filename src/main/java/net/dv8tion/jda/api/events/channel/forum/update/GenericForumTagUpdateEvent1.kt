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
package net.dv8tion.jda.api.events.channel.forum.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.api.events.UpdateEvent
import net.dv8tion.jda.api.events.channel.forum.GenericForumTagEvent
import javax.annotation.Nonnull

/**
 * Abstraction of all [ForumTag] updates.
 *
 *
 * **Requirements**<br></br>
 * This requires [CacheFlag.FORUM_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS] to be enabled.
 * [JDABuilder.createLight(...)][net.dv8tion.jda.api.JDABuilder.createLight] disables this by default.
 *
 * @param <T>
 * The type of the updated field
</T> */
abstract class GenericForumTagUpdateEvent<T>(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull channel: IPostContainer?, @Nonnull tag: ForumTag?,
    private val previous: T, private val next: T, @get:Nonnull
    @param:Nonnull override val propertyIdentifier: String
) : GenericForumTagEvent(api, responseNumber, channel, tag), UpdateEvent<ForumTag?, T?> {

    @get:Nonnull
    override val entity: E
        get() = getTag()
    override val oldValue: T?
        get() = previous
    override val newValue: T?
        get() = next
}

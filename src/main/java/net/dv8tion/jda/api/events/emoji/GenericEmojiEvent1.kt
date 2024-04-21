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
package net.dv8tion.jda.api.events.emoji

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.api.events.Event
import javax.annotation.Nonnull

/**
 * Indicates that a [Custom Emoji][RichCustomEmoji] was created/removed/updated.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * These events require the [EMOJI][net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] CacheFlag to be enabled, which requires
 * the [GUILD_EMOJIS_AND_STICKERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS_AND_STICKERS] intent.
 *
 * <br></br>[createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disables that CacheFlag by default!
 */
abstract class GenericEmojiEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The affected [RichCustomEmoji] for this event
     *
     * @return The emoji
     */
    @get:Nonnull
    @param:Nonnull val emoji: RichCustomEmoji
) : Event(api, responseNumber) {

    @get:Nonnull
    val guild: Guild?
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] where the emoji came from
         *
         * @return The origin Guild
         */
        get() = emoji.guild
    val isManaged: Boolean
        /**
         * Whether this emoji is managed by an integration
         *
         * @return True, if this emoji is managed by an integration
         */
        get() = emoji.isManaged
}

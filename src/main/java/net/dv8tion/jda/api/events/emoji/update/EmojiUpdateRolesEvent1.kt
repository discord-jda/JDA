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
package net.dv8tion.jda.api.events.emoji.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import javax.annotation.Nonnull

/**
 * Indicates that the role whitelist for a [Custom Emoji][RichCustomEmoji] changed.
 *
 *
 * Can be used to retrieve the old role whitelist
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [EMOJI][net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] CacheFlag to be enabled, which requires
 * the [GUILD_EMOJIS_AND_STICKERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS_AND_STICKERS] intent.
 *
 * <br></br>[createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disables that CacheFlag by default!
 *
 *
 * Identifier: `roles`
 */
class EmojiUpdateRolesEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull emoji: RichCustomEmoji,
    @Nonnull oldRoles: List<Role?>?
) : GenericEmojiUpdateEvent<List<Role?>?>(api, responseNumber, emoji, oldRoles, emoji.roles, IDENTIFIER) {
    @get:Nonnull
    val oldRoles: List<Role>?
        /**
         * The old role whitelist
         *
         * @return The old role whitelist
         */
        get() = oldValue

    @get:Nonnull
    val newRoles: List<Role>?
        /**
         * The new role whitelist
         *
         * @return The new role whitelist
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "roles"
    }
}

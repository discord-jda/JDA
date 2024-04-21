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
package net.dv8tion.jda.api.events.sticker.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.sticker.GuildSticker
import javax.annotation.Nonnull

/**
 * Indicates that the name of a [GuildSticker] changed.
 *
 *
 * Can be used to retrieve the old name
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [STICKER][net.dv8tion.jda.api.utils.cache.CacheFlag.STICKER] CacheFlag to be enabled, which requires
 * the [GUILD_EMOJIS_AND_STICKERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS_AND_STICKERS] intent.
 *
 * <br></br>[createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disables that CacheFlag by default!
 *
 *
 * Identifier: `name`
 */
class GuildStickerUpdateNameEvent(
    @Nonnull api: JDA, responseNumber: Long,
    @Nonnull guild: Guild?, @Nonnull sticker: GuildSticker, @Nonnull oldValue: String
) : GenericGuildStickerUpdateEvent<String?>(api, responseNumber, guild, sticker, IDENTIFIER, oldValue, sticker.name) {
    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "name"
    }
}

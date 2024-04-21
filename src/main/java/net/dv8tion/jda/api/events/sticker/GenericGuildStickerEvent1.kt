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
package net.dv8tion.jda.api.events.sticker

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.sticker.GuildSticker
import net.dv8tion.jda.api.events.Event
import javax.annotation.Nonnull

/**
 * Indicates that an [GuildSticker] was created/removed/updated.
 *
 *
 * **Requirements**
 *
 *
 * These events require the [STICKER][net.dv8tion.jda.api.utils.cache.CacheFlag.STICKER] CacheFlag to be enabled, which requires
 * the [GUILD_EMOJIS_AND_STICKERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS_AND_STICKERS] intent.
 *
 * <br></br>[createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disables that CacheFlag by default!
 */
abstract class GenericGuildStickerEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The [Guild] this sticker belongs to
     *
     * @return The relevant guild
     */
    @get:Nonnull
    @param:Nonnull val guild: Guild,
    /**
     * The relevant [GuildSticker] for this event
     *
     * @return The sticker
     */
    @get:Nonnull
    @param:Nonnull val sticker: GuildSticker
) : Event(api, responseNumber)

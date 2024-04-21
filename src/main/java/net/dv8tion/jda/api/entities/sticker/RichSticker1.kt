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
package net.dv8tion.jda.api.entities.sticker

import javax.annotation.Nonnull

/**
 * Covers more details of stickers which are missing in messages.
 *
 *
 * This is used when stickers are fetched directly from the API or cache, instead of message objects.
 */
interface RichSticker : Sticker {
    @JvmField
    @get:Nonnull
    val type: Sticker.Type?

    @JvmField
    @get:Nonnull
    val tags: Set<String?>?

    @JvmField
    @get:Nonnull
    val description: String?
}

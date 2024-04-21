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

import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.utils.ImageProxy
import javax.annotation.Nonnull

/**
 * A pack of [StandardStickers][StandardSticker] used for nitro.
 */
interface StickerPack : ISnowflake {
    @get:Nonnull
    val stickers: List<StandardSticker?>

    @get:Nonnull
    val name: String?

    @get:Nonnull
    val description: String?

    /**
     * The id of the sticker shown as cover.
     *
     * @return The sticker id for the cover sticker, or `0` if there is no cover
     */
    val coverIdLong: Long
    val coverId: String?
        /**
         * The id of the sticker shown as cover.
         *
         * @return The sticker id for the cover sticker, or `null` if there is no cover
         */
        get() {
            val id = coverIdLong
            return if (id == 0L) null else java.lang.Long.toUnsignedString(id)
        }
    val coverSticker: StandardSticker?
        /**
         * The [StandardSticker] shown as cover.
         *
         * @return The cover sticker, or `null` if there is no cover
         */
        get() {
            val id = coverIdLong
            return if (id == 0L) null else stickers.stream().filter { s: StandardSticker? -> s!!.idLong === id }
                .findFirst().orElse(null)
        }

    /**
     * The id for the pack banner.
     * <br></br>This is shown when you at the top of the pack pop-out in the client.
     *
     * @return The banner id, or `0` if there is no banner
     */
    val bannerIdLong: Long
    val bannerId: String?
        /**
         * The id for the pack banner.
         * <br></br>This is shown when you at the top of the pack pop-out in the client.
         *
         * @return The banner id, or `null` if there is no banner
         */
        get() {
            val id = bannerIdLong
            return if (id == 0L) null else java.lang.Long.toUnsignedString(id)
        }
    val bannerUrl: String?
        /**
         * The url for the pack banner.
         * <br></br>This is shown when you at the top of the pack pop-out in the client.
         *
         * @return The banner id, or `null` if there is no banner
         */
        get() {
            val bannerId = bannerId
            return if (bannerId == null) null else String.format(BANNER_URL, bannerId, "png")
        }
    val banner: ImageProxy?
        /**
         * The [ImageProxy] for the pack banner.
         * <br></br>This is shown when you at the top of the pack pop-out in the client.
         *
         * @return The banner proxy, or `null` if there is no banner
         */
        get() {
            val url = bannerUrl
            return url?.let { ImageProxy(it) }
        }

    /**
     * The stock-keeping unit (SKU) for this sticker pack.
     * <br></br>This is used for store purchases, if there was a store to buy the pack from.
     *
     * @return The SKU id for this pack, or `0` if there is no SKU
     */
    val skuIdLong: Long
    val skuId: String?
        /**
         * The stock-keeping unit (SKU) for this sticker pack.
         * <br></br>This is used for store purchases, if there was a store to buy the pack from.
         *
         * @return The SKU id for this pack, or `null` if there is no SKU
         */
        get() {
            val id = skuIdLong
            return if (id == 0L) null else java.lang.Long.toUnsignedString(id)
        }

    companion object {
        /**
         * Format string used for [.getBannerUrl].
         * <br></br>The parameters of the format string are the [.getBannerId] and the file extension (png).
         */
        const val BANNER_URL = "https://cdn.discordapp.com/app-assets/710982414301790216/store/%s.%s"
    }
}

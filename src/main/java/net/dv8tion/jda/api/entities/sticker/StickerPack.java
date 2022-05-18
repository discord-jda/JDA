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

package net.dv8tion.jda.api.entities.sticker;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A pack of {@link StandardSticker StandardStickers} used for nitro.
 */
public interface StickerPack extends ISnowflake
{
    /**
     * Format string used for {@link #getBannerUrl()}.
     * <br>The parameters of the format string are the {@link #getBannerId()} and the file extension (png).
     */
    String BANNER_URL = "https://cdn.discordapp.com/app-assets/710982414301790216/store/%s.%s";

    /**
     * The {@link StandardSticker StandardStickers} in this pack.
     *
     * @return Immutable List of {@link StandardSticker StandardStickers}
     */
    @Nonnull
    List<StandardSticker> getStickers();

    /**
     * The name of this pack.
     *
     * @return Pack name
     */
    @Nonnull
    String getName();

    /**
     * The description of the sticker pack
     *
     * @return The description
     */
    @Nonnull
    String getDescription();

    /**
     * The id of the sticker shown as cover.
     *
     * @return The sticker id for the cover sticker, or {@code 0} if there is no cover
     */
    long getCoverIdLong();

    /**
     * The id of the sticker shown as cover.
     *
     * @return The sticker id for the cover sticker, or {@code null} if there is no cover
     */
    @Nullable
    default String getCoverId()
    {
        long id = getCoverIdLong();
        return id == 0 ? null : Long.toUnsignedString(id);
    }

    /**
     * The {@link StandardSticker} shown as cover.
     *
     * @return The cover sticker, or {@code null} if there is no cover
     */
    @Nullable
    default StandardSticker getCoverSticker()
    {
        long id = getCoverIdLong();
        if (id == 0L)
            return null;
        return getStickers().stream().filter(s -> s.getIdLong() == id).findFirst().orElse(null);
    }

    /**
     * The id for the pack banner.
     * <br>This is shown when you at the top of the pack pop-out in the client.
     *
     * @return The banner id, or {@code 0} if there is no banner
     */
    long getBannerIdLong();

    /**
     * The id for the pack banner.
     * <br>This is shown when you at the top of the pack pop-out in the client.
     *
     * @return The banner id, or {@code null} if there is no banner
     */
    @Nullable
    default String getBannerId()
    {
        long id = getBannerIdLong();
        return id == 0 ? null : Long.toUnsignedString(id);
    }

    /**
     * The url for the pack banner.
     * <br>This is shown when you at the top of the pack pop-out in the client.
     *
     * @return The banner id, or {@code null} if there is no banner
     */
    @Nullable
    default String getBannerUrl()
    {
        String bannerId = getBannerId();
        return bannerId == null ? null : String.format(BANNER_URL, bannerId, "png");
    }

    /**
     * The {@link ImageProxy} for the pack banner.
     * <br>This is shown when you at the top of the pack pop-out in the client.
     *
     * @return The banner proxy, or {@code null} if there is no banner
     */
    @Nullable
    default ImageProxy getBanner()
    {
        String url = getBannerUrl();
        return url == null ? null : new ImageProxy(url);
    }

    /**
     * The stock-keeping unit (SKU) for this sticker pack.
     * <br>This is used for store purchases, if there was a store to buy the pack from.
     *
     * @return The SKU id for this pack, or {@code 0} if there is no SKU
     */
    long getSkuIdLong();


    /**
     * The stock-keeping unit (SKU) for this sticker pack.
     * <br>This is used for store purchases, if there was a store to buy the pack from.
     *
     * @return The SKU id for this pack, or {@code null} if there is no SKU
     */
    @Nullable
    default String getSkuId()
    {
        long id = getSkuIdLong();
        return id == 0 ? null : Long.toUnsignedString(id);
    }
}

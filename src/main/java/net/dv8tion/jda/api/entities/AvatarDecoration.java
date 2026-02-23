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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.utils.DiscordAssets;
import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.internal.utils.EntityString;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Represents an avatar decoration of a {@link User} or {@link Member}.
 */
public class AvatarDecoration {

    private final String assetId;
    private final String skuId;

    public AvatarDecoration(@Nonnull String assetId, @Nonnull String skuId) {
        this.assetId = assetId;
        this.skuId = skuId;
    }

    /**
     * The SKU ID of the avatar decoration.
     *
     * @return The SKU ID of the avatar decoration.
     */
    @Nonnull
    public String getSkuId() {
        return skuId;
    }

    /**
     * Returns the ID of this avatar decoration.
     *
     * @return The ID of this avatar decoration.
     */
    @Nonnull
    public String getAssetId() {
        return assetId;
    }

    /**
     * The URL for the avatar decoration.
     *
     * @return The avatar decoration's URL.
     *
     * @see    DiscordAssets#avatarDecoration(ImageFormat, String)
     */
    @Nonnull
    public String getAssetUrl() {
        return getAssetProxy().getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this decoration avatar.
     *
     * @return {@link ImageProxy} of this decoration avatar
     *
     * @see    #getAssetUrl()
     * @see    DiscordAssets#avatarDecoration(ImageFormat, String)
     */
    @Nonnull
    public ImageProxy getAssetProxy() {
        return DiscordAssets.avatarDecoration(ImageFormat.PNG, assetId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AvatarDecoration)) {
            return false;
        }
        AvatarDecoration that = (AvatarDecoration) o;
        return assetId.equals(that.assetId) && skuId.equals(that.skuId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, skuId);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("assetId", assetId)
                .addMetadata("skuId", skuId)
                .toString();
    }
}

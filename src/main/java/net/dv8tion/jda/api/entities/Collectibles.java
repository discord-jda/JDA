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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The collectibles a user has equipped.
 *
 * <p>This excludes avatar decorations and profile effects.
 */
public interface Collectibles {
    /**
     * Returns the equipped nameplate.
     *
     * @return The displayed nameplate
     */
    @Nullable
    Nameplate getNameplate();

    /**
     * A decoration visible in Direct Messages and a guild's member list.
     */
    interface Nameplate {
        /**
         * Returns the SKU ID of this nameplate.
         *
         * <p>This is unique and will never change.
         *
         * @return The SKU ID
         */
        @Nonnull
        String getSkuId();

        /**
         * Returns the asset path of this nameplate.
         *
         * <p>This is unique but not necessarily immutable.
         *
         * @return The asset path
         */
        @Nonnull
        String getAssetPath();

        /**
         * Returns a URL of this nameplate, as an static asset.
         *
         * <p>Size parameters are ignored by this endpoint.
         *
         * @return The URL to this nameplate's static asset
         *
         * @see    #getAnimatedAssetUrl()
         * @see    #getStaticAsset()
         */
        @Nonnull
        default String getStaticAssetUrl() {
            return getStaticAsset().getUrl();
        }

        /**
         * Returns an {@link ImageProxy} of this nameplate, as a static asset.
         *
         * <p>Size parameters are ignored by this endpoint.
         *
         * @return The proxy to this nameplate's static asset
         *
         * @see    #getAnimatedAsset()
         * @see    #getStaticAssetUrl()
         */
        @Nonnull
        default ImageProxy getStaticAsset() {
            return DiscordAssets.staticNameplate(ImageFormat.PNG, getAssetPath());
        }

        /**
         * Returns a URL of this nameplate, as an animated asset.
         *
         * <p>Size parameters are ignored by this endpoint.
         *
         * @return The URL to this nameplate's animated asset
         *
         * @see    #getStaticAssetUrl()
         * @see    #getAnimatedAsset()
         */
        @Nonnull
        default String getAnimatedAssetUrl() {
            return getAnimatedAsset().getUrl();
        }

        /**
         * Returns an {@link ImageProxy} of this nameplate, as an animated asset.
         *
         * <p>Size parameters are ignored by this endpoint.
         *
         * @return The proxy to this nameplate's animated asset
         *
         * @see    #getStaticAsset()
         * @see    #getAnimatedAssetUrl()
         */
        @Nonnull
        default ImageProxy getAnimatedAsset() {
            return DiscordAssets.animatedNameplate(ImageFormat.WEBM, getAssetPath());
        }

        /**
         * Returns the name of the background color of this nameplate.
         *
         * @return Palette name of this nameplate's background color
         *
         * @see   <a href="https://docs.discord.com/developers/resources/user#nameplate" target="_blank">Official documentation for nameplates</a>
         */
        @Nonnull
        String getPalette();
    }
}

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

package net.dv8tion.jda.api.entities.channel.concrete;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.detached.IDetachableEntity;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.DiscordAssets;
import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Group DM channel.
 *
 * <p>This is only used for user-installed apps.
 */
public interface GroupChannel extends MessageChannel, IDetachableEntity {
    /**
     * Template for {@link #getIconUrl()}.
     *
     * @deprecated Replaced by {@link DiscordAssets#channelIcon(ImageFormat, String, String)}
     */
    @Deprecated
    String ICON_URL = "https://cdn.discordapp.com/channel-icons/%s/%s.png";

    /**
     * The Discord hash-id of the group channel icon image.
     * If no icon has been set, this returns {@code null}.
     *
     * @return Possibly-null String containing the group channel's icon hash-id.
     */
    @Nullable
    String getIconId();

    /**
     * The URL of the group channel icon image.
     * If no icon has been set, this returns {@code null}.
     *
     * @return Possibly-null String containing the group channel's icon URL.
     *
     * @deprecated Replaced by {@link #getIconUrl(ImageFormat)}
     */
    @Nullable
    @Deprecated
    default String getIconUrl() {
        String iconId = getIconId();
        return iconId == null ? null : Helpers.format(ICON_URL, getId(), iconId);
    }

    /**
     * The URL of the group channel icon image.
     * If no icon has been set, this returns {@code null}.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null String containing the group channel's icon URL.
     *
     * @see    DiscordAssets#channelIcon(ImageFormat, String, String)
     */
    @Nullable
    default String getIconUrl(@Nonnull ImageFormat format) {
        ImageProxy icon = getIcon(format);
        return icon == null ? null : icon.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this group channel's icon.
     *
     * @return Possibly-null {@link ImageProxy} of this group channel's icon
     *
     * @deprecated Replaced by {@link #getIcon(ImageFormat)}
     *
     * @see    #getIconUrl()
     */
    @Nullable
    @Deprecated
    default ImageProxy getIcon() {
        String iconUrl = getIconUrl();
        return iconUrl == null ? null : new ImageProxy(iconUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this group channel's icon.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this group channel's icon
     *
     * @see    #getIconUrl(ImageFormat)
     * @see    DiscordAssets#channelIcon(ImageFormat, String, String)
     */
    @Nullable
    default ImageProxy getIcon(@Nonnull ImageFormat format) {
        return DiscordAssets.channelIcon(format, getId(), getIconId());
    }

    /**
     * Returns the ID of the user which owns this {@link GroupChannel}.
     *
     * @return The ID of the user which owns this {@link GroupChannel}
     */
    long getOwnerIdLong();

    /**
     * Returns the ID of the user which owns this {@link GroupChannel}.
     *
     * @return The ID of the user which owns this {@link GroupChannel}
     */
    @Nonnull
    default String getOwnerId() {
        return Long.toUnsignedString(getOwnerIdLong());
    }

    /**
     * Retrieves the {@link User} which owns this {@link GroupChannel}.
     *
     * @return A {@link RestAction} to retrieve the {@link User User} which owns this {@link GroupChannel}.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<User> retrieveOwner() {
        return getJDA().retrieveUserById(getOwnerIdLong());
    }
}

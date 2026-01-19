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

package net.dv8tion.jda.api.events.self;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.DiscordAssets;
import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the avatar of the current user changed.
 *
 * <p>Can be used to retrieve the old avatar.
 *
 * <p>Identifier: {@code avatar}
 */
public class SelfUpdateAvatarEvent extends GenericSelfUpdateEvent<String> {
    public static final String IDENTIFIER = "avatar";
    private static final String AVATAR_URL = "https://cdn.discordapp.com/avatars/%s/%s%s";

    public SelfUpdateAvatarEvent(@Nonnull JDA api, long responseNumber, @Nullable String oldAvatarId) {
        super(api, responseNumber, oldAvatarId, api.getSelfUser().getAvatarId(), IDENTIFIER);
    }

    /**
     * The old avatar id
     *
     * @return The old avatar id
     */
    @Nullable
    public String getOldAvatarId() {
        return getOldValue();
    }

    /**
     * The old avatar url
     *
     * @return  The old avatar url
     */
    @Nullable
    public String getOldAvatarUrl() {
        return previous == null
                ? null
                : String.format(
                        AVATAR_URL, getSelfUser().getId(), previous, previous.startsWith("a_") ? ".gif" : ".png");
    }

    /**
     * The old avatar url
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return  The old avatar url
     */
    @Nullable
    public String getOldAvatarUrl(@Nonnull ImageFormat format) {
        ImageProxy proxy = getOldAvatar(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this bot's new avatar image.
     * <p>
     * <b>Note:</b> the old avatar may not always be downloadable as it might have been removed from Discord.
     *
     * @return Possibly-null {@link ImageProxy} of this bot's new avatar image
     *
     * @see    #getOldAvatarUrl()
     */
    @Nullable
    public ImageProxy getOldAvatar() {
        String oldAvatarUrl = getOldAvatarUrl();
        return oldAvatarUrl == null ? null : new ImageProxy(oldAvatarUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this bot's new avatar image.
     * <p>
     * <b>Note:</b> the old avatar may not always be downloadable as it might have been removed from Discord.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this bot's new avatar image
     *
     * @see    #getOldAvatarUrl()
     */
    @Nullable
    public ImageProxy getOldAvatar(@Nonnull ImageFormat format) {
        return DiscordAssets.userAvatar(format, getSelfUser().getId(), previous);
    }

    /**
     * The new avatar id
     *
     * @return The new avatar id
     */
    @Nullable
    public String getNewAvatarId() {
        return getNewValue();
    }

    /**
     * The new avatar url
     *
     * @return  The new avatar url
     */
    @Nullable
    public String getNewAvatarUrl() {
        return next == null
                ? null
                : String.format(AVATAR_URL, getSelfUser().getId(), next, next.startsWith("a_") ? ".gif" : ".png");
    }

    /**
     * The new avatar url
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return  The new avatar url
     */
    @Nullable
    public String getNewAvatarUrl(@Nonnull ImageFormat format) {
        ImageProxy proxy = getNewAvatar(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this bot's new avatar image.
     *
     * @return Possibly-null {@link ImageProxy} of this bot's new avatar image
     *
     * @see    #getNewAvatarUrl()
     */
    @Nullable
    public ImageProxy getNewAvatar() {
        String newAvatarUrl = getNewAvatarUrl();
        return newAvatarUrl == null ? null : new ImageProxy(newAvatarUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this bot's new avatar image.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this bot's new avatar image
     *
     * @see    #getNewAvatarUrl()
     */
    @Nullable
    public ImageProxy getNewAvatar(@Nonnull ImageFormat format) {
        return DiscordAssets.userAvatar(format, getSelfUser().getId(), next);
    }
}

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

package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.DiscordAssets;
import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the Avatar of a {@link net.dv8tion.jda.api.entities.User User} changed.
 *
 * <p>Can be used to retrieve the User who changed their avatar and their previous Avatar ID/URL.
 *
 * <p>Identifier: {@code avatar}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event requires the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class UserUpdateAvatarEvent extends GenericUserUpdateEvent<String> {
    public static final String IDENTIFIER = "avatar";

    public UserUpdateAvatarEvent(
            @Nonnull JDA api, long responseNumber, @Nonnull User user, @Nullable String oldAvatar) {
        super(api, responseNumber, user, oldAvatar, user.getAvatarId(), IDENTIFIER);
    }

    /**
     * The previous avatar id
     *
     * @return The previous avatar id
     */
    @Nullable
    public String getOldAvatarId() {
        return getOldValue();
    }

    /**
     * The previous avatar url
     *
     * @return The previous avatar url
     *
     * @deprecated Replaced by {@link #getOldAvatarUrl(ImageFormat)}
     */
    @Nullable
    @Deprecated
    public String getOldAvatarUrl() {
        return previous == null
                ? null
                : String.format(
                        User.AVATAR_URL, getUser().getId(), previous, previous.startsWith("a_") ? "gif" : "png");
    }

    /**
     * The previous avatar url
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The previous avatar url
     */
    @Nullable
    public String getOldAvatarUrl(@Nonnull ImageFormat format) {
        ImageProxy proxy = getOldAvatar(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this user's old avatar image.
     * <p>
     * <b>Note:</b> the old avatar may not always be downloadable as it might have been removed from Discord.
     *
     * @return Possibly-null {@link ImageProxy} of this user's old avatar image
     *
     * @deprecated Replaced by {@link #getOldAvatar(ImageFormat)}
     *
     * @see    #getOldAvatarUrl()
     */
    @Nullable
    @Deprecated
    public ImageProxy getOldAvatar() {
        String oldAvatarUrl = getOldAvatarUrl();
        return oldAvatarUrl == null ? null : new ImageProxy(oldAvatarUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this user's old avatar image.
     * <p>
     * <b>Note:</b> the old avatar may not always be downloadable as it might have been removed from Discord.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this user's old avatar image
     *
     * @see    #getOldAvatarUrl(ImageFormat)
     */
    @Nullable
    public ImageProxy getOldAvatar(@Nonnull ImageFormat format) {
        return DiscordAssets.userAvatar(format, getUser().getId(), previous);
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
     * The url of the new avatar
     *
     * @return The url of the new avatar
     *
     * @deprecated Replaced by {@link #getNewAvatarUrl(ImageFormat)}
     */
    @Nullable
    @Deprecated
    public String getNewAvatarUrl() {
        return next == null
                ? null
                : String.format(User.AVATAR_URL, getUser().getId(), next, next.startsWith("a_") ? "gif" : "png");
    }

    /**
     * The url of the new avatar
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The url of the new avatar
     */
    @Nullable
    public String getNewAvatarUrl(@Nonnull ImageFormat format) {
        ImageProxy proxy = getNewAvatar(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this user's new avatar image.
     *
     * @return Possibly-null {@link ImageProxy} of this user's new avatar image
     *
     * @deprecated Replaced by {@link #getNewAvatar(ImageFormat)}
     *
     * @see    #getNewAvatarUrl()
     */
    @Nullable
    @Deprecated
    public ImageProxy getNewAvatar() {
        String newAvatarUrl = getNewAvatarUrl();
        return newAvatarUrl == null ? null : new ImageProxy(newAvatarUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this user's new avatar image.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this user's new avatar image
     *
     * @see    #getNewAvatarUrl(ImageFormat)
     */
    @Nullable
    public ImageProxy getNewAvatar(@Nonnull ImageFormat format) {
        return DiscordAssets.userAvatar(format, getUser().getId(), next);
    }
}

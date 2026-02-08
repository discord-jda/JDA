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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class to retrieve an {@link ImageProxy} of most Discord assets.
 */
public final class DiscordAssets {
    private DiscordAssets() {}

    /**
     * Returns an {@link ImageProxy} of an application's icon.
     * <br>This returns {@code null} if the icon ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  applicationId
     *         The application ID
     * @param  iconId
     *         The application's icon ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the icon ID
     *
     * @return An {@link ImageProxy} of the application's icon, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy applicationIcon(
            @Nonnull ImageFormat format, @Nonnull String applicationId, @Nullable String iconId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(applicationId, "Application ID");
        if (iconId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("app-icons").addPathSegment(applicationId);
        return format.finishProxy(builder, iconId);
    }

    /**
     * Returns an {@link ImageProxy} of an application's cover.
     * <br>This returns {@code null} if the cover ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  applicationId
     *         The application ID
     * @param  coverId
     *         The application's cover ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the cover ID
     *
     * @return An {@link ImageProxy} of the application's cover, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy applicationCover(
            @Nonnull ImageFormat format, @Nonnull String applicationId, @Nullable String coverId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(applicationId, "Application ID");
        if (coverId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("application").addPathSegment(applicationId);
        return format.finishProxy(builder, coverId);
    }

    /**
     * Returns an {@link ImageProxy} of an application team's icon.
     * <br>This returns {@code null} if the icon ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  teamId
     *         The team ID
     * @param  iconId
     *         The team's icon ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the icon ID
     *
     * @return An {@link ImageProxy} of the application team's icon, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy applicationTeamIcon(
            @Nonnull ImageFormat format, @Nonnull String teamId, @Nullable String iconId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(teamId, "Team ID");
        if (iconId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("team-icons").addPathSegment(teamId);
        return format.finishProxy(builder, iconId);
    }

    /**
     * Returns an {@link ImageProxy} of a channel's icon.
     * <br>This returns {@code null} if the icon ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  channelId
     *         The channel ID
     * @param  iconId
     *         The team's icon ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the icon ID
     *
     * @return An {@link ImageProxy} of the channel's icon, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy channelIcon(
            @Nonnull ImageFormat format, @Nonnull String channelId, @Nullable String iconId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(channelId, "Channel ID");
        if (iconId == null) {
            return null;
        }

        HttpUrl.Builder builder =
                newUrl().addEncodedPathSegment("channel-icons").addPathSegment(channelId);
        return format.finishProxy(builder, iconId);
    }

    /**
     * Returns an {@link ImageProxy} of a custom emoji's icon.
     * <br>This returns {@code null} if the ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     *     <li>{@link ImageFormat#ANIMATED_WEBP ANIMATED_WEBP}</li>
     *     <li>{@link ImageFormat#GIF GIF}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  id
     *         The custom emoji's ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the ID
     *
     * @return An {@link ImageProxy} of the custom emoji's icon, or {@code null}
     */
    @Nonnull
    public static ImageProxy customEmoji(@Nonnull ImageFormat format, @Nonnull String id) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(id, "ID");

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("emojis");
        return format.finishProxy(builder, id);
    }

    /**
     * Returns an {@link ImageProxy} of a guild's icon.
     * <br>This returns {@code null} if the icon ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     *     <li>{@link ImageFormat#ANIMATED_WEBP ANIMATED_WEBP}</li>
     *     <li>{@link ImageFormat#GIF GIF}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  guildId
     *         The guild ID
     * @param  iconId
     *         The guild's icon ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the icon ID
     *
     * @return An {@link ImageProxy} of the guild's icon, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy guildIcon(@Nonnull ImageFormat format, @Nonnull String guildId, @Nullable String iconId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(guildId, "Guild ID");
        if (iconId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("icons").addPathSegment(guildId);
        return format.finishProxy(builder, iconId);
    }

    /**
     * Returns an {@link ImageProxy} of a guild's splash image.
     * <br>This returns {@code null} if the splash ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  guildId
     *         The guild ID
     * @param  splashId
     *         The guild's splash ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the splash ID
     *
     * @return An {@link ImageProxy} of the guild's splash image, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy guildSplash(
            @Nonnull ImageFormat format, @Nonnull String guildId, @Nullable String splashId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(guildId, "Guild ID");
        if (splashId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("splashes").addPathSegment(guildId);
        return format.finishProxy(builder, splashId);
    }

    /**
     * Returns an {@link ImageProxy} of a guild's banner.
     * <br>This returns {@code null} if the banner ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     *     <li>{@link ImageFormat#ANIMATED_WEBP ANIMATED_WEBP}</li>
     *     <li>{@link ImageFormat#GIF GIF}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  guildId
     *         The guild ID
     * @param  bannerId
     *         The guild's banner ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the banner ID
     *
     * @return An {@link ImageProxy} of the guild's banner, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy guildBanner(
            @Nonnull ImageFormat format, @Nonnull String guildId, @Nullable String bannerId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(guildId, "Guild ID");
        if (bannerId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("banners").addPathSegment(guildId);
        return format.finishProxy(builder, bannerId);
    }

    /**
     * Returns an {@link ImageProxy} of a member's avatar.
     * <br>This returns {@code null} if the avatar ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     *     <li>{@link ImageFormat#ANIMATED_WEBP ANIMATED_WEBP}</li>
     *     <li>{@link ImageFormat#GIF GIF}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  guildId
     *         The guild ID
     * @param  userId
     *         The user ID
     * @param  avatarId
     *         The member's avatar ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the avatar ID
     *
     * @return An {@link ImageProxy} of the member's avatar, or {@code null}
     */
    @Contract("_, _, _, null -> null; _, _, _, !null -> !null")
    public static ImageProxy memberAvatar(
            @Nonnull ImageFormat format, @Nonnull String guildId, @Nonnull String userId, @Nullable String avatarId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(guildId, "Guild ID");
        Checks.isSnowflake(userId, "User ID");
        if (avatarId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("guilds")
                .addPathSegment(guildId)
                .addEncodedPathSegment("users")
                .addPathSegment(userId)
                .addEncodedPathSegment("avatars");
        return format.finishProxy(builder, avatarId);
    }

    /**
     * Returns an {@link ImageProxy} of a role's icon.
     * <br>This returns {@code null} if the icon ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  roleId
     *         The role ID
     * @param  iconId
     *         The icon ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the icon ID
     *
     * @return An {@link ImageProxy} of the roles's icon, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy roleIcon(@Nonnull ImageFormat format, @Nonnull String roleId, @Nullable String iconId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(roleId, "Role ID");
        if (iconId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("role-icons").addPathSegment(roleId);
        return format.finishProxy(builder, iconId);
    }

    /**
     * Returns an {@link ImageProxy} of a scheduled event's cover image.
     * <br>This returns {@code null} if the avatar ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  eventId
     *         The event ID
     * @param  imageId
     *         The event's image ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the image ID
     *
     * @return An {@link ImageProxy} of the scheduled event's cover image, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy scheduledEventCoverImage(
            @Nonnull ImageFormat format, @Nonnull String eventId, @Nullable String imageId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(eventId, "Event ID");
        if (imageId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("guild-events").addPathSegment(eventId);
        return format.finishProxy(builder, imageId);
    }

    /**
     * Returns an {@link ImageProxy} of a sticker pack's banner.
     * <br>This returns {@code null} if the banner ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  bannerId
     *         The sticker pack's banner ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the banner ID
     *
     * @return An {@link ImageProxy} of the sticker pack's banner, or {@code null}
     */
    @Contract("_, null -> null; _, !null -> !null")
    public static ImageProxy stickerPackBanner(@Nonnull ImageFormat format, @Nullable String bannerId) {
        Checks.notNull(format, "Format");
        if (bannerId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("app-assets")
                .addEncodedPathSegment("710982414301790216")
                .addEncodedPathSegment("store");
        return format.finishProxy(builder, bannerId);
    }

    /**
     * Returns an {@link ImageProxy} of a user's avatar.
     * <br>This returns {@code null} if the avatar ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     *     <li>{@link ImageFormat#ANIMATED_WEBP ANIMATED_WEBP}</li>
     *     <li>{@link ImageFormat#GIF GIF}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  userId
     *         The user ID
     * @param  avatarId
     *         The user's avatar ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the avatar ID
     *
     * @return An {@link ImageProxy} of the user's avatar, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy userAvatar(
            @Nonnull ImageFormat format, @Nonnull String userId, @Nullable String avatarId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(userId, "User ID");
        if (avatarId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("avatars").addPathSegment(userId);
        return format.finishProxy(builder, avatarId);
    }

    /**
     * Returns an {@link ImageProxy} of a user's banner.
     * <br>This returns {@code null} if the banner ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     *     <li>{@link ImageFormat#ANIMATED_WEBP ANIMATED_WEBP}</li>
     *     <li>{@link ImageFormat#GIF GIF}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  userId
     *         The user ID
     * @param  bannerId
     *         The user's banner ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the banner ID
     *
     * @return An {@link ImageProxy} of the user's banner, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy userBanner(
            @Nonnull ImageFormat format, @Nonnull String userId, @Nullable String bannerId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(userId, "User ID");
        if (bannerId == null) {
            return null;
        }

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("banners").addPathSegment(userId);
        return format.finishProxy(builder, bannerId);
    }

    /**
     * Returns an {@link ImageProxy} of a user's default avatar.
     *
     * <p>At the time of writing, the only supported format is {@link ImageFormat#PNG PNG}.
     *
     * <p>Size parameters are ignored by this endpoint.
     *
     * @param  avatarId
     *         The user's default avatar ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}
     *
     * @return An {@link ImageProxy} of the user's default avatar
     */
    @Nonnull
    public static ImageProxy userDefaultAvatar(@Nonnull ImageFormat format, @Nonnull String avatarId) {
        Checks.notNull(format, "Format");
        Checks.notNull(avatarId, "Avatar ID");

        HttpUrl.Builder builder = newUrl().addEncodedPathSegment("embed").addEncodedPathSegment("avatars");
        return format.finishProxy(builder, avatarId);
    }

    /**
     * Returns an {@link ImageProxy} of a user's tag badge.
     * <br>This returns {@code null} if the badge ID is {@code null}.
     *
     * <p>At the time of writing, the supported formats are:
     * <ul>
     *     <li>{@link ImageFormat#PNG PNG}</li>
     *     <li>{@link ImageFormat#JPG JPG}</li>
     *     <li>{@link ImageFormat#STATIC_WEBP STATIC_WEBP}</li>
     * </ul>
     *
     * @param  format
     *         The image format to request the image as
     * @param  guildId
     *         The guild ID
     * @param  badgeId
     *         The badge ID
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}, except for the badge ID
     *
     * @return An {@link ImageProxy} of the user's tag badge, or {@code null}
     */
    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy userTagBadge(
            @Nonnull ImageFormat format, @Nonnull String guildId, @Nullable String badgeId) {
        Checks.notNull(format, "Format");
        Checks.isSnowflake(guildId, "Guild ID");
        if (badgeId == null) {
            return null;
        }

        HttpUrl.Builder builder =
                newUrl().addEncodedPathSegment("guild-tag-badges").addPathSegment(guildId);
        return format.finishProxy(builder, badgeId);
    }

    private static HttpUrl.Builder newUrl() {
        return new HttpUrl.Builder().scheme("https").host("cdn.discordapp.com");
    }
}

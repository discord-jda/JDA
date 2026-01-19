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

package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

public final class DiscordAssets {
    private static final String APPLICATION_ICON_URL = "https://cdn.discordapp.com/app-icons/%s/%s";
    private static final String APPLICATION_COVER_URL = "https://cdn.discordapp.com/application/%s/%s";

    private static final String APPLICATION_TEAM_ICON_URL = "https://cdn.discordapp.com/team-icons/%s/%s";

    private static final String CHANNEL_ICON_URL = "https://cdn.discordapp.com/channel-icons/%s/%s";

    private static final String CUSTOM_EMOJI_ICON_URL = "https://cdn.discordapp.com/emojis/%s";

    private static final String GUILD_ICON_URL = "https://cdn.discordapp.com/icons/%s/%s";
    private static final String GUILD_SPLASH_URL = "https://cdn.discordapp.com/splashes/%s/%s";
    private static final String GUILD_BANNER_URL = "https://cdn.discordapp.com/banners/%s/%s";

    private static final String MEMBER_AVATAR_URL = "https://cdn.discordapp.com/guilds/%s/users/%s/avatars/%s";

    private static final String ROLE_ICON_URL = "https://cdn.discordapp.com/role-icons/%s/%s";

    private static final String SCHEDULED_EVENT_IMAGE_URL = "https://cdn.discordapp.com/guild-events/%s/%s";

    private static final String STICKER_PACK_BANNER_URL =
            "https://cdn.discordapp.com/app-assets/710982414301790216/store/%s";

    private static final String USER_AVATAR_URL = "https://cdn.discordapp.com/avatars/%s/%s";
    private static final String USER_BANNER_URL = "https://cdn.discordapp.com/banners/%s/%s";
    private static final String USER_DEFAULT_AVATAR_URL = "https://cdn.discordapp.com/embed/avatars/%s";
    private static final String USER_TAG_BADGE_URL = "https://cdn.discordapp.com/guild-tag-badges/%s/%s";

    private DiscordAssets() {}

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy applicationIcon(
            @Nonnull ImageFormat format, @Nonnull String applicationId, String iconId) {
        if (iconId == null) {
            return null;
        }
        return createProxy(format, APPLICATION_ICON_URL, applicationId, iconId);
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy applicationCover(
            @Nonnull ImageFormat format, @Nonnull String applicationId, String coverId) {
        if (coverId == null) {
            return null;
        }
        return createProxy(format, APPLICATION_COVER_URL, applicationId, coverId);
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy applicationTeamIcon(@Nonnull ImageFormat format, @Nonnull String teamId, String iconId) {
        if (iconId == null) {
            return null;
        }
        return createProxy(format, APPLICATION_TEAM_ICON_URL, teamId, iconId);
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy channelIcon(@Nonnull ImageFormat format, @Nonnull String channelId, String iconId) {
        if (iconId == null) {
            return null;
        }
        return createProxy(format, CHANNEL_ICON_URL, channelId, iconId);
    }

    @Nonnull
    public static ImageProxy customEmojiIcon(@Nonnull ImageFormat format, @Nonnull String id) {
        return createProxy(format, CUSTOM_EMOJI_ICON_URL, id);
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy guildIcon(@Nonnull ImageFormat format, @Nonnull String guildId, String iconId) {
        if (iconId == null) {
            return null;
        }
        return createProxy(format, GUILD_ICON_URL, guildId, iconId);
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy guildSplash(@Nonnull ImageFormat format, @Nonnull String guildId, String splashId) {
        if (splashId == null) {
            return null;
        }
        return createProxy(format, GUILD_SPLASH_URL, guildId, splashId);
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy guildBanner(@Nonnull ImageFormat format, @Nonnull String guildId, String bannerId) {
        if (bannerId == null) {
            return null;
        }
        return createProxy(format, GUILD_BANNER_URL, guildId, bannerId);
    }

    @Contract("_, _, _, null -> null; _, _, _, !null -> !null")
    public static ImageProxy memberAvatar(
            @Nonnull ImageFormat format, @Nonnull String guildId, @Nonnull String userId, String avatarId) {
        if (avatarId == null) {
            return null;
        }
        return createProxy(format, MEMBER_AVATAR_URL, guildId, userId, avatarId);
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy roleIcon(@Nonnull ImageFormat format, @Nonnull String roleId, String iconId) {
        if (iconId == null) {
            return null;
        }
        return createProxy(format, ROLE_ICON_URL, roleId, iconId);
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy scheduledEventImage(@Nonnull ImageFormat format, @Nonnull String eventId, String imageId) {
        if (imageId == null) {
            return null;
        }
        return createProxy(format, SCHEDULED_EVENT_IMAGE_URL, eventId, imageId);
    }

    @Contract("_, null -> null; _, !null -> !null")
    public static ImageProxy stickerPackBanner(@Nonnull ImageFormat format, String bannerId) {
        if (bannerId == null) {
            return null;
        }
        return createProxy(format, STICKER_PACK_BANNER_URL, bannerId);
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy userAvatar(@Nonnull ImageFormat format, @Nonnull String userId, String avatarId) {
        if (avatarId == null) {
            return null;
        }
        return createProxy(format, USER_AVATAR_URL, userId, avatarId);
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy userBanner(@Nonnull ImageFormat format, @Nonnull String userId, String bannerId) {
        if (bannerId == null) {
            return null;
        }
        return createProxy(format, USER_BANNER_URL, userId, bannerId);
    }

    @Nonnull
    public static ImageProxy userDefaultAvatar(@Nonnull String avatarId) {
        String base = String.format(USER_DEFAULT_AVATAR_URL, avatarId);
        return new ImageProxy(base + ".png");
    }

    @Contract("_, _, null -> null; _, _, !null -> !null")
    public static ImageProxy userTagBadge(@Nonnull ImageFormat format, @Nonnull String guildId, String badgeId) {
        if (badgeId == null) {
            return null;
        }
        return createProxy(format, USER_TAG_BADGE_URL, guildId, badgeId);
    }

    private static ImageProxy createProxy(ImageFormat imageFormat, String format, String... args) {
        Checks.notNull(imageFormat, "Format");
        String base = String.format(format, (Object[]) args);
        String url = base + "." + imageFormat.getExtension() + "?" + String.join("&", imageFormat.getQueryParameters());
        return new ImageProxy(url);
    }
}

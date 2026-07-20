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

package net.dv8tion.jda.api.events.guild.member.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.DiscordAssets;
import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Member Member} updated their {@link net.dv8tion.jda.api.entities.Guild Guild} banner.
 *
 * <p>Can be used to retrieve members who change their per guild banner, the triggering guild, the old banner id and the new banner id.
 *
 * <p>Identifier: {@code banner}
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
public class GuildMemberUpdateBannerEvent extends GenericGuildMemberUpdateEvent<String> {
    public static final String IDENTIFIER = "banner";

    public GuildMemberUpdateBannerEvent(
            @Nonnull JDA api, long responseNumber, @Nonnull Member member, @Nullable String oldBannerId) {
        super(api, responseNumber, member, oldBannerId, member.getBannerId(), IDENTIFIER);
    }

    /**
     * The old banner id
     *
     * @return The old banner id
     */
    @Nullable
    public String getOldBannerId() {
        return getOldValue();
    }

    /**
     * The previous banner url
     *
     * @return The previous banner url
     */
    @Nullable
    public String getOldBannerUrl() {
        return previous == null
                ? null
                : getOldBannerUrl(previous.startsWith("a_") ? ImageFormat.ANIMATED_WEBP : ImageFormat.PNG);
    }

    /**
     * The previous banner url
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The previous banner url
     *
     * @see    DiscordAssets#memberBanner(ImageFormat, String, String, String)
     */
    @Nullable
    public String getOldBannerUrl(@Nonnull ImageFormat format) {
        ImageProxy proxy = getOldBanner(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this member's old banner.
     * <p>
     * <b>Note:</b> the old banner may not always be downloadable as it might have been removed from Discord.
     *
     * @return Possibly-null {@link ImageProxy} of this member's old banner
     *
     * @see    #getOldBannerUrl()
     */
    @Nullable
    public ImageProxy getOldBanner() {
        String oldBannerUrl = getOldBannerUrl();
        return oldBannerUrl == null ? null : new ImageProxy(oldBannerUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this member's old banner.
     * <p>
     * <b>Note:</b> the old banner may not always be downloadable as it might have been removed from Discord.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this member's old banner
     *
     * @see    #getOldBannerUrl(ImageFormat)
     * @see    DiscordAssets#memberBanner(ImageFormat, String, String, String)
     */
    @Nullable
    public ImageProxy getOldBanner(@Nonnull ImageFormat format) {
        return DiscordAssets.memberBanner(format, getGuild().getId(), getUser().getId(), previous);
    }

    /**
     * The new banner id
     *
     * @return The new banner id
     */
    @Nullable
    public String getNewBannerId() {
        return getNewValue();
    }

    /**
     * The url of the new banner
     *
     * @return The url of the new banner
     */
    @Nullable
    public String getNewBannerUrl() {
        return next == null
                ? null
                : getNewBannerUrl(next.startsWith("a_") ? ImageFormat.ANIMATED_WEBP : ImageFormat.PNG);
    }

    /**
     * The url of the new banner
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The url of the new banner
     *
     * @see    DiscordAssets#memberBanner(ImageFormat, String, String, String)
     */
    @Nullable
    public String getNewBannerUrl(@Nonnull ImageFormat format) {
        ImageProxy proxy = getNewBanner(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this member's new banner.
     *
     * @return Possibly-null {@link ImageProxy} of this member's new banner
     *
     * @see    #getNewBannerUrl()
     */
    @Nullable
    public ImageProxy getNewBanner() {
        String newBannerUrl = getNewBannerUrl();
        return newBannerUrl == null ? null : new ImageProxy(newBannerUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this member's new banner.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this member's new banner
     *
     * @see    #getNewBannerUrl(ImageFormat)
     * @see    DiscordAssets#memberBanner(ImageFormat, String, String, String)
     */
    @Nullable
    public ImageProxy getNewBanner(@Nonnull ImageFormat format) {
        return DiscordAssets.memberBanner(format, getGuild().getId(), getUser().getId(), next);
    }
}

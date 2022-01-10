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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manager providing functionality to update one or more fields for a {@link net.dv8tion.jda.api.entities.Guild Guild}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("Official JDA Guild")
 *        .setIcon(null)
 *        .queue();
 * manager.reset(GuildManager.NAME | GuildManager.ICON)
 *        .setName("Minn's Meme Den")
 *        .setExplicitContentLevel(Guild.ExplicitContentLevel.HIGH)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.api.entities.Guild#getManager()
 */
public interface GuildManager extends Manager<GuildManager>
{
    /** Used to reset the name field */
    long NAME   = 1;
    /** Used to reset the icon field */
    long ICON   = 1 << 1;
    /** Used to reset the splash field */
    long SPLASH = 1 << 2;
    /** Used to reset the afk channel field */
    long AFK_CHANNEL    = 1 << 3;
    /** Used to reset the afk timeout field */
    long AFK_TIMEOUT    = 1 << 4;
    /** Used to reset the system channel field */
    long SYSTEM_CHANNEL = 1 << 5;
    /** Used to reset the mfa level field */
    long MFA_LEVEL      = 1 << 6;
    /** Used to reset the default notification level field */
    long NOTIFICATION_LEVEL     = 1 << 7;
    /** Used to reset the explicit content level field */
    long EXPLICIT_CONTENT_LEVEL = 1 << 8;
    /** Used to reset the verification level field */
    long VERIFICATION_LEVEL     = 1 << 9;
    /** Used to reset the banner field */
    long BANNER                 = 1 << 10;
    /** Used to reset the description field */
    long DESCRIPTION                = 1 << 11;
    /** Used to reset the rules channel field */
    long RULES_CHANNEL              = 1 << 12;
    /** Used to reset the community updates channel field */
    long COMMUNITY_UPDATES_CHANNEL  = 1 << 13;
    /** Used to reset the premium progress bar enabled field */
    long BOOST_PROGRESS_BAR_ENABLED = 1 << 14;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(GuildManager.NAME | GuildManager.ICON);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ICON}</li>
     *     <li>{@link #SPLASH}</li>
     *     <li>{@link #AFK_CHANNEL}</li>
     *     <li>{@link #AFK_TIMEOUT}</li>
     *     <li>{@link #SYSTEM_CHANNEL}</li>
     *     <li>{@link #RULES_CHANNEL}</li>
     *     <li>{@link #COMMUNITY_UPDATES_CHANNEL}</li>
     *     <li>{@link #MFA_LEVEL}</li>
     *     <li>{@link #NOTIFICATION_LEVEL}</li>
     *     <li>{@link #EXPLICIT_CONTENT_LEVEL}</li>
     *     <li>{@link #VERIFICATION_LEVEL}</li>
     *     <li>{@link #BOOST_PROGRESS_BAR_ENABLED}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @Override
    GuildManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(GuildManager.NAME, GuildManager.ICON);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ICON}</li>
     *     <li>{@link #SPLASH}</li>
     *     <li>{@link #AFK_CHANNEL}</li>
     *     <li>{@link #AFK_TIMEOUT}</li>
     *     <li>{@link #SYSTEM_CHANNEL}</li>
     *     <li>{@link #RULES_CHANNEL}</li>
     *     <li>{@link #COMMUNITY_UPDATES_CHANNEL}</li>
     *     <li>{@link #MFA_LEVEL}</li>
     *     <li>{@link #NOTIFICATION_LEVEL}</li>
     *     <li>{@link #EXPLICIT_CONTENT_LEVEL}</li>
     *     <li>{@link #VERIFICATION_LEVEL}</li>
     *     <li>{@link #BOOST_PROGRESS_BAR_ENABLED}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @Override
    GuildManager reset(long... fields);

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} object of this Manager.
     * Useful if this Manager was returned via a create function
     *
     * @return The {@link net.dv8tion.jda.api.entities.Guild Guild} of this Manager
     */
    @Nonnull
    Guild getGuild();

    /**
     * Sets the name of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  name
     *         The new name for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 2-100 characters long
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setName(@Nonnull String name);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Icon Icon} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  icon
     *         The new icon for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         or {@code null} to reset
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setIcon(@Nullable Icon icon);

    /**
     * Sets the Splash {@link net.dv8tion.jda.api.entities.Icon Icon} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  splash
     *         The new splash for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         or {@code null} to reset
     *
     * @throws java.lang.IllegalStateException
     *         If the guild's {@link net.dv8tion.jda.api.entities.Guild#getFeatures() features} do not include {@code INVITE_SPLASH}
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setSplash(@Nullable Icon splash);

    /**
     * Sets the AFK {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  afkChannel
     *         The new afk channel for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         or {@code null} to reset
     *
     * @throws IllegalArgumentException
     *         If the provided channel is not from this guild
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setAfkChannel(@Nullable VoiceChannel afkChannel);

    /**
     * Sets the system {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  systemChannel
     *         The new system channel for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         or {@code null} to reset
     *
     * @throws IllegalArgumentException
     *         If the provided channel is not from this guild
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setSystemChannel(@Nullable TextChannel systemChannel);

    /**
     * Sets the rules {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  rulesChannel
     *         The new rules channel for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         or {@code null} to reset
     *
     * @throws IllegalArgumentException
     *         If the provided channel is not from this guild
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setRulesChannel(@Nullable TextChannel rulesChannel);

    /**
     * Sets the community updates {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  communityUpdatesChannel
     *         The new community updates channel for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         or {@code null} to reset
     *
     * @throws IllegalArgumentException
     *         If the provided channel is not from this guild
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setCommunityUpdatesChannel(@Nullable TextChannel communityUpdatesChannel);

    /**
     * Sets the afk {@link net.dv8tion.jda.api.entities.Guild.Timeout Timeout} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  timeout
     *         The new afk timeout for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided timeout is {@code null}
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setAfkTimeout(@Nonnull Guild.Timeout timeout);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel Verification Level} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  level
     *         The new Verification Level for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided level is {@code null} or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setVerificationLevel(@Nonnull Guild.VerificationLevel level);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Guild.NotificationLevel Notification Level} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  level
     *         The new Notification Level for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided level is {@code null} or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setDefaultNotificationLevel(@Nonnull Guild.NotificationLevel level);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Guild.MFALevel MFA Level} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  level
     *         The new MFA Level for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided level is {@code null} or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setRequiredMFALevel(@Nonnull Guild.MFALevel level);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel Explicit Content Level} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  level
     *         The new MFA Level for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @throws IllegalArgumentException
     *         If the provided level is {@code null} or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setExplicitContentLevel(@Nonnull Guild.ExplicitContentLevel level);

    /**
     * Sets the Banner {@link net.dv8tion.jda.api.entities.Icon Icon} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  banner
     *         The new banner for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         or {@code null} to reset
     *
     * @throws java.lang.IllegalStateException
     *         If the guild's {@link net.dv8tion.jda.api.entities.Guild#getFeatures() features} do not include {@code BANNER}
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setBanner(@Nullable Icon banner);

    /**
     * Sets the Description {@link net.dv8tion.jda.api.entities.Icon Icon} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  description
     *         The new description for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         or {@code null} to reset
     *
     * @throws java.lang.IllegalStateException
     *         If the guild's {@link net.dv8tion.jda.api.entities.Guild#getFeatures() features} do not include {@code VERIFIED}
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setDescription(@Nullable String description);

    /**
     * Sets whether this {@link net.dv8tion.jda.api.entities.Guild Guild} should have its boost progress bar shown.
     *
     * @param  boostProgressBarEnabled
     *         Whether the boost progress bar should be shown
     *         for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildManager setBoostProgressBarEnabled(boolean boostProgressBarEnabled);
}

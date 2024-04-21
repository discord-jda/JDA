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
package net.dv8tion.jda.api.managers

import Guild.ExplicitContentLevel
import Guild.MFALevel
import Guild.NotificationLevel
import Guild.VerificationLevel
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for a [Guild][net.dv8tion.jda.api.entities.Guild].
 *
 *
 * **Example**
 * <pre>`manager.setName("Official JDA Guild")
 * .setIcon(null)
 * .queue();
 * manager.reset(GuildManager.NAME | GuildManager.ICON)
 * .setName("Minn's Meme Den")
 * .setExplicitContentLevel(Guild.ExplicitContentLevel.HIGH)
 * .queue();
`</pre> *
 *
 * @see net.dv8tion.jda.api.entities.Guild.getManager
 */
interface GuildManager : Manager<GuildManager?> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(GuildManager.NAME | GuildManager.ICON);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.ICON]
     *  * [.SPLASH]
     *  * [.AFK_CHANNEL]
     *  * [.AFK_TIMEOUT]
     *  * [.SYSTEM_CHANNEL]
     *  * [.RULES_CHANNEL]
     *  * [.COMMUNITY_UPDATES_CHANNEL]
     *  * [.MFA_LEVEL]
     *  * [.NOTIFICATION_LEVEL]
     *  * [.EXPLICIT_CONTENT_LEVEL]
     *  * [.VERIFICATION_LEVEL]
     *  * [.BOOST_PROGRESS_BAR_ENABLED]
     *  * [.FEATURES]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): GuildManager?

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br></br>Example: `manager.reset(GuildManager.NAME, GuildManager.ICON);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.ICON]
     *  * [.SPLASH]
     *  * [.AFK_CHANNEL]
     *  * [.AFK_TIMEOUT]
     *  * [.SYSTEM_CHANNEL]
     *  * [.RULES_CHANNEL]
     *  * [.COMMUNITY_UPDATES_CHANNEL]
     *  * [.MFA_LEVEL]
     *  * [.NOTIFICATION_LEVEL]
     *  * [.EXPLICIT_CONTENT_LEVEL]
     *  * [.VERIFICATION_LEVEL]
     *  * [.BOOST_PROGRESS_BAR_ENABLED]
     *  * [.FEATURES]
     *
     *
     * @param  fields
     * Integer values containing the flags to reset.
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): GuildManager?

    @get:Nonnull
    val guild: Guild?

    /**
     * Sets the name of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  name
     * The new name for this [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @throws IllegalArgumentException
     * If the provided name is `null` or not between 2-100 characters long
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): GuildManager?

    /**
     * Sets the [Icon][net.dv8tion.jda.api.entities.Icon] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  icon
     * The new icon for this [Guild][net.dv8tion.jda.api.entities.Guild]
     * or `null` to reset
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setIcon(icon: Icon?): GuildManager?

    /**
     * Sets the Splash [Icon][net.dv8tion.jda.api.entities.Icon] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  splash
     * The new splash for this [Guild][net.dv8tion.jda.api.entities.Guild]
     * or `null` to reset
     *
     * @throws java.lang.IllegalStateException
     * If the guild's [features][net.dv8tion.jda.api.entities.Guild.getFeatures] do not include `INVITE_SPLASH`
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setSplash(splash: Icon?): GuildManager?

    /**
     * Sets the AFK [VoiceChannel][net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  afkChannel
     * The new afk channel for this [Guild][net.dv8tion.jda.api.entities.Guild]
     * or `null` to reset
     *
     * @throws IllegalArgumentException
     * If the provided channel is not from this guild
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setAfkChannel(afkChannel: VoiceChannel?): GuildManager?

    /**
     * Sets the system [TextChannel][net.dv8tion.jda.api.entities.channel.concrete.TextChannel] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  systemChannel
     * The new system channel for this [Guild][net.dv8tion.jda.api.entities.Guild]
     * or `null` to reset
     *
     * @throws IllegalArgumentException
     * If the provided channel is not from this guild
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setSystemChannel(systemChannel: TextChannel?): GuildManager?

    /**
     * Sets the rules [TextChannel][net.dv8tion.jda.api.entities.channel.concrete.TextChannel] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  rulesChannel
     * The new rules channel for this [Guild][net.dv8tion.jda.api.entities.Guild]
     * or `null` to reset
     *
     * @throws IllegalArgumentException
     * If the provided channel is not from this guild
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setRulesChannel(rulesChannel: TextChannel?): GuildManager?

    /**
     * Sets the community updates [TextChannel][net.dv8tion.jda.api.entities.channel.concrete.TextChannel] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  communityUpdatesChannel
     * The new community updates channel for this [Guild][net.dv8tion.jda.api.entities.Guild]
     * or `null` to reset
     *
     * @throws IllegalArgumentException
     * If the provided channel is not from this guild
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setCommunityUpdatesChannel(communityUpdatesChannel: TextChannel?): GuildManager?

    /**
     * Sets the afk [Timeout][net.dv8tion.jda.api.entities.Guild.Timeout] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  timeout
     * The new afk timeout for this [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @throws IllegalArgumentException
     * If the provided timeout is `null`
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setAfkTimeout(@Nonnull timeout: Guild.Timeout?): GuildManager?

    /**
     * Sets the [Verification Level][net.dv8tion.jda.api.entities.Guild.VerificationLevel] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  level
     * The new Verification Level for this [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @throws IllegalArgumentException
     * If the provided level is `null` or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setVerificationLevel(@Nonnull level: VerificationLevel?): GuildManager?

    /**
     * Sets the [Notification Level][net.dv8tion.jda.api.entities.Guild.NotificationLevel] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  level
     * The new Notification Level for this [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @throws IllegalArgumentException
     * If the provided level is `null` or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setDefaultNotificationLevel(@Nonnull level: NotificationLevel?): GuildManager?

    /**
     * Sets the [MFA Level][net.dv8tion.jda.api.entities.Guild.MFALevel] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  level
     * The new MFA Level for this [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @throws IllegalArgumentException
     * If the provided level is `null` or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setRequiredMFALevel(@Nonnull level: MFALevel?): GuildManager?

    /**
     * Sets the [Explicit Content Level][net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  level
     * The new MFA Level for this [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @throws IllegalArgumentException
     * If the provided level is `null` or UNKNOWN
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setExplicitContentLevel(@Nonnull level: ExplicitContentLevel?): GuildManager?

    /**
     * Sets the Banner [Icon][net.dv8tion.jda.api.entities.Icon] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  banner
     * The new banner for this [Guild][net.dv8tion.jda.api.entities.Guild]
     * or `null` to reset
     *
     * @throws java.lang.IllegalStateException
     * If the guild's [features][net.dv8tion.jda.api.entities.Guild.getFeatures] do not include `BANNER`
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setBanner(banner: Icon?): GuildManager?

    /**
     * Sets the Description [Icon][net.dv8tion.jda.api.entities.Icon] of this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  description
     * The new description for this [Guild][net.dv8tion.jda.api.entities.Guild]
     * or `null` to reset
     *
     * @throws java.lang.IllegalStateException
     * If the guild's [features][net.dv8tion.jda.api.entities.Guild.getFeatures] do not include `VERIFIED`
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setDescription(description: String?): GuildManager?

    /**
     * Sets whether this [Guild][net.dv8tion.jda.api.entities.Guild] should have its boost progress bar shown.
     *
     * @param  boostProgressBarEnabled
     * Whether the boost progress bar should be shown
     * for this [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setBoostProgressBarEnabled(boostProgressBarEnabled: Boolean): GuildManager?

    /**
     * Configures the new [features][Guild.getFeatures] of the [Guild].
     * <br></br>The list of available features, including which ones can be configured, is available in the
     * [Official Discord API Documentation](https://discord.com/developers/docs/resources/guild#guild-object-guild-features).
     *
     *
     * **Example**
     * <pre>`List<String> features = new ArrayList<>(guild.getFeatures());
     * features.add("INVITES_DISABLED");
     * guild.getManager().setFeatures(features).queue();
    `</pre> *
     *
     * @param  features
     * The new features to use
     *
     * @throws IllegalArgumentException
     * If the provided list is null
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setFeatures(@Nonnull features: Collection<String?>?): GuildManager?

    /**
     * Adds a [Guild Feature][Guild.getFeatures] to the list of features.
     * <br></br>The list of available features, including which ones can be configured, is available in the
     * [Official Discord API Documentation](https://discord.com/developers/docs/resources/guild#guild-object-guild-features).
     *
     * @param  features
     * The features to add
     *
     * @throws IllegalArgumentException
     * If any of the provided features is null
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addFeatures(@Nonnull features: Collection<String?>?): GuildManager?

    /**
     * Adds a [Guild Feature][Guild.getFeatures] to the list of features.
     * <br></br>The list of available features, including which ones can be configured, is available in the
     * [Official Discord API Documentation](https://discord.com/developers/docs/resources/guild#guild-object-guild-features).
     *
     * @param  features
     * The features to add
     *
     * @throws IllegalArgumentException
     * If any of the provided features is null
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addFeatures(@Nonnull vararg features: String?): GuildManager? {
        Checks.noneNull(features, "Features")
        return addFeatures(Arrays.asList(*features))
    }

    /**
     * Removes a [Guild Feature][Guild.getFeatures] from the list of features.
     * <br></br>The list of available features, including which ones can be configured, is available in the
     * [Official Discord API Documentation](https://discord.com/developers/docs/resources/guild#guild-object-guild-features).
     *
     * @param  features
     * The features to remove
     *
     * @throws IllegalArgumentException
     * If any of the provided features is null
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun removeFeatures(@Nonnull features: Collection<String?>?): GuildManager?

    /**
     * Removes a [Guild Feature][Guild.getFeatures] from the list of features.
     * <br></br>The list of available features, including which ones can be configured, is available in the
     * [Official Discord API Documentation](https://discord.com/developers/docs/resources/guild#guild-object-guild-features).
     *
     * @param  features
     * The features to remove
     *
     * @throws IllegalArgumentException
     * If any of the provided features is null
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun removeFeatures(@Nonnull vararg features: String?): GuildManager? {
        Checks.noneNull(features, "Features")
        return removeFeatures(Arrays.asList(*features))
    }

    /**
     * Configures the `INVITES_DISABLED` feature flag of this guild.
     * <br></br>This is equivalent to adding or removing the feature `INVITES_DISABLED` via [.setFeatures].
     *
     * @param  disabled
     * True, to pause/disable all invites to the guild
     *
     * @return GuildManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setInvitesDisabled(disabled: Boolean): GuildManager? {
        return if (disabled) addFeatures("INVITES_DISABLED") else removeFeatures("INVITES_DISABLED")
    }

    companion object {
        /** Used to reset the name field  */
        const val NAME: Long = 1

        /** Used to reset the icon field  */
        const val ICON = (1 shl 1).toLong()

        /** Used to reset the splash field  */
        const val SPLASH = (1 shl 2).toLong()

        /** Used to reset the afk channel field  */
        const val AFK_CHANNEL = (1 shl 3).toLong()

        /** Used to reset the afk timeout field  */
        const val AFK_TIMEOUT = (1 shl 4).toLong()

        /** Used to reset the system channel field  */
        const val SYSTEM_CHANNEL = (1 shl 5).toLong()

        /** Used to reset the mfa level field  */
        const val MFA_LEVEL = (1 shl 6).toLong()

        /** Used to reset the default notification level field  */
        const val NOTIFICATION_LEVEL = (1 shl 7).toLong()

        /** Used to reset the explicit content level field  */
        const val EXPLICIT_CONTENT_LEVEL = (1 shl 8).toLong()

        /** Used to reset the verification level field  */
        const val VERIFICATION_LEVEL = (1 shl 9).toLong()

        /** Used to reset the banner field  */
        const val BANNER = (1 shl 10).toLong()

        /** Used to reset the description field  */
        const val DESCRIPTION = (1 shl 11).toLong()

        /** Used to reset the rules channel field  */
        const val RULES_CHANNEL = (1 shl 12).toLong()

        /** Used to reset the community updates channel field  */
        const val COMMUNITY_UPDATES_CHANNEL = (1 shl 13).toLong()

        /** Used to reset the premium progress bar enabled field  */
        const val BOOST_PROGRESS_BAR_ENABLED = (1 shl 14).toLong()

        /** Used to add or remove modifiable features (such as `"INVITES_DISABLED"`)  */
        const val FEATURES = (1 shl 15).toLong()
    }
}

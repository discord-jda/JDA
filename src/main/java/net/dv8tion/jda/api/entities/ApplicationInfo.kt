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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.utils.ImageProxy
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.Nonnull

/**
 * Represents a Discord Application from its bot's point of view.
 *
 * @since  3.0
 * @author Aljoscha Grebe
 *
 * @see net.dv8tion.jda.api.JDA.retrieveApplicationInfo
 */
interface ApplicationInfo : ISnowflake {
    /**
     * Whether the bot requires code grant to invite or not.
     *
     *
     * This means that additional OAuth2 steps are required to authorize the application to make a bot join a guild
     * like `&response_type=code` together with a valid `&redirect_uri`.
     * <br></br>For more information look at the [Discord OAuth2 documentation](https://discord.com/developers/docs/topics/oauth2).
     *
     * @return Whether the bot requires code grant
     */
    fun doesBotRequireCodeGrant(): Boolean

    @get:Nonnull
    val description: String?

    /**
     * The URL for the application's terms of service.
     *
     * @return The URL for the application's terms of service or `null` if none is set
     */
    val termsOfServiceUrl: String?

    /**
     * The URL for the application's privacy policy.
     *
     * @return The URL for the application's privacy policy or `null` if none is set
     */
    val privacyPolicyUrl: String?

    /**
     * The icon id of the bot's application.
     * <br></br>The application icon is **not** necessarily the same as the bot's avatar!
     *
     * @return The icon id of the bot's application or null if no icon is defined
     */
    val iconId: String?

    /**
     * The icon-url of the bot's application.
     * <br></br>The application icon is **not** necessarily the same as the bot's avatar!
     *
     * @return The icon-url of the bot's application or null if no icon is defined
     */
    val iconUrl: String?
    val icon: ImageProxy?
        /**
         * Returns an [ImageProxy] for this application info's icon.
         *
         * @return The [ImageProxy] of this application info's icon or null if no icon is defined
         *
         * @see .getIconUrl
         */
        get() {
            val iconUrl = iconUrl
            return iconUrl?.let { ImageProxy(it) }
        }

    /**
     * The team information for this application.
     *
     * @return The [net.dv8tion.jda.api.entities.ApplicationTeam], or null if this application is not in a team.
     */
    val team: ApplicationTeam?

    /**
     * Configures the required scopes applied to the [.getInviteUrl] and similar methods.
     * <br></br>To use slash commands you must add `"applications.commands"` to these scopes. The scope `"bot"` is always applied.
     *
     * @param  scopes
     * The scopes to use with [.getInviteUrl] and the likes
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The current ApplicationInfo instance
     */
    @Nonnull
    fun setRequiredScopes(@Nonnull vararg scopes: String?): ApplicationInfo? {
        Checks.noneNull(scopes, "Scopes")
        return setRequiredScopes(Arrays.asList(*scopes))
    }

    /**
     * Configures the required scopes applied to the [.getInviteUrl] and similar methods.
     * <br></br>To use slash commands you must add `"applications.commands"` to these scopes. The scope `"bot"` is always applied.
     *
     * @param  scopes
     * The scopes to use with [.getInviteUrl] and the likes
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The current ApplicationInfo instance
     */
    @Nonnull
    fun setRequiredScopes(@Nonnull scopes: Collection<String?>?): ApplicationInfo?

    /**
     * Creates a OAuth invite-link used to invite the bot.
     *
     *
     * The link is provided in the following format:
     * <br></br>`https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS`
     * <br></br>Unnecessary query parameters are stripped.
     *
     * @param  permissions
     * Possibly empty [Collection][java.util.Collection] of [Permissions][net.dv8tion.jda.api.Permission]
     * that should be requested via invite.
     *
     * @return The link used to invite the bot
     */
    @Nonnull
    fun getInviteUrl(permissions: Collection<Permission?>?): String? {
        return getInviteUrl(null, permissions)
    }

    /**
     * Creates a OAuth invite-link used to invite the bot.
     *
     *
     * The link is provided in the following format:
     * <br></br>`https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS`
     * <br></br>Unnecessary query parameters are stripped.
     *
     * @param  permissions
     * [Permissions][net.dv8tion.jda.api.Permission] that should be requested via invite.
     *
     * @return The link used to invite the bot
     */
    @Nonnull
    fun getInviteUrl(vararg permissions: Permission?): String? {
        return getInviteUrl(null, *permissions)
    }

    /**
     * Creates a OAuth invite-link used to invite the bot.
     *
     *
     * The link is provided in the following format:
     * <br></br>`https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS&guild_id=GUILD_ID`
     * <br></br>Unnecessary query parameters are stripped.
     *
     * @param  guildId
     * The id of the pre-selected guild.
     * @param  permissions
     * Possibly empty [Collection][java.util.Collection] of [Permissions][net.dv8tion.jda.api.Permission]
     * that should be requested via invite.
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return The link used to invite the bot
     */
    @Nonnull
    fun getInviteUrl(guildId: String?, permissions: Collection<Permission?>?): String?

    /**
     * Creates a OAuth invite-link used to invite the bot.
     *
     *
     * The link is provided in the following format:
     * <br></br>`https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS&guild_id=GUILD_ID`
     * <br></br>Unnecessary query parameters are stripped.
     *
     * @param  guildId
     * The id of the pre-selected guild.
     * @param  permissions
     * Possibly empty [Collection][java.util.Collection] of [Permissions][net.dv8tion.jda.api.Permission]
     * that should be requested via invite.
     *
     * @return The link used to invite the bot
     */
    @Nonnull
    fun getInviteUrl(guildId: Long, permissions: Collection<Permission?>?): String? {
        return getInviteUrl(java.lang.Long.toUnsignedString(guildId), permissions)
    }

    /**
     * Creates a OAuth invite-link used to invite the bot.
     *
     *
     * The link is provided in the following format:
     * <br></br>`https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS&guild_id=GUILD_ID`
     * <br></br>Unnecessary query parameters are stripped.
     *
     * @param  guildId
     * The id of the pre-selected guild.
     * @param  permissions
     * Possibly empty array of [Permissions][net.dv8tion.jda.api.Permission]
     * that should be requested via invite.
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return The link used to invite the bot
     */
    @Nonnull
    fun getInviteUrl(guildId: String?, vararg permissions: Permission?): String? {
        return getInviteUrl(guildId, if (permissions == null) null else Arrays.asList(*permissions))
    }

    /**
     * Creates a OAuth invite-link used to invite the bot.
     *
     *
     * The link is provided in the following format:
     * <br></br>`https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS&guild_id=GUILD_ID`
     * <br></br>Unnecessary query parameters are stripped.
     *
     * @param  guildId
     * The id of the pre-selected guild.
     * @param  permissions
     * Possibly empty array of [Permissions][net.dv8tion.jda.api.Permission]
     * that should be requested via invite.
     *
     * @return The link used to invite the bot
     */
    @Nonnull
    fun getInviteUrl(guildId: Long, vararg permissions: Permission?): String? {
        return getInviteUrl(java.lang.Long.toUnsignedString(guildId), *permissions)
    }

    @get:Nonnull
    val jDA: JDA?

    @get:Nonnull
    val name: String?

    @get:Nonnull
    val owner: User?

    /**
     * Whether the bot is public or not.
     * Public bots can be added by anyone. When false only the owner can invite the bot to guilds.
     *
     * @return Whether the bot is public
     */
    val isBotPublic: Boolean

    @get:Nonnull
    val tags: List<String?>?

    @get:Nonnull
    val redirectUris: List<String?>?

    /**
     * The interaction endpoint URL of this bot's application.
     *
     *
     * This returns `null` if no interaction endpoint URL is set in the [Developer Portal](https://discord.com/developers/applications).
     *
     *
     * A non-null value means your bot will no longer receive [interactions][net.dv8tion.jda.api.interactions.Interaction]
     * through JDA, such as slash commands, components and modals.
     *
     * @return Interaction endpoint URL of this bot's application, or `null` if it has not been set
     */
    val interactionsEndpointUrl: String?

    /**
     * The role connections (linked roles) verification URL of this bot's application.
     *
     *
     * This returns `null` if no role connection verification URL is set in the [Developer Portal](https://discord.com/developers/applications).
     *
     * @return Role connections verification URL of this bot's application, or `null` if it has not been set
     */
    val roleConnectionsVerificationUrl: String?

    /**
     * The custom Authorization URL of this bot's application.
     *
     *
     * This returns `null` if no custom URL is set in the [Developer Portal](https://discord.com/developers/applications) or if In-app Authorization is enabled.
     *
     * @return Custom Authorization URL, or null if it has not been set
     */
    val customAuthorizationUrl: String?

    @get:Nonnull
    val scopes: List<String?>?

    @get:Nonnull
    val permissions: EnumSet<Permission?>?

    /**
     * The `long` representation of the literal permissions the default authorization URL is set up with.
     *
     * @return Never-negative long containing offset permissions the default authorization URL is set up with.
     */
    val permissionsRaw: Long

    @get:Nonnull
    val flags: EnumSet<Flag?>?
        /**
         * The [Flags][Flag] set for the application.
         * <br></br>Modifying the returned EnumSet will have not actually change the flags of the application.
         *
         * @return [EnumSet] of [Flag]
         */
        get() = Flag.fromRaw(flagsRaw)

    /**
     * The raw bitset representing this application's flags.
     *
     * @return The bitset
     */
    val flagsRaw: Long

    /**
     * Flag constants corresponding to the [Discord Enum](https://discord.com/developers/docs/resources/application#application-object-application-flags)
     *
     * @see .getFlags
     */
    enum class Flag(private val value: Long) {
        /** Bot can use [GatewayIntent.GUILD_PRESENCES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES] in 100 or more guilds  */
        GATEWAY_PRESENCE(1 shl 12),

        /** Bot can use [GatewayIntent.GUILD_PRESENCES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES] in under 100 guilds  */
        GATEWAY_PRESENCE_LIMITED(1 shl 13),

        /** Bot can use [GatewayIntent.GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] in 100 or more guilds  */
        GATEWAY_GUILD_MEMBERS(1 shl 14),

        /** Bot can use [GatewayIntent.GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] in under 100 guilds  */
        GATEWAY_GUILD_MEMBERS_LIMITED(1 shl 15),

        /** Indicates unusual growth of an app that prevents verification  */
        VERIFICATION_PENDING_GUILD_LIMIT(1 shl 16),

        /** Indicates if an app is embedded within the Discord client (currently unavailable publicly)  */
        EMBEDDED(1 shl 17),

        /** Bot can use [GatewayIntent.MESSAGE_CONTENT][net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT] in 100 or more guilds  */
        GATEWAY_MESSAGE_CONTENT(1 shl 18),

        /** Bot can use [GatewayIntent.MESSAGE_CONTENT][net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT] in under 100 guilds  */
        GATEWAY_MESSAGE_CONTENT_LIMITED(1 shl 19);

        companion object {
            /**
             * Converts the provided bitset to the corresponding enum constants.
             *
             * @param  raw
             * The bitset of flags
             *
             * @return [EnumSet] of [Flag]
             */
            @Nonnull
            fun fromRaw(raw: Long): EnumSet<Flag?> {
                val set = EnumSet.noneOf(
                    Flag::class.java
                )
                for (flag in entries) {
                    if (raw and flag.value != 0L) set.add(flag)
                }
                return set
            }
        }
    }
}

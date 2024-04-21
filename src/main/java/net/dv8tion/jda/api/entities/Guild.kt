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

import net.dv8tion.jda.annotations.DeprecatedSince
import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.Incubating
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.entities.automod.AutoModRule
import net.dv8tion.jda.api.entities.automod.build.AutoModRuleData
import net.dv8tion.jda.api.entities.channel.attribute.ICopyableChannel
import net.dv8tion.jda.api.entities.channel.attribute.IGuildChannelContainer
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.api.entities.sticker.GuildSticker
import net.dv8tion.jda.api.entities.sticker.StickerItem
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.api.entities.templates.Template
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.PrivilegeConfig
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege
import net.dv8tion.jda.api.managers.*
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.*
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction
import net.dv8tion.jda.api.requests.restaction.pagination.BanPaginationAction
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.ImageProxy
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.cache.MemberCacheView
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView
import net.dv8tion.jda.api.utils.cache.SortedChannelCacheView
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView
import net.dv8tion.jda.api.utils.concurrent.Task
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.dv8tion.jda.internal.requests.DeferredRestAction
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import net.dv8tion.jda.internal.utils.Helpers
import net.dv8tion.jda.internal.utils.concurrent.task.GatewayTask
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.OffsetDateTime
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull
import kotlin.math.max

/**
 * Represents a Discord [Guild][net.dv8tion.jda.api.entities.Guild].
 * This should contain all information provided from Discord about a Guild.
 *
 * @see JDA.getGuildCache
 * @see JDA.getGuildById
 * @see JDA.getGuildsByName
 * @see JDA.getGuilds
 */
interface Guild : IGuildChannelContainer<GuildChannel?>, ISnowflake {
    /**
     * Retrieves the list of guild commands.
     * <br></br>This list does not include global commands! Use [JDA.retrieveCommands] for global commands.
     * <br></br>This list does not include localization data. Use [.retrieveCommands] to get localization data
     *
     * @return [RestAction] - Type: [List] of [Command]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveCommands(): RestAction<List<Command?>?>? {
        return retrieveCommands(false)
    }

    /**
     * Retrieves the list of guild commands.
     * <br></br>This list does not include global commands! Use [JDA.retrieveCommands] for global commands.
     *
     * @param  withLocalizations
     * `true` if the localization data (such as name and description) should be included
     *
     * @return [RestAction] - Type: [List] of [Command]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveCommands(withLocalizations: Boolean): RestAction<List<Command?>?>?

    /**
     * Retrieves the existing [Command] instance by id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  id
     * The command id
     *
     * @throws IllegalArgumentException
     * If the provided id is not a valid snowflake
     *
     * @return [RestAction] - Type: [Command]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveCommandById(@Nonnull id: String?): RestAction<Command?>?

    /**
     * Retrieves the existing [Command] instance by id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  id
     * The command id
     *
     * @return [RestAction] - Type: [Command]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveCommandById(id: Long): RestAction<Command?>? {
        return retrieveCommandById(java.lang.Long.toUnsignedString(id))
    }

    /**
     * Creates or updates a command.
     * <br></br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     *
     * To specify a complete list of all commands you can use [.updateCommands] instead.
     *
     *
     * You need the OAuth2 scope `"applications.commands"` in order to add commands to a guild.
     *
     * @param  command
     * The [CommandData] for the command
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [RestAction] - Type: [Command]
     * <br></br>The RestAction used to create or update the command
     *
     * @see Commands.slash
     * @see Commands.message
     * @see Commands.user
     */
    @Nonnull
    @CheckReturnValue
    fun upsertCommand(@Nonnull command: CommandData?): RestAction<Command?>?

    /**
     * Creates or updates a slash command.
     * <br></br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     *
     * To specify a complete list of all commands you can use [.updateCommands] instead.
     *
     *
     * You need the OAuth2 scope `"applications.commands"` in order to add commands to a guild.
     *
     * @param  name
     * The lowercase alphanumeric (with dash) name, 1-32 characters
     * @param  description
     * The description for the command, 1-100 characters
     *
     * @throws IllegalArgumentException
     * If null is provided or the name/description do not meet the requirements
     *
     * @return [CommandCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun upsertCommand(@Nonnull name: String?, @Nonnull description: String?): CommandCreateAction? {
        return upsertCommand(CommandDataImpl(name!!, description!!)) as CommandCreateAction?
    }

    /**
     * Configures the complete list of guild commands.
     * <br></br>This will replace the existing command list for this guild. You should only use this at most once on startup!
     *
     *
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     *
     * You need the OAuth2 scope `"applications.commands"` in order to add commands to a guild.
     *
     *
     * **Examples**
     *
     *
     * Set list to 2 commands:
     * <pre>`guild.updateCommands()
     * .addCommands(Commands.slash("ping", "Gives the current ping"))
     * .addCommands(Commands.slash("ban", "Ban the target user")
     * .addOption(OptionType.USER, "user", "The user to ban", true))
     * .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
     * .queue();
    `</pre> *
     *
     *
     * Delete all commands:
     * <pre>`guild.updateCommands().queue();
    `</pre> *
     *
     * @return [CommandListUpdateAction]
     *
     * @see JDA.updateCommands
     */
    @Nonnull
    @CheckReturnValue
    fun updateCommands(): CommandListUpdateAction?

    /**
     * Edit an existing command by id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  id
     * The id of the command to edit
     *
     * @throws IllegalArgumentException
     * If the provided id is not a valid snowflake
     *
     * @return [CommandEditAction] used to edit the command
     */
    @Nonnull
    @CheckReturnValue
    fun editCommandById(@Nonnull id: String?): CommandEditAction?

    /**
     * Edit an existing command by id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  id
     * The id of the command to edit
     *
     * @return [CommandEditAction] used to edit the command
     */
    @Nonnull
    @CheckReturnValue
    fun editCommandById(id: Long): CommandEditAction? {
        return editCommandById(java.lang.Long.toUnsignedString(id))
    }

    /**
     * Delete the command for this id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  commandId
     * The id of the command that should be deleted
     *
     * @throws IllegalArgumentException
     * If the provided id is not a valid snowflake
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun deleteCommandById(@Nonnull commandId: String?): RestAction<Void?>?

    /**
     * Delete the command for this id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  commandId
     * The id of the command that should be deleted
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun deleteCommandById(commandId: Long): RestAction<Void?>? {
        return deleteCommandById(java.lang.Long.toUnsignedString(commandId))
    }

    /**
     * Retrieves the [IntegrationPrivileges][IntegrationPrivilege] for the target with the specified ID.
     * <br></br>**The ID can either be of a Command or Application!**
     *
     *
     * Moderators of a guild can modify these privileges through the Integrations Menu
     *
     *
     * If there is no command or application with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  targetId
     * The id of the command (global or guild), or application
     *
     * @throws IllegalArgumentException
     * If the id is not a valid snowflake
     *
     * @return [RestAction] - Type: [List] of [IntegrationPrivilege]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveIntegrationPrivilegesById(@Nonnull targetId: String?): RestAction<List<IntegrationPrivilege?>?>?

    /**
     * Retrieves the [IntegrationPrivileges][IntegrationPrivilege] for the target with the specified ID.
     * <br></br>**The ID can either be of a Command or Application!**
     *
     *
     * Moderators of a guild can modify these privileges through the Integrations Menu
     *
     *
     * If there is no command or application with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  targetId
     * The id of the command (global or guild), or application
     *
     * @throws IllegalArgumentException
     * If the id is not a valid snowflake
     *
     * @return [RestAction] - Type: [List] of [IntegrationPrivilege]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveIntegrationPrivilegesById(targetId: Long): RestAction<List<IntegrationPrivilege?>?>? {
        return retrieveIntegrationPrivilegesById(java.lang.Long.toUnsignedString(targetId))
    }

    /**
     * Retrieves the [IntegrationPrivileges][IntegrationPrivilege] for the commands in this guild.
     * <br></br>The RestAction provides a [PrivilegeConfig] providing the privileges of this application and its commands.
     *
     *
     * Moderators of a guild can modify these privileges through the Integrations Menu
     *
     * @return [RestAction] - Type: [PrivilegeConfig]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveCommandPrivileges(): RestAction<PrivilegeConfig?>?

    /**
     * Retrieves the available regions for this Guild
     * <br></br>Shortcut for [retrieveRegions(true)][.retrieveRegions]
     * <br></br>This will include deprecated voice regions by default.
     *
     * @return [RestAction] - Type [EnumSet][java.util.EnumSet]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveRegions(): RestAction<EnumSet<Region?>?>? {
        return retrieveRegions(true)
    }

    /**
     * Retrieves the available regions for this Guild
     *
     * @param  includeDeprecated
     * Whether to include deprecated regions
     *
     * @return [RestAction] - Type [EnumSet][java.util.EnumSet]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveRegions(includeDeprecated: Boolean): RestAction<EnumSet<Region?>?>?

    /**
     * Retrieves all current [AutoModRules][AutoModRule] for this guild.
     *
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission
     *
     * @return [RestAction] - Type: [List] of [AutoModRule]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveAutoModRules(): RestAction<List<AutoModRule?>?>?

    /**
     * Retrieves the [AutoModRule] for the provided id.
     *
     * @param  id
     * The id of the rule
     *
     * @throws IllegalArgumentException
     * If the provided id is not a valid snowflake
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission
     *
     * @return [RestAction] - Type: [AutoModRule]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveAutoModRuleById(@Nonnull id: String?): RestAction<AutoModRule?>?

    /**
     * Retrieves the [AutoModRule] for the provided id.
     *
     * @param  id
     * The id of the rule
     *
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission
     *
     * @return [RestAction] - Type: [AutoModRule]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveAutoModRuleById(id: Long): RestAction<AutoModRule?>? {
        return retrieveAutoModRuleById(java.lang.Long.toUnsignedString(id))
    }

    /**
     * Creates a new [AutoModRule] for this guild.
     *
     *
     * You can only create a certain number of rules for each [AutoModTriggerType].
     * The maximum is provided by [AutoModTriggerType.getMaxPerGuild].
     *
     * @param  data
     * The data for the new rule
     *
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have the [required permissions][AutoModRuleData.getRequiredPermissions]
     * @throws IllegalStateException
     *
     *  * If the provided data does not have any [AutoModResponse] configured
     *  * If any of the configured [AutoModResponses][AutoModResponse] is not supported by the [AutoModTriggerType]
     *
     *
     * @return [AuditableRestAction] - Type: [AutoModRule]
     */
    @Nonnull
    @CheckReturnValue
    fun createAutoModRule(@Nonnull data: AutoModRuleData?): AuditableRestAction<AutoModRule?>?

    /**
     * Returns an [AutoModRuleManager], which can be used to modify the rule for the provided id.
     *
     * The manager allows modifying multiple fields in a single request.
     * <br></br>You modify multiple fields in one request by chaining setters before calling [RestAction.queue()][net.dv8tion.jda.api.requests.RestAction.queue].
     *
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission.
     *
     * @return The manager instance
     */
    @Nonnull
    @CheckReturnValue
    fun modifyAutoModRuleById(@Nonnull id: String?): AutoModRuleManager?

    /**
     * Returns an [AutoModRuleManager], which can be used to modify the rule for the provided id.
     *
     * The manager allows modifying multiple fields in a single request.
     * <br></br>You modify multiple fields in one request by chaining setters before calling [RestAction.queue()][net.dv8tion.jda.api.requests.RestAction.queue].
     *
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission.
     *
     * @return The manager instance
     */
    @Nonnull
    @CheckReturnValue
    fun modifyAutoModRuleById(id: Long): AutoModRuleManager? {
        return modifyAutoModRuleById(java.lang.Long.toUnsignedString(id))
    }

    /**
     * Deletes the [AutoModRule] for the provided id.
     *
     * @param  id
     * The id of the rule
     *
     * @throws IllegalArgumentException
     * If the provided id is not a valid snowflake
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission
     *
     * @return [AuditableRestAction] - Type: [Void]
     */
    @Nonnull
    @CheckReturnValue
    fun deleteAutoModRuleById(@Nonnull id: String?): AuditableRestAction<Void?>?

    /**
     * Deletes the [AutoModRule] for the provided id.
     *
     * @param  id
     * The id of the rule
     *
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission
     *
     * @return [AuditableRestAction] - Type: [Void]
     */
    @Nonnull
    @CheckReturnValue
    fun deleteAutoModRuleById(id: Long): AuditableRestAction<Void?>? {
        return deleteAutoModRuleById(java.lang.Long.toUnsignedString(id))
    }

    /**
     * Adds the user to this guild as a member.
     * <br></br>This requires an **OAuth2 Access Token** with the scope `guilds.join`.
     *
     * @param  accessToken
     * The access token
     * @param  user
     * The [UserSnowflake] for the member to add.
     * This can be a member or user instance or [User.fromId].
     *
     * @throws IllegalArgumentException
     * If the access token is blank, empty, or null,
     * or if the provided user reference is null or is already in this guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.CREATE_INSTANT_INVITE][net.dv8tion.jda.api.Permission.CREATE_INSTANT_INVITE]
     *
     * @return [MemberAction]
     *
     * @see [Discord OAuth2 Documentation](https://discord.com/developers/docs/topics/oauth2)
     *
     *
     * @since  3.7.0
     */
    @Nonnull
    @CheckReturnValue
    fun addMember(@Nonnull accessToken: String?, @Nonnull user: UserSnowflake?): MemberAction?

    /**
     * Whether this guild has loaded members.
     * <br></br>This will always be false if the [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] intent is disabled.
     *
     * @return True, if members are loaded.
     */
    @JvmField
    val isLoaded: Boolean

    /**
     * Re-apply the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy] of this session to all [Members][Member] of this Guild.
     *
     *
     * **Example**<br></br>
     * <pre>`// Check if the members of this guild have at least 50% bots (bot collection/farm)
     * public void checkBots(Guild guild) {
     * // Keep in mind: This requires the GUILD_MEMBERS intent which is disabled in createDefault and createLight by default
     * guild.retrieveMembers() // Load members CompletableFuture<Void> (async and eager)
     * .thenApply((v) -> guild.getMemberCache()) // Turn into CompletableFuture<MemberCacheView>
     * .thenAccept((members) -> {
     * int total = members.size();
     * // Casting to double to get a double as result of division, don't need to worry about precision with small counts like this
     * double bots = (double) members.applyStream(stream ->
     * stream.map(Member::getUser)
     * .filter(User::isBot)
     * .count()); // Count bots
     * if (bots / total > 0.5) // Check how many members are bots
     * System.out.println("More than 50% of members in this guild are bots");
     * })
     * .thenRun(guild::pruneMemberCache); // Then prune the cache
     * }
    `</pre> *
     *
     * @see .unloadMember
     * @see JDA.unloadUser
     */
    fun pruneMemberCache()

    /**
     * Attempts to remove the user with the provided id from the member cache.
     * <br></br>If you attempt to remove the [SelfUser][JDA.getSelfUser] this will simply return `false`.
     *
     *
     * This should be used by an implementation of [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
     * as an upstream request to remove a member. For example a Least-Recently-Used (LRU) cache might use this to drop
     * old members if the cache capacity is reached. Or a timeout cache could use this to remove expired members.
     *
     * @param  userId
     * The target user id
     *
     * @return True, if the cache was changed
     *
     * @see .pruneMemberCache
     * @see JDA.unloadUser
     */
    fun unloadMember(userId: Long): Boolean

    /**
     * The expected member count for this guild.
     * <br></br>If this guild is not lazy loaded this should be identical to the size returned by [.getMemberCache].
     *
     *
     * When [GatewayIntent.GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] is disabled, this will not be updated.
     *
     * @return The expected member count for this guild
     */
    val memberCount: Int

    @JvmField
    @get:Nonnull
    val name: String?

    /**
     * The Discord hash-id of the [Guild][net.dv8tion.jda.api.entities.Guild] icon image.
     * If no icon has been set, this returns `null`.
     *
     *
     * The Guild icon can be modified using [GuildManager.setIcon].
     *
     * @return Possibly-null String containing the Guild's icon hash-id.
     */
    @JvmField
    val iconId: String?
    val iconUrl: String?
        /**
         * The URL of the [Guild][net.dv8tion.jda.api.entities.Guild] icon image.
         * If no icon has been set, this returns `null`.
         *
         *
         * The Guild icon can be modified using [GuildManager.setIcon].
         *
         * @return Possibly-null String containing the Guild's icon URL.
         */
        get() {
            val iconId = iconId
            return if (iconId == null) null else kotlin.String.format(
                Guild.Companion.ICON_URL,
                id,
                iconId,
                if (iconId.startsWith("a_")) "gif" else "png"
            )
        }
    val icon: ImageProxy?
        /**
         * Returns an [ImageProxy] for this guild's icon.
         *
         * @return The [ImageProxy] of this guild's icon
         *
         * @see .getIconUrl
         */
        get() {
            val iconUrl = iconUrl
            return iconUrl?.let { ImageProxy(it) }
        }

    @JvmField
    @get:Nonnull
    val features: Set<String?>
    val isInvitesDisabled: Boolean
        /**
         * Whether the invites for this guild are paused/disabled.
         * <br></br>This is equivalent to `getFeatures().contains("INVITES_DISABLED")`.
         *
         * @return True, if invites are paused/disabled
         */
        get() = features.contains("INVITES_DISABLED")

    /**
     * The Discord hash-id of the splash image for this Guild. A Splash image is an image displayed when viewing a
     * Discord Guild Invite on the web or in client just before accepting or declining the invite.
     * If no splash has been set, this returns `null`.
     * <br></br>Splash images are VIP/Partner Guild only.
     *
     *
     * The Guild splash can be modified using [GuildManager.setSplash].
     *
     * @return Possibly-null String containing the Guild's splash hash-id
     */
    @JvmField
    val splashId: String?
    val splashUrl: String?
        /**
         * The URL of the splash image for this Guild. A Splash image is an image displayed when viewing a
         * Discord Guild Invite on the web or in client just before accepting or declining the invite.
         * If no splash has been set, this returns `null`.
         * <br></br>Splash images are VIP/Partner Guild only.
         *
         *
         * The Guild splash can be modified using [GuildManager.setSplash].
         *
         * @return Possibly-null String containing the Guild's splash URL.
         */
        get() {
            val splashId = splashId
            return if (splashId == null) null else kotlin.String.format(Guild.Companion.SPLASH_URL, id, splashId)
        }
    val splash: ImageProxy?
        /**
         * Returns an [ImageProxy] for this guild's splash icon.
         *
         * @return Possibly-null [ImageProxy] of this guild's splash icon
         *
         * @see .getSplashUrl
         */
        get() {
            val splashUrl = splashUrl
            return splashUrl?.let { ImageProxy(it) }
        }

    /**
     * The vanity url code for this Guild. The vanity url is the custom invite code of partnered / official / boosted Guilds.
     * <br></br>The returned String will be the code that can be provided to `discord.gg/{code}` to get the invite link.
     *
     * @return The vanity code or null
     *
     * @since  4.0.0
     *
     * @see .getVanityUrl
     */
    @JvmField
    val vanityCode: String?
    val vanityUrl: String?
        /**
         * The vanity url for this Guild. The vanity url is the custom invite code of partnered / official / boosted Guilds.
         * <br></br>The returned String will be the vanity invite link to this guild.
         *
         * @return The vanity url or null
         *
         * @since  4.0.0
         */
        get() = if (vanityCode == null) null else "https://discord.gg/" + vanityCode

    /**
     * Retrieves the Vanity Invite meta data for this guild.
     * <br></br>This allows you to inspect how many times the vanity invite has been used.
     * You can use [.getVanityUrl] if you only care about the invite.
     *
     *
     * This action requires the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction] include the following:
     *
     *  * [INVITE_CODE_INVALID][net.dv8tion.jda.api.requests.ErrorResponse.INVITE_CODE_INVALID]
     * <br></br>If this guild does not have a vanity invite
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The vanity invite cannot be fetched due to a permission discrepancy
     *
     *
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_SERVER]
     *
     * @return [RestAction] - Type: [VanityInvite]
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveVanityInvite(): RestAction<VanityInvite?>?

    /**
     * The description for this guild.
     * <br></br>This is displayed in the server browser below the guild name for verified guilds.
     *
     *
     * The description can be modified using [GuildManager.setDescription].
     *
     * @return The description
     *
     * @since  4.0.0
     */
    @JvmField
    val description: String?

    @JvmField
    @get:Nonnull
    val locale: DiscordLocale?

    /**
     * The guild banner id.
     * <br></br>This is shown in guilds below the guild name.
     *
     *
     * The banner can be modified using [GuildManager.setBanner].
     *
     * @return The guild banner id or null
     *
     * @since  4.0.0
     *
     * @see .getBannerUrl
     */
    @JvmField
    val bannerId: String?
    val bannerUrl: String?
        /**
         * The guild banner url.
         * <br></br>This is shown in guilds below the guild name.
         *
         *
         * The banner can be modified using [GuildManager.setBanner].
         *
         * @return The guild banner url or null
         *
         * @since  4.0.0
         */
        get() {
            val bannerId = bannerId
            return if (bannerId == null) null else kotlin.String.format(
                Guild.Companion.BANNER_URL,
                id,
                bannerId,
                if (bannerId.startsWith("a_")) "gif" else "png"
            )
        }
    val banner: ImageProxy?
        /**
         * Returns an [ImageProxy] for this guild's banner image.
         *
         * @return Possibly-null [ImageProxy] of this guild's banner image
         *
         * @see .getBannerUrl
         */
        get() {
            val bannerUrl = bannerUrl
            return bannerUrl?.let { ImageProxy(it) }
        }

    @JvmField
    @get:Nonnull
    val boostTier: BoostTier

    /**
     * The amount of boosts this server currently has.
     *
     * @return The boost count
     *
     * @since  4.0.0
     */
    @JvmField
    val boostCount: Int

    @get:Nonnull
    val boosters: List<Member?>?
    val maxBitrate: Int
        /**
         * The maximum bitrate that can be applied to a voice channel in this guild.
         * <br></br>This depends on the features of this guild that can be unlocked for partners or through boosting.
         *
         * @return The maximum bitrate
         *
         * @since  4.0.0
         */
        get() {
            val maxBitrate = if (features.contains("VIP_REGIONS")) 384000 else 96000
            return max(maxBitrate.toDouble(), boostTier.maxBitrate.toDouble()).toInt()
        }
    val maxFileSize: Long
        /**
         * Returns the maximum size for files that can be uploaded to this Guild.
         * This returns 8 MiB for Guilds without a Boost Tier or Guilds with Boost Tier 1, 50 MiB for Guilds with Boost Tier 2 and 100 MiB for Guilds with Boost Tier 3.
         *
         * @return The maximum size for files that can be uploaded to this Guild
         *
         * @since 4.2.0
         */
        get() = boostTier.maxFileSize
    val maxEmojis: Int
        /**
         * The maximum amount of custom emojis a guild can have based on the guilds boost tier.
         *
         * @return The maximum amount of custom emojis
         */
        get() {
            val max = if (features.contains("MORE_EMOJI")) 200 else 50
            return max(max.toDouble(), boostTier.maxEmojis.toDouble()).toInt()
        }

    /**
     * The maximum amount of members that can join this guild.
     *
     * @return The maximum amount of members
     *
     * @since  4.0.0
     *
     * @see .retrieveMetaData
     */
    @JvmField
    val maxMembers: Int

    /**
     * The maximum amount of connected members this guild can have at a time.
     * <br></br>This includes members that are invisible but still connected to discord.
     * If too many members are online the guild will become unavailable for others.
     *
     * @return The maximum amount of connected members this guild can have
     *
     * @since  4.0.0
     *
     * @see .retrieveMetaData
     */
    @JvmField
    val maxPresences: Int

    /**
     * Loads [MetaData] for this guild instance.
     *
     * @return [RestAction] - Type: [MetaData]
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveMetaData(): RestAction<Guild.MetaData?>?

    /**
     * Provides the [VoiceChannel] that has been set as the channel
     * which [Members][net.dv8tion.jda.api.entities.Member] will be moved to after they have been inactive in a
     * [VoiceChannel] for longer than [.getAfkTimeout].
     * <br></br>If no channel has been set as the AFK channel, this returns `null`.
     *
     *
     * This value can be modified using [GuildManager.setAfkChannel].
     *
     * @return Possibly-null [VoiceChannel] that is the AFK Channel.
     */
    @JvmField
    val afkChannel: VoiceChannel?

    /**
     * Provides the [TextChannel] that has been set as the channel
     * which newly joined [Members][net.dv8tion.jda.api.entities.Member] will be announced in.
     * <br></br>If no channel has been set as the system channel, this returns `null`.
     *
     *
     * This value can be modified using [GuildManager.setSystemChannel].
     *
     * @return Possibly-null [TextChannel] that is the system Channel.
     */
    @JvmField
    val systemChannel: TextChannel?

    /**
     * Provides the [TextChannel] that lists the rules of the guild.
     * <br></br>If this guild doesn't have the COMMUNITY [feature][.getFeatures], this returns `null`.
     *
     * @return Possibly-null [TextChannel] that is the rules channel
     *
     * @see .getFeatures
     */
    @JvmField
    val rulesChannel: TextChannel?

    /**
     * Provides the [TextChannel] that receives community updates.
     * <br></br>If this guild doesn't have the COMMUNITY [feature][.getFeatures], this returns `null`.
     *
     * @return Possibly-null [TextChannel] that is the community updates channel
     *
     * @see .getFeatures
     */
    @JvmField
    val communityUpdatesChannel: TextChannel?

    /**
     * The [Member][net.dv8tion.jda.api.entities.Member] object for the owner of this Guild.
     * <br></br>This is null when the owner is no longer in this guild or not yet loaded (lazy loading).
     * Sometimes owners of guilds delete their account or get banned by Discord.
     *
     *
     * If lazy-loading is used it is recommended to use [.retrieveOwner] instead.
     *
     *
     * Ownership can be transferred using [net.dv8tion.jda.api.entities.Guild.transferOwnership].
     *
     *
     * This only works when the member was added to cache. Lazy loading might load this later.
     * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
     *
     * @return Possibly-null Member object for the Guild owner.
     *
     * @see .getOwnerIdLong
     * @see .retrieveOwner
     */
    @JvmField
    val owner: Member?

    /**
     * The ID for the current owner of this guild.
     * <br></br>This is useful for debugging purposes or as a shortcut.
     *
     * @return The ID for the current owner
     *
     * @see .getOwner
     */
    val ownerIdLong: Long

    @get:Nonnull
    val ownerId: String?
        /**
         * The ID for the current owner of this guild.
         * <br></br>This is useful for debugging purposes or as a shortcut.
         *
         * @return The ID for the current owner
         *
         * @see .getOwner
         */
        get() = java.lang.Long.toUnsignedString(ownerIdLong)

    @JvmField
    @get:Nonnull
    val afkTimeout: Guild.Timeout?

    /**
     * Used to determine if the provided [UserSnowflake] is a member of this Guild.
     *
     *
     * This will only check cached members! If the cache is not loaded (see [.isLoaded]), this may return false despite the user being a member.
     * This is false when [.getMember] returns `null`.
     *
     * @param  user
     * The user to check
     *
     * @return True - if this user is present and cached in this guild
     */
    fun isMember(@Nonnull user: UserSnowflake?): Boolean

    @JvmField
    @get:Nonnull
    val selfMember: Member

    @JvmField
    @get:Nonnull
    val nSFWLevel: NSFWLevel?

    /**
     * Gets the Guild specific [Member][net.dv8tion.jda.api.entities.Member] object for the provided
     * [UserSnowflake].
     * <br></br>If the user is not in this guild or currently uncached, `null` is returned.
     *
     *
     * This will only check cached members!
     *
     * @param  user
     * The [UserSnowflake] for the member to get.
     * This can be a member or user instance or [User.fromId].
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided user is null
     *
     * @return Possibly-null [Member][net.dv8tion.jda.api.entities.Member] for the related [User][net.dv8tion.jda.api.entities.User].
     *
     * @see .retrieveMember
     */
    fun getMember(@Nonnull user: UserSnowflake?): Member?

    /**
     * Gets a [Member][net.dv8tion.jda.api.entities.Member] object via the id of the user. The id relates to
     * [net.dv8tion.jda.api.entities.User.getId], and this method is similar to [JDA.getUserById]
     * <br></br>This is more efficient that using [JDA.getUserById] and [.getMember].
     * <br></br>If no Member in this Guild has the `userId` provided, this returns `null`.
     *
     *
     * This will only check cached members!
     *
     * @param  userId
     * The Discord id of the User for which a Member object is requested.
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null [Member][net.dv8tion.jda.api.entities.Member] with the related `userId`.
     *
     * @see .retrieveMemberById
     */
    fun getMemberById(@Nonnull userId: String?): Member? {
        return getMemberCache().getElementById(userId!!)
    }

    /**
     * Gets a [Member][net.dv8tion.jda.api.entities.Member] object via the id of the user. The id relates to
     * [net.dv8tion.jda.api.entities.User.getIdLong], and this method is similar to [JDA.getUserById]
     * <br></br>This is more efficient that using [JDA.getUserById] and [.getMember].
     * <br></br>If no Member in this Guild has the `userId` provided, this returns `null`.
     *
     *
     * This will only check cached members!
     * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
     *
     * @param  userId
     * The Discord id of the User for which a Member object is requested.
     *
     * @return Possibly-null [Member][net.dv8tion.jda.api.entities.Member] with the related `userId`.
     *
     * @see .retrieveMemberById
     */
    fun getMemberById(userId: Long): Member? {
        return getMemberCache().getElementById(userId)
    }
    /**
     * Searches for a [net.dv8tion.jda.api.entities.Member] that has the matching Discord Tag.
     * <br></br>Format has to be in the form `Username#Discriminator` where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     * <br></br>This does not check the [nickname][net.dv8tion.jda.api.entities.Member.getNickname] of the member
     * but the username.
     *
     *
     * This will only check cached members!
     * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
     *
     *
     * This only checks users that are in this guild. If a user exists
     * with the tag that is not available in the [Member-Cache][.getMemberCache] it will not be detected.
     * <br></br>Currently Discord does not offer a way to retrieve a user by their discord tag.
     *
     * @param  tag
     * The Discord Tag in the format `Username#Discriminator`
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided tag is null or not in the described format
     *
     * @return The [net.dv8tion.jda.api.entities.Member] for the discord tag or null if no member has the provided tag
     *
     * @see net.dv8tion.jda.api.JDA.getUserByTag
     */
    @ForRemoval @Deprecated(
        """This will become obsolete in the future.
                  Discriminators are being phased out and replaced by globally unique usernames.
                  For more information, see <a href=""" https ://support.discord.com/hc/en-us/articles/12620128861463" target="_blank">New Usernames &amp; Display Names</a>.") open fun /*@@dskabj@@*/getMemberByTag(@javax.annotation.Nonnull tag:/*@@bslmjn@@*/kotlin.String?): /*@@jvnvgq@@*/net.dv8tion.jda.api.entities.Member? {
        var user: net.dv8tion.jda.api.entities.User? = getJDA().getUserByTag(tag)
    return if (user == null)null else getMember(user)
}
/**
 * Searches for a [net.dv8tion.jda.api.entities.Member] that has the matching Discord Tag.
 * <br></br>Format has to be in the form `Username#Discriminator` where the
 * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
 * must be exactly 4 digits.
 * <br></br>This does not check the [nickname][net.dv8tion.jda.api.entities.Member.getNickname] of the member
 * but the username.
 *
 *
 * This will only check cached members!
 * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 *
 *
 * This only checks users that are in this guild. If a user exists
 * with the tag that is not available in the [Member-Cache][.getMemberCache] it will not be detected.
 * <br></br>Currently Discord does not offer a way to retrieve a user by their discord tag.
 *
 * @param  username
 * The name of the user
 * @param  discriminator
 * The discriminator of the user
 *
 * @throws java.lang.IllegalArgumentException
 * If the provided arguments are null or not in the described format
 *
 * @return The [net.dv8tion.jda.api.entities.Member] for the discord tag or null if no member has the provided tag
 *
 * @see .getMemberByTag
 */
@ForRemoval @Deprecated(
    """This will become obsolete in the future.
                  Discriminators are being phased out and replaced by globally unique usernames.
                  For more information, see <a href=""" https ://support.discord.com/hc/en-us/articles/12620128861463" target="_blank">New Usernames &amp; Display Names</a>.") open fun /*@@drsssm@@*/getMemberByTag(@javax.annotation.Nonnull username:/*@@bslmjn@@*/kotlin.String?, @javax.annotation.Nonnull discriminator:/*@@bslmjn@@*/kotlin.String?): /*@@jvnvgq@@*/net.dv8tion.jda.api.entities.Member? {
    var user: net.dv8tion.jda.api.entities.User? = getJDA().getUserByTag(username, discriminator)
return if (user == null)null else getMember(user)
}
/**
 * A list of all [Members][net.dv8tion.jda.api.entities.Member] in this Guild.
 * <br></br>The Members are not provided in any particular order.
 *
 *
 * This will only check cached members!
 * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 *
 *
 * This copies the backing store into a list. This means every call
 * creates a new list with O(n) complexity. It is recommended to store this into
 * a local variable or use [.getMemberCache] and use its more efficient
 * versions of handling these values.
 *
 * @return Immutable list of all **cached** members in this Guild.
 *
 * @see .loadMembers
 */
@Nonnull
fun getMembers(): List<Member?>? {
    return getMemberCache().asList()
}

/**
 * Gets a list of all [Members][net.dv8tion.jda.api.entities.Member] who have the same name as the one provided.
 * <br></br>This compares against [net.dv8tion.jda.api.entities.Member.getUser][.getName()][net.dv8tion.jda.api.entities.User.getName]
 * <br></br>If there are no [Members][net.dv8tion.jda.api.entities.Member] with the provided name, then this returns an empty list.
 *
 *
 * This will only check cached members!
 * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 *
 * @param  name
 * The name used to filter the returned Members.
 * @param  ignoreCase
 * Determines if the comparison ignores case when comparing. True - case insensitive.
 *
 * @throws IllegalArgumentException
 * If the provided name is null
 *
 * @return Possibly-empty immutable list of all Members with the same name as the name provided.
 *
 * @see .retrieveMembersByPrefix
 * @incubating This will be replaced in the future when the rollout of globally unique usernames has been completed.
 */
@Nonnull
@Incubating
fun getMembersByName(@Nonnull name: String?, ignoreCase: Boolean): List<Member?>? {
    return getMemberCache().getElementsByUsername(name!!, ignoreCase)
}

/**
 * Gets a list of all [Members][net.dv8tion.jda.api.entities.Member] who have the same nickname as the one provided.
 * <br></br>This compares against [Member.getNickname]. If a Member does not have a nickname, the comparison results as false.
 * <br></br>If there are no [Members][net.dv8tion.jda.api.entities.Member] with the provided name, then this returns an empty list.
 *
 *
 * This will only check cached members!
 * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 *
 * @param  nickname
 * The nickname used to filter the returned Members.
 * @param  ignoreCase
 * Determines if the comparison ignores case when comparing. True - case insensitive.
 *
 * @return Possibly-empty immutable list of all Members with the same nickname as the nickname provided.
 *
 * @see .retrieveMembersByPrefix
 */
@Nonnull
fun getMembersByNickname(nickname: String?, ignoreCase: Boolean): List<Member?>? {
    return getMemberCache().getElementsByNickname(nickname, ignoreCase)
}

/**
 * Gets a list of all [Members][net.dv8tion.jda.api.entities.Member] who have the same effective name as the one provided.
 * <br></br>This compares against [net.dv8tion.jda.api.entities.Member.getEffectiveName].
 * <br></br>If there are no [Members][net.dv8tion.jda.api.entities.Member] with the provided name, then this returns an empty list.
 *
 *
 * This will only check cached members!
 * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 *
 * @param  name
 * The name used to filter the returned Members.
 * @param  ignoreCase
 * Determines if the comparison ignores case when comparing. True - case insensitive.
 *
 * @throws IllegalArgumentException
 * If the provided name is null
 *
 * @return Possibly-empty immutable list of all Members with the same effective name as the name provided.
 *
 * @see .retrieveMembersByPrefix
 */
@Nonnull
fun getMembersByEffectiveName(@Nonnull name: String?, ignoreCase: Boolean): List<Member?>? {
    return getMemberCache().getElementsByName(name!!, ignoreCase)
}

/**
 * Gets a list of [Members][net.dv8tion.jda.api.entities.Member] that have all [Roles][Role] provided.
 * <br></br>If there are no [Members][net.dv8tion.jda.api.entities.Member] with all provided roles, then this returns an empty list.
 *
 *
 * This will only check cached members!
 * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 *
 * @param  roles
 * The [Roles][Role] that a [Member][net.dv8tion.jda.api.entities.Member]
 * must have to be included in the returned list.
 *
 * @throws java.lang.IllegalArgumentException
 * If a provided [Role] is from a different guild or null.
 *
 * @return Possibly-empty immutable list of Members with all provided Roles.
 *
 * @see .findMembersWithRoles
 */
@Nonnull
fun getMembersWithRoles(@Nonnull vararg roles: Role?): List<Member?>? {
    Checks.notNull(roles, "Roles")
    return getMembersWithRoles(Arrays.asList(*roles))
}

/**
 * Gets a list of [Members][net.dv8tion.jda.api.entities.Member] that have all provided [Roles][Role].
 * <br></br>If there are no [Members][net.dv8tion.jda.api.entities.Member] with all provided roles, then this returns an empty list.
 *
 *
 * This will only check cached members!
 * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 *
 * @param  roles
 * The [Roles][Role] that a [Member][net.dv8tion.jda.api.entities.Member]
 * must have to be included in the returned list.
 *
 * @throws java.lang.IllegalArgumentException
 * If a provided [Role] is from a different guild or null.
 *
 * @return Possibly-empty immutable list of Members with all provided Roles.
 *
 * @see .findMembersWithRoles
 */
@Nonnull
fun getMembersWithRoles(@Nonnull roles: Collection<Role?>): List<Member?>? {
    Checks.noneNull(roles, "Roles")
    for (role in roles) Checks.check(this == role.getGuild(), "All roles must be from the same guild!")
    return getMemberCache().getElementsWithRoles(roles)
}

/**
 * [MemberCacheView][net.dv8tion.jda.api.utils.cache.MemberCacheView] for all cached
 * [Members][net.dv8tion.jda.api.entities.Member] of this Guild.
 *
 *
 * This will only provide cached members!
 * <br></br>See [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 *
 * @return [MemberCacheView][net.dv8tion.jda.api.utils.cache.MemberCacheView]
 *
 * @see .loadMembers
 */
@Nonnull
fun getMemberCache(): MemberCacheView

/**
 * Sorted [SnowflakeCacheView] of
 * all cached [ScheduledEvents][ScheduledEvent] of this Guild.
 * <br></br>Scheduled events are sorted by their start time, and events that start at the same time
 * are sorted by their snowflake ID.
 *
 *
 * This requires [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 *
 * @return [SortedSnowflakeCacheView]
 */
@Nonnull
fun getScheduledEventCache(): SortedSnowflakeCacheView<ScheduledEvent?>

/**
 * Gets a list of all [ScheduledEvents][ScheduledEvent] in this Guild that have the same
 * name as the one provided.
 * <br></br>If there are no [ScheduledEvents][ScheduledEvent] with the provided name,
 * then this returns an empty list.
 *
 *
 * This requires [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 *
 * @param  name
 * The name used to filter the returned [ScheduledEvent] objects.
 * @param  ignoreCase
 * Determines if the comparison ignores case when comparing. True - case insensitive.
 *
 * @throws java.lang.IllegalArgumentException
 * If the name is blank, empty or `null`
 *
 * @return Possibly-empty immutable list of all ScheduledEvent names that match the provided name.
 */
@Nonnull
fun getScheduledEventsByName(@Nonnull name: String?, ignoreCase: Boolean): List<ScheduledEvent?>? {
    return getScheduledEventCache().getElementsByName(name!!, ignoreCase)
}

/**
 * Gets a [ScheduledEvent] from this guild that has the same id as the
 * one provided. This method is similar to [JDA.getScheduledEventById], but it only
 * checks this specific Guild for a scheduled event.
 * <br></br>If there is no [ScheduledEvent] with an id that matches the provided
 * one, then this returns `null`.
 *
 *
 * This requires [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 *
 * @param  id
 * The id of the [ScheduledEvent].
 *
 * @throws java.lang.NumberFormatException
 * If the provided `id` cannot be parsed by [Long.parseLong]
 *
 * @return Possibly-null [ScheduledEvent] with matching id.
 */
fun getScheduledEventById(@Nonnull id: String?): ScheduledEvent? {
    return getScheduledEventCache().getElementById(id!!)
}

/**
 * Gets a [ScheduledEvent] from this guild that has the same id as the
 * one provided. This method is similar to [JDA.getScheduledEventById], but it only
 * checks this specific Guild for a scheduled event.
 * <br></br>If there is no [ScheduledEvent] with an id that matches the provided
 * one, then this returns `null`.
 *
 *
 * This requires [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 *
 * @param  id
 * The id of the [ScheduledEvent].
 *
 * @return Possibly-null [ScheduledEvent] with matching id.
 */
fun getScheduledEventById(id: Long): ScheduledEvent? {
    return getScheduledEventCache().getElementById(id)
}

/**
 * Gets all [ScheduledEvents][ScheduledEvent] in this guild.
 * <br></br>Scheduled events are sorted by their start time, and events that start at the same time
 * are sorted by their snowflake ID.
 *
 *
 * This copies the backing store into a list. This means every call
 * creates a new list with O(n) complexity. It is recommended to store this into
 * a local variable or use [.getScheduledEventCache] and use its more efficient
 * versions of handling these values.
 *
 *
 * This requires [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 *
 * @return Possibly-empty immutable List of [ScheduledEvents][ScheduledEvent].
 */
@Nonnull
fun getScheduledEvents(): List<ScheduledEvent?>? {
    return getScheduledEventCache().asList()
}

@Nonnull
fun getStageChannelCache(): SortedSnowflakeCacheView<StageChannel?>?

@Nonnull
fun getThreadChannelCache(): SortedSnowflakeCacheView<ThreadChannel?>?

@Nonnull
fun getCategoryCache(): SortedSnowflakeCacheView<Category?>?

@Nonnull
fun getTextChannelCache(): SortedSnowflakeCacheView<TextChannel?>?

@Nonnull
fun getNewsChannelCache(): SortedSnowflakeCacheView<NewsChannel?>?

@Nonnull
fun getVoiceChannelCache(): SortedSnowflakeCacheView<VoiceChannel?>?

@Nonnull
fun getForumChannelCache(): SortedSnowflakeCacheView<ForumChannel?>?

/**
 * [SortedChannelCacheView] of [GuildChannel].
 *
 *
 * Provides cache access to all channels of this guild, including thread channels (unlike [.getChannels]).
 * The cache view attempts to provide a sorted list, based on how channels are displayed in the client.
 * Various methods like [SortedChannelCacheView.forEachUnordered] or [SortedChannelCacheView.lockedIterator]
 * bypass sorting for optimization reasons.
 *
 *
 * It is possible to filter the channels to more specific types using
 * [ChannelCacheView.getElementById] or [SortedChannelCacheView.ofType].
 *
 * @return [SortedChannelCacheView]
 */
@Nonnull
fun getChannelCache(): SortedChannelCacheView<GuildChannel?>?

/**
 * Populated list of [channels][GuildChannel] for this guild.
 * <br></br>This includes all types of channels, except for threads.
 * <br></br>This includes hidden channels by default,
 * you can use [getChannels(false)][.getChannels] to exclude hidden channels.
 *
 *
 * The returned list is ordered in the same fashion as it would be by the official discord client.
 *
 *  1. TextChannel, ForumChannel, and NewsChannel without parent
 *  1. VoiceChannel and StageChannel without parent
 *  1. Categories
 *
 *  1. TextChannel, ForumChannel, and NewsChannel with category as parent
 *  1. VoiceChannel and StageChannel with category as parent
 *
 *
 *
 *
 * @return Immutable list of channels for this guild
 *
 * @see .getChannels
 */
@Nonnull
fun getChannels(): List<GuildChannel?>? {
    return getChannels(true)
}

/**
 * Populated list of [channels][GuildChannel] for this guild.
 * <br></br>This includes all types of channels, except for threads.
 *
 *
 * The returned list is ordered in the same fashion as it would be by the official discord client.
 *
 *  1. TextChannel, ForumChannel, and NewsChannel without parent
 *  1. VoiceChannel and StageChannel without parent
 *  1. Categories
 *
 *  1. TextChannel, ForumChannel, and NewsChannel with category as parent
 *  1. VoiceChannel and StageChannel with category as parent
 *
 *
 *
 *
 * @param  includeHidden
 * Whether to include channels with denied [View Channel Permission][Permission.VIEW_CHANNEL]
 *
 * @return Immutable list of channels for this guild
 *
 * @see .getChannels
 */
@Nonnull
fun getChannels(includeHidden: Boolean): List<GuildChannel?>?

/**
 * Gets a [Role] from this guild that has the same id as the
 * one provided.
 * <br></br>If there is no [Role] with an id that matches the provided
 * one, then this returns `null`.
 *
 * @param  id
 * The id of the [Role].
 *
 * @throws java.lang.NumberFormatException
 * If the provided `id` cannot be parsed by [Long.parseLong]
 *
 * @return Possibly-null [Role] with matching id.
 */
fun getRoleById(@Nonnull id: String?): Role? {
    return getRoleCache().getElementById(id!!)
}

/**
 * Gets a [Role] from this guild that has the same id as the
 * one provided.
 * <br></br>If there is no [Role] with an id that matches the provided
 * one, then this returns `null`.
 *
 * @param  id
 * The id of the [Role].
 *
 * @return Possibly-null [Role] with matching id.
 */
fun getRoleById(id: Long): Role? {
    return getRoleCache().getElementById(id)
}

/**
 * Gets all [Roles][Role] in this [Guild][net.dv8tion.jda.api.entities.Guild].
 * <br></br>The roles returned will be sorted according to their position. The highest role being at index 0
 * and the lowest at the last index.
 *
 *
 * This copies the backing store into a list. This means every call
 * creates a new list with O(n) complexity. It is recommended to store this into
 * a local variable or use [.getRoleCache] and use its more efficient
 * versions of handling these values.
 *
 * @return An immutable List of [Roles][Role].
 */
@Nonnull
fun getRoles(): List<Role?>? {
    return getRoleCache().asList()
}

/**
 * Gets a list of all [Roles][Role] in this Guild that have the same
 * name as the one provided.
 * <br></br>If there are no [Roles][Role] with the provided name, then this returns an empty list.
 *
 * @param  name
 * The name used to filter the returned [Roles][Role].
 * @param  ignoreCase
 * Determines if the comparison ignores case when comparing. True - case insensitive.
 *
 * @return Possibly-empty immutable list of all Role names that match the provided name.
 */
@Nonnull
fun getRolesByName(@Nonnull name: String?, ignoreCase: Boolean): List<Role?>? {
    return getRoleCache().getElementsByName(name!!, ignoreCase)
}

/**
 * Looks up a role which is the integration role for a bot.
 * <br></br>These roles are created when the bot requested a list of permission in the authorization URL.
 *
 *
 * To check whether a role is a bot role you can use `role.getTags().isBot()` and you can use
 * [Role.RoleTags.getBotIdLong] to check which bot it applies to.
 *
 *
 * This requires [CacheFlag.ROLE_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.ROLE_TAGS] to be enabled.
 * See [JDABuilder.enableCache(...)][net.dv8tion.jda.api.JDABuilder.enableCache].
 *
 * @param  userId
 * The user id of the bot
 *
 * @return The bot role, or null if no role matches
 */
fun getRoleByBot(userId: Long): Role? {
    return getRoleCache().applyStream { stream: Stream<Role?> ->
        stream.filter { role: Role? -> role.getTags().getBotIdLong() == userId }
            .findFirst()
            .orElse(null)
    }
}

/**
 * Looks up a role which is the integration role for a bot.
 * <br></br>These roles are created when the bot requested a list of permission in the authorization URL.
 *
 *
 * To check whether a role is a bot role you can use `role.getTags().isBot()` and you can use
 * [Role.RoleTags.getBotIdLong] to check which bot it applies to.
 *
 *
 * This requires [CacheFlag.ROLE_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.ROLE_TAGS] to be enabled.
 * See [JDABuilder.enableCache(...)][net.dv8tion.jda.api.JDABuilder.enableCache].
 *
 * @param  userId
 * The user id of the bot
 *
 * @throws IllegalArgumentException
 * If the userId is null or not a valid snowflake
 *
 * @return The bot role, or null if no role matches
 */
fun getRoleByBot(@Nonnull userId: String?): Role? {
    return getRoleByBot(MiscUtil.parseSnowflake(userId))
}

/**
 * Looks up a role which is the integration role for a bot.
 * <br></br>These roles are created when the bot requested a list of permission in the authorization URL.
 *
 *
 * To check whether a role is a bot role you can use `role.getTags().isBot()` and you can use
 * [Role.RoleTags.getBotIdLong] to check which bot it applies to.
 *
 *
 * This requires [CacheFlag.ROLE_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.ROLE_TAGS] to be enabled.
 * See [JDABuilder.enableCache(...)][net.dv8tion.jda.api.JDABuilder.enableCache].
 *
 * @param  user
 * The bot user
 *
 * @throws IllegalArgumentException
 * If null is provided
 *
 * @return The bot role, or null if no role matches
 */
fun getRoleByBot(@Nonnull user: User): Role? {
    Checks.notNull(user, "User")
    return getRoleByBot(user.getIdLong())
}

/**
 * Looks up the role which is the integration role for the currently connected bot (self-user).
 * <br></br>These roles are created when the bot requested a list of permission in the authorization URL.
 *
 *
 * To check whether a role is a bot role you can use `role.getTags().isBot()` and you can use
 * [Role.RoleTags.getBotIdLong] to check which bot it applies to.
 *
 *
 * This requires [CacheFlag.ROLE_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.ROLE_TAGS] to be enabled.
 * See [JDABuilder.enableCache(...)][net.dv8tion.jda.api.JDABuilder.enableCache].
 *
 * @return The bot role, or null if no role matches
 */
fun getBotRole(): Role? {
    return getRoleByBot(getJDA().getSelfUser())
}

/**
 * Looks up the role which is the booster role of this guild.
 * <br></br>These roles are created when the first user boosts this guild.
 *
 *
 * To check whether a role is a booster role you can use `role.getTags().isBoost()`.
 *
 *
 * This requires [CacheFlag.ROLE_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.ROLE_TAGS] to be enabled.
 * See [JDABuilder.enableCache(...)][net.dv8tion.jda.api.JDABuilder.enableCache].
 *
 * @return The boost role, or null if no role matches
 */
fun getBoostRole(): Role? {
    return getRoleCache().applyStream { stream: Stream<Role?> ->
        stream.filter { role: Role? -> role.getTags().isBoost() }
            .findFirst()
            .orElse(null)
    }
}

/**
 * Sorted [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
 * all cached [Roles][Role] of this Guild.
 * <br></br>Roles are sorted according to their position.
 *
 * @return [SortedSnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView]
 */
@Nonnull
fun getRoleCache(): SortedSnowflakeCacheView<Role?>

/**
 * Gets a [RichCustomEmoji] from this guild that has the same id as the
 * one provided.
 * <br></br>If there is no [RichCustomEmoji] with an id that matches the provided
 * one, then this returns `null`.
 *
 *
 * **Unicode emojis are not included as [RichCustomEmoji]!**
 *
 *
 * This requires the [CacheFlag.EMOJI][net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] to be enabled!
 *
 * @param  id
 * the emoji id
 *
 * @throws java.lang.NumberFormatException
 * If the provided `id` cannot be parsed by [Long.parseLong]
 *
 * @return An Emoji matching the specified id
 *
 * @see .retrieveEmojiById
 */
fun getEmojiById(@Nonnull id: String?): RichCustomEmoji? {
    return getEmojiCache().getElementById(id!!)
}

/**
 * Gets an [RichCustomEmoji] from this guild that has the same id as the
 * one provided.
 * <br></br>If there is no [RichCustomEmoji] with an id that matches the provided
 * one, then this returns `null`.
 *
 *
 * **Unicode emojis are not included as [RichCustomEmoji]!**
 *
 *
 * This requires the [CacheFlag.EMOJI][net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] to be enabled!
 *
 * @param  id
 * the emoji id
 *
 * @return An emoji matching the specified id
 *
 * @see .retrieveEmojiById
 */
fun getEmojiById(id: Long): RichCustomEmoji? {
    return getEmojiCache().getElementById(id)
}

/**
 * Gets all [Custom Emojis][RichCustomEmoji] belonging to this [Guild][net.dv8tion.jda.api.entities.Guild].
 * <br></br>Emojis are not ordered in any specific way in the returned list.
 *
 *
 * **Unicode emojis are not included as [RichCustomEmoji]!**
 *
 *
 * This copies the backing store into a list. This means every call
 * creates a new list with O(n) complexity. It is recommended to store this into
 * a local variable or use [.getEmojiCache] and use its more efficient
 * versions of handling these values.
 *
 *
 * This requires the [CacheFlag.EMOJI][net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] to be enabled!
 *
 * @return An immutable List of [Custom Emojis][RichCustomEmoji].
 *
 * @see .retrieveEmojis
 */
@Nonnull
fun getEmojis(): List<RichCustomEmoji?>? {
    return getEmojiCache().asList()
}

/**
 * Gets a list of all [Custom Emojis][RichCustomEmoji] in this Guild that have the same
 * name as the one provided.
 * <br></br>If there are no [Emojis][RichCustomEmoji] with the provided name, then this returns an empty list.
 *
 *
 * **Unicode emojis are not included as [RichCustomEmoji]!**
 *
 *
 * This requires the [CacheFlag.EMOJI][net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] to be enabled!
 *
 * @param  name
 * The name used to filter the returned [Emojis][RichCustomEmoji]. Without colons.
 * @param  ignoreCase
 * Determines if the comparison ignores case when comparing. True - case insensitive.
 *
 * @return Possibly-empty immutable list of all Emojis that match the provided name.
 */
@Nonnull
fun getEmojisByName(@Nonnull name: String?, ignoreCase: Boolean): List<RichCustomEmoji?>? {
    return getEmojiCache().getElementsByName(name!!, ignoreCase)
}

/**
 * [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
 * all cached [Custom Emojis][RichCustomEmoji] of this Guild.
 * <br></br>This will be empty if [net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] is disabled.
 *
 *
 * This requires the [CacheFlag.EMOJI][net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] to be enabled!
 *
 * @return [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
 *
 * @see .retrieveEmojis
 */
@Nonnull
fun getEmojiCache(): SnowflakeCacheView<RichCustomEmoji?>

/**
 * Gets a [GuildSticker][net.dv8tion.jda.api.entities.sticker.GuildSticker] from this guild that has the same id as the
 * one provided.
 * <br></br>If there is no [GuildSticker][net.dv8tion.jda.api.entities.sticker.GuildSticker] with an id that matches the provided
 * one, then this returns `null`.
 *
 *
 * This requires the [CacheFlag.STICKER][net.dv8tion.jda.api.utils.cache.CacheFlag.STICKER] to be enabled!
 *
 * @param  id
 * the sticker id
 *
 * @throws java.lang.NumberFormatException
 * If the provided `id` cannot be parsed by [Long.parseLong]
 *
 * @return A Sticker matching the specified id
 *
 * @see .retrieveSticker
 */
fun getStickerById(@Nonnull id: String?): GuildSticker? {
    return getStickerCache().getElementById(id!!)
}

/**
 * Gets a [GuildSticker][net.dv8tion.jda.api.entities.sticker.GuildSticker] from this guild that has the same id as the
 * one provided.
 * <br></br>If there is no [GuildSticker][net.dv8tion.jda.api.entities.sticker.GuildSticker] with an id that matches the provided
 * one, then this returns `null`.
 *
 *
 * This requires the [CacheFlag.STICKER][net.dv8tion.jda.api.utils.cache.CacheFlag.STICKER] to be enabled!
 *
 * @param  id
 * the sticker id
 *
 * @return A Sticker matching the specified id
 *
 * @see .retrieveSticker
 */
fun getStickerById(id: Long): GuildSticker? {
    return getStickerCache().getElementById(id)
}

/**
 * Gets all custom [GuildStickers][net.dv8tion.jda.api.entities.sticker.GuildSticker] belonging to this [Guild][net.dv8tion.jda.api.entities.Guild].
 * <br></br>GuildStickers are not ordered in any specific way in the returned list.
 *
 *
 * This copies the backing store into a list. This means every call
 * creates a new list with O(n) complexity. It is recommended to store this into
 * a local variable or use [.getStickerCache] and use its more efficient
 * versions of handling these values.
 *
 *
 * This requires the [CacheFlag.STICKER][net.dv8tion.jda.api.utils.cache.CacheFlag.STICKER] to be enabled!
 *
 * @return An immutable List of [GuildStickers][net.dv8tion.jda.api.entities.sticker.GuildSticker].
 *
 * @see .retrieveStickers
 */
@Nonnull
fun getStickers(): List<GuildSticker?>? {
    return getStickerCache().asList()
}

/**
 * Gets a list of all [GuildStickers][net.dv8tion.jda.api.entities.sticker.GuildSticker] in this Guild that have the same
 * name as the one provided.
 * <br></br>If there are no [GuildStickers][net.dv8tion.jda.api.entities.sticker.GuildSticker] with the provided name, then this returns an empty list.
 *
 *
 * This requires the [CacheFlag.STICKER][net.dv8tion.jda.api.utils.cache.CacheFlag.STICKER] to be enabled!
 *
 * @param  name
 * The name used to filter the returned [GuildStickers][net.dv8tion.jda.api.entities.sticker.GuildSticker]. Without colons.
 * @param  ignoreCase
 * Determines if the comparison ignores case when comparing. True - case insensitive.
 *
 * @return Possibly-empty immutable list of all Stickers that match the provided name.
 */
@Nonnull
fun getStickersByName(@Nonnull name: String?, ignoreCase: Boolean): List<GuildSticker?>? {
    return getStickerCache().getElementsByName(name!!, ignoreCase)
}

/**
 * [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
 * all cached [GuildStickers][net.dv8tion.jda.api.entities.sticker.GuildSticker] of this Guild.
 * <br></br>This will be empty if [net.dv8tion.jda.api.utils.cache.CacheFlag.STICKER] is disabled.
 *
 *
 * This requires the [CacheFlag.STICKER][net.dv8tion.jda.api.utils.cache.CacheFlag.STICKER] to be enabled!
 *
 * @return [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
 *
 * @see .retrieveStickers
 */
@Nonnull
fun getStickerCache(): SnowflakeCacheView<GuildSticker?>

/**
 * Retrieves an immutable list of Custom Emojis together with their respective creators.
 *
 *
 * Note that [RichCustomEmoji.getOwner] is only available if the currently
 * logged in account has [Permission.MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS].
 *
 * @return [RestAction] - Type: List of [RichCustomEmoji]
 */
@Nonnull
@CheckReturnValue
fun retrieveEmojis(): RestAction<List<RichCustomEmoji?>?>?

/**
 * Retrieves a custom emoji together with its respective creator.
 * <br></br>**This does not include unicode emoji.**
 *
 *
 * Note that [RichCustomEmoji.getOwner] is only available if the currently
 * logged in account has [Permission.MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS].
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
 * <br></br>If the provided id does not correspond to an emoji in this guild
 *
 *
 * @param  id
 * The emoji id
 *
 * @throws IllegalArgumentException
 * If the provided id is not a valid snowflake
 *
 * @return [RestAction] - Type: [RichCustomEmoji]
 */
@Nonnull
@CheckReturnValue
fun retrieveEmojiById(@Nonnull id: String?): RestAction<RichCustomEmoji?>?

/**
 * Retrieves a Custom Emoji together with its respective creator.
 *
 *
 * Note that [RichCustomEmoji.getOwner] is only available if the currently
 * logged in account has [Permission.MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS].
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
 * <br></br>If the provided id does not correspond to an emoji in this guild
 *
 *
 * @param  id
 * The emoji id
 *
 * @return [RestAction] - Type: [RichCustomEmoji]
 */
@Nonnull
@CheckReturnValue
fun retrieveEmojiById(id: Long): RestAction<RichCustomEmoji?>? {
    return retrieveEmojiById(java.lang.Long.toUnsignedString(id))
}

/**
 * Retrieves a custom emoji together with its respective creator.
 *
 *
 * Note that [RichCustomEmoji.getOwner] is only available if the currently
 * logged in account has [Permission.MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS].
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
 * <br></br>If the provided emoji does not correspond to an emoji in this guild anymore
 *
 *
 * @param  emoji
 * The emoji reference to retrieve
 *
 * @return [RestAction] - Type: [RichCustomEmoji]
 */
@Nonnull
@CheckReturnValue
fun retrieveEmoji(@Nonnull emoji: CustomEmoji): RestAction<RichCustomEmoji?>? {
    Checks.notNull(emoji, "Emoji")
    if (emoji is RichCustomEmoji && emoji.guild != null) Checks.check(
        emoji.guild == this, "Emoji must be from the same Guild!"
    )
    val jda = getJDA()
    return DeferredRestAction<RichCustomEmoji?, RestAction<RichCustomEmoji?>?>(jda, RichCustomEmoji::class.java,
        Supplier<RichCustomEmoji?> {
            if (emoji is RichCustomEmoji) {
                val richEmoji = emoji
                if (richEmoji.owner != null || !getSelfMember().hasPermission(Permission.MANAGE_GUILD_EXPRESSIONS)) return@Supplier richEmoji
            }
            null
        }) { retrieveEmojiById(emoji.id) }
}

/**
 * Retrieves all the stickers from this guild.
 * <br></br>This also includes [unavailable][GuildSticker.isAvailable] stickers.
 *
 * @return [RestAction] - Type: List of [GuildSticker]
 */
@Nonnull
@CheckReturnValue
fun retrieveStickers(): RestAction<List<GuildSticker?>?>?

/**
 * Attempts to retrieve a [GuildSticker] object for this guild based on the provided snowflake reference.
 *
 *
 * The returned [RestAction][net.dv8tion.jda.api.requests.RestAction] can encounter the following Discord errors:
 *
 *  * [UNKNOWN_STICKER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_STICKER]
 * <br></br>Occurs when the provided id does not refer to a sticker known by Discord.
 *
 *
 * @param  sticker
 * The reference of the requested [Sticker].
 * <br></br>Can be [RichSticker], [StickerItem], or [Sticker.fromId].
 *
 * @throws IllegalArgumentException
 * If null is provided
 *
 * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [GuildSticker]
 * <br></br>On request, gets the sticker with id matching provided id from Discord.
 */
@Nonnull
@CheckReturnValue
fun retrieveSticker(@Nonnull sticker: StickerSnowflake?): RestAction<GuildSticker?>?

/**
 * Modify a sticker using [GuildStickerManager].
 * <br></br>You can update multiple fields at once, by calling the respective setters before executing the request.
 *
 *
 * The returned [RestAction][net.dv8tion.jda.api.requests.RestAction] can encounter the following Discord errors:
 *
 *  * [UNKNOWN_STICKER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_STICKER]
 * <br></br>Occurs when the provided id does not refer to a sticker known by Discord.
 *
 *
 * @throws IllegalArgumentException
 * If null is provided
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account does not have [MANAGE_GUILD_EXPRESSIONS][Permission.MANAGE_GUILD_EXPRESSIONS] in the guild.
 *
 * @return [GuildStickerManager]
 */
@Nonnull
@CheckReturnValue
fun editSticker(@Nonnull sticker: StickerSnowflake?): GuildStickerManager?

/**
 * Retrieves an immutable list of the currently banned [Users][net.dv8tion.jda.api.entities.User].
 * <br></br>If you wish to ban or unban a user, use either [.ban] or
 * [.unban].
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The ban list cannot be fetched due to a permission discrepancy
 *
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.BAN_MEMBERS] permission.
 *
 * @return The [BanPaginationAction][net.dv8tion.jda.api.requests.restaction.pagination.BanPaginationAction] of the guild's bans.
 */
@Nonnull
@CheckReturnValue
fun retrieveBanList(): BanPaginationAction?

/**
 * Retrieves a [Ban][net.dv8tion.jda.api.entities.Guild.Ban] of the provided [UserSnowflake].
 * <br></br>If you wish to ban or unban a user, use either [.ban] or [.unban].
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The ban list cannot be fetched due to a permission discrepancy
 *
 *  * [UNKNOWN_BAN][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_BAN]
 * <br></br>Either the ban was removed before finishing the task or it did not exist in the first place
 *
 *
 * @param  user
 * The [UserSnowflake] for the banned user.
 * This can be a user instance or [User.fromId].
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.BAN_MEMBERS] permission.
 *
 * @return [RestAction] - Type: [Ban][net.dv8tion.jda.api.entities.Guild.Ban]
 * <br></br>An unmodifiable ban object for the user banned from this guild
 */
@Nonnull
@CheckReturnValue
fun retrieveBan(@Nonnull user: UserSnowflake?): RestAction<Ban?>?

/**
 * The method calculates the amount of Members that would be pruned if [.prune] was executed.
 * Prunability is determined by a Member being offline for at least *days* days.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The prune count cannot be fetched due to a permission discrepancy
 *
 *
 * @param  days
 * Minimum number of days since a member has been offline to get affected.
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the account doesn't have [KICK_MEMBER][net.dv8tion.jda.api.Permission.KICK_MEMBERS] Permission.
 * @throws IllegalArgumentException
 * If the provided days are less than `1` or more than `30`
 *
 * @return [RestAction] - Type: Integer
 * <br></br>The amount of Members that would be affected.
 */
@Nonnull
@CheckReturnValue
fun retrievePrunableMemberCount(days: Int): RestAction<Int?>?

/**
 * The @everyone [Role] of this [Guild][net.dv8tion.jda.api.entities.Guild].
 * <br></br>This role is special because its [position][Role.getPosition] is calculated as
 * `-1`. All other role positions are 0 or greater. This implies that the public role is **always** below
 * any custom roles created in this Guild. Additionally, all members of this guild are implied to have this role so
 * it is not included in the list returned by [Member.getRoles()][net.dv8tion.jda.api.entities.Member.getRoles].
 * <br></br>The ID of this Role is the Guild's ID thus it is equivalent to using [getRoleById(getIdLong())][.getRoleById].
 *
 * @return The @everyone [Role]
 */
@Nonnull
fun getPublicRole(): Role?

/**
 * The default [StandardGuildChannel] for a [Guild][net.dv8tion.jda.api.entities.Guild].
 * <br></br>This is the channel that the Discord client will default to opening when a Guild is opened for the first time when accepting an invite
 * that is not directed at a specific [channel][IInviteContainer].
 *
 *
 * Note: This channel is the first channel in the guild (ordered by position) that the [.getPublicRole]
 * has the [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] in.
 *
 * @return The [channel][StandardGuildChannel] representing the default channel for this guild
 */
fun getDefaultChannel(): DefaultGuildChannelUnion?

/**
 * Returns the [GuildManager] for this Guild, used to modify
 * all properties and settings of the Guild.
 * <br></br>You modify multiple fields in one request by chaining setters before calling [RestAction.queue()][RestAction.queue].
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account does not have [Permission.MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER]
 *
 * @return The Manager of this Guild
 */
@Nonnull
fun getManager(): GuildManager?

/**
 * Returns whether this Guild has its boost progress bar shown.
 *
 * @return True, if this Guild has its boost progress bar shown
 */
fun isBoostProgressBarEnabled(): Boolean

/**
 * A [PaginationAction] implementation
 * that allows to [iterate][Iterable] over all [AuditLogEntries][net.dv8tion.jda.api.audit.AuditLogEntry] of
 * this Guild.
 * <br></br>This iterates from the most recent action to the first logged one. (Limit 90 days into history by discord api)
 *
 *
 * **Examples**<br></br>
 * <pre>`public void logBan(GuildBanEvent event) {
 * Guild guild = event.getGuild();
 * List<TextChannel> modLog = guild.getTextChannelsByName("mod-log", true);
 * guild.retrieveAuditLogs()
 * .type(ActionType.BAN) // filter by type
 * .limit(1)
 * .queue(list -> {
 * if (list.isEmpty()) return;
 * AuditLogEntry entry = list.get(0);
 * String message = String.format("%#s banned %#s with reason %s",
 * entry.getUser(), event.getUser(), entry.getReason());
 * modLog.forEach(channel ->
 * channel.sendMessage(message).queue()
 * );
 * });
 * }
`</pre> *
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account
 * does not have the permission [VIEW_AUDIT_LOGS][net.dv8tion.jda.api.Permission.VIEW_AUDIT_LOGS]
 *
 * @return [AuditLogPaginationAction]
 */
@Nonnull
@CheckReturnValue
fun retrieveAuditLogs(): AuditLogPaginationAction?

/**
 * Used to leave a Guild. If the currently logged in account is the owner of this guild ([net.dv8tion.jda.api.entities.Guild.getOwner])
 * then ownership of the Guild needs to be transferred to a different [Member][net.dv8tion.jda.api.entities.Member]
 * before leaving using [.transferOwnership].
 *
 * @throws java.lang.IllegalStateException
 * Thrown if the currently logged in account is the Owner of this Guild.
 *
 * @return [RestAction] - Type: [java.lang.Void]
 */
@Nonnull
@CheckReturnValue
fun leave(): RestAction<Void?>?

/**
 * Used to completely delete a Guild. This can only be done if the currently logged in account is the owner of the Guild.
 * <br></br>If the account has MFA enabled, use [.delete] instead to provide the MFA code.
 *
 * @throws net.dv8tion.jda.api.exceptions.PermissionException
 * Thrown if the currently logged in account is not the owner of this Guild.
 * @throws java.lang.IllegalStateException
 * If the currently logged in account has MFA enabled. ([net.dv8tion.jda.api.entities.SelfUser.isMfaEnabled]).
 *
 * @return [RestAction] - Type: [java.lang.Void]
 */
@Nonnull
@CheckReturnValue
fun delete(): RestAction<Void?>?

/**
 * Used to completely delete a guild. This can only be done if the currently logged in account is the owner of the Guild.
 * <br></br>This method is specifically used for when MFA is enabled on the logged in account [SelfUser.isMfaEnabled].
 * If MFA is not enabled, use [.delete].
 *
 * @param  mfaCode
 * The Multifactor Authentication code generated by an app like
 * [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2).
 * <br></br>**This is not the MFA token given to you by Discord.** The code is typically 6 characters long.
 *
 * @throws net.dv8tion.jda.api.exceptions.PermissionException
 * Thrown if the currently logged in account is not the owner of this Guild.
 * @throws java.lang.IllegalArgumentException
 * If the provided `mfaCode` is `null` or empty when [SelfUser.isMfaEnabled] is true.
 *
 * @return [RestAction] - Type: [java.lang.Void]
 */
@Nonnull
@CheckReturnValue
fun delete(mfaCode: String?): RestAction<Void?>?

/**
 * The [AudioManager][net.dv8tion.jda.api.managers.AudioManager] that represents the
 * audio connection for this Guild.
 * <br></br>If no AudioManager exists for this Guild, this will create a new one.
 * <br></br>This operation is synchronized on all audio managers for this JDA instance,
 * this means that calling getAudioManager() on any other guild while a thread is accessing this method may be locked.
 *
 * @throws IllegalStateException
 * If [GatewayIntent.GUILD_VOICE_STATES] is disabled
 *
 * @return The AudioManager for this Guild.
 *
 * @see net.dv8tion.jda.api.JDA.getAudioManagerCache
 */
@Nonnull
fun getAudioManager(): AudioManager?

/**
 * Once the currently logged in account is connected to a [StageChannel],
 * this will trigger a [Request-to-Speak][GuildVoiceState.getRequestToSpeakTimestamp] (aka raise your hand).
 *
 *
 * This will set an internal flag to automatically request to speak once the bot joins a stage channel.
 * <br></br>You can use [.cancelRequestToSpeak] to move back to the audience or cancel your pending request.
 *
 *
 * If the self member has [Permission.VOICE_MUTE_OTHERS] this will immediately promote them to speaker.
 *
 *
 * Example:
 * <pre>`stageChannel.createStageInstance("Talent Show").queue()
 * guild.requestToSpeak(); // Set request to speak flag
 * guild.getAudioManager().openAudioConnection(stageChannel); // join the channel
`</pre> *
 *
 * @return [Task] representing the request to speak.
 * Calling [Task.get] can result in deadlocks and should be avoided at all times.
 *
 * @see .cancelRequestToSpeak
 */
@Nonnull
fun requestToSpeak(): Task<Void?>?

/**
 * Cancels the [Request-to-Speak][.requestToSpeak].
 * <br></br>This can also be used to move back to the audience if you are currently a speaker.
 *
 *
 * If there is no request to speak or the member is not currently connected to a [StageChannel], this does nothing.
 *
 * @return [Task] representing the request to speak cancellation.
 * Calling [Task.get] can result in deadlocks and should be avoided at all times.
 *
 * @see .requestToSpeak
 */
@Nonnull
fun cancelRequestToSpeak(): Task<Void?>?

/**
 * Returns the [JDA][net.dv8tion.jda.api.JDA] instance of this Guild
 *
 * @return the corresponding JDA instance
 */
@Nonnull
fun getJDA(): JDA

/**
 * Retrieves all [Invites][net.dv8tion.jda.api.entities.Invite] for this guild.
 * <br></br>Requires [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in this guild.
 * Will throw an [InsufficientPermissionException][net.dv8tion.jda.api.exceptions.InsufficientPermissionException] otherwise.
 *
 *
 * To get all invites for a [GuildChannel]
 * use [GuildChannel.retrieveInvites()][IInviteContainer.retrieveInvites]
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * if the account does not have [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in this Guild.
 *
 * @return [RestAction] - Type: List&lt;[Invite][net.dv8tion.jda.api.entities.Invite]&gt;
 * <br></br>The list of expanded Invite objects
 *
 * @see IInviteContainer.retrieveInvites
 */
@Nonnull
@CheckReturnValue
fun retrieveInvites(): RestAction<List<Invite?>?>?

/**
 * Retrieves all [Templates][net.dv8tion.jda.api.entities.templates.Template] for this guild.
 * <br></br>Requires [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in this guild.
 * Will throw an [InsufficientPermissionException][net.dv8tion.jda.api.exceptions.InsufficientPermissionException] otherwise.
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * if the account does not have [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in this Guild.
 *
 * @return [RestAction] - Type: List&lt;[Template][net.dv8tion.jda.api.entities.templates.Template]&gt;
 * <br></br>The list of Template objects
 */
@Nonnull
@CheckReturnValue
fun retrieveTemplates(): RestAction<List<Template?>?>?

/**
 * Used to create a new [Template][net.dv8tion.jda.api.entities.templates.Template] for this Guild.
 * <br></br>Requires [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in this Guild.
 * Will throw an [InsufficientPermissionException][net.dv8tion.jda.api.exceptions.InsufficientPermissionException] otherwise.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [Guild already has a template][net.dv8tion.jda.api.requests.ErrorResponse.ALREADY_HAS_TEMPLATE]
 * <br></br>The guild already has a template.
 *
 *
 * @param  name
 * The name of the template
 * @param  description
 * The description of the template
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * if the account does not have [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in this Guild
 * @throws IllegalArgumentException
 * If the provided name is `null` or not between 1-100 characters long, or
 * if the provided description is not between 0-120 characters long
 *
 * @return [RestAction] - Type: [Template][net.dv8tion.jda.api.entities.templates.Template]
 * <br></br>The created Template object
 */
@Nonnull
@CheckReturnValue
fun createTemplate(@Nonnull name: String?, description: String?): RestAction<Template?>?

/**
 * Retrieves all [Webhooks][net.dv8tion.jda.api.entities.Webhook] for this Guild.
 * <br></br>Requires [MANAGE_WEBHOOKS][net.dv8tion.jda.api.Permission.MANAGE_WEBHOOKS] in this Guild.
 *
 *
 * To get all webhooks for a specific [TextChannel], use
 * [TextChannel.retrieveWebhooks]
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * if the account does not have [MANAGE_WEBHOOKS][net.dv8tion.jda.api.Permission.MANAGE_WEBHOOKS] in this Guild.
 *
 * @return [RestAction] - Type: List&lt;[Webhook][net.dv8tion.jda.api.entities.Webhook]&gt;
 * <br></br>A list of all Webhooks in this Guild.
 *
 * @see TextChannel.retrieveWebhooks
 */
@Nonnull
@CheckReturnValue
fun retrieveWebhooks(): RestAction<List<Webhook?>?>?

/**
 * Retrieves the [welcome screen][GuildWelcomeScreen] for this Guild.
 * <br></br>The welcome screen is shown to all members after joining the Guild.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [Unknown Guild Welcome Screen][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_GUILD_WELCOME_SCREEN]
 * <br></br>The guild has no welcome screen
 *  * [Missing Permissions][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The guild's welcome screen is disabled
 * and the currently logged in account doesn't have the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission
 *
 *
 * @return [RestAction] - Type: [GuildWelcomeScreen]
 * <br></br>The welcome screen for this Guild.
 */
@Nonnull
@CheckReturnValue
fun retrieveWelcomeScreen(): RestAction<GuildWelcomeScreen?>?

/**
 * A list containing the [GuildVoiceState][net.dv8tion.jda.api.entities.GuildVoiceState] of every [Member][net.dv8tion.jda.api.entities.Member]
 * in this [Guild][net.dv8tion.jda.api.entities.Guild].
 * <br></br>This will never return an empty list because if it were empty, that would imply that there are no
 * [Members][net.dv8tion.jda.api.entities.Member] in this [Guild][net.dv8tion.jda.api.entities.Guild], which is
 * impossible.
 *
 * @return Never-empty immutable list containing all the [GuildVoiceStates][GuildVoiceState] on this [Guild][net.dv8tion.jda.api.entities.Guild].
 */
@Nonnull
fun getVoiceStates(): List<GuildVoiceState?>?

/**
 * Returns the verification-Level of this Guild. Verification level is one of the factors that determines if a Member
 * can send messages in a Guild.
 * <br></br>For a short description of the different values, see [net.dv8tion.jda.api.entities.Guild.VerificationLevel].
 *
 *
 * This value can be modified using [GuildManager.setVerificationLevel].
 *
 * @return The Verification-Level of this Guild.
 */
@Nonnull
fun getVerificationLevel(): VerificationLevel?

/**
 * Returns the default message Notification-Level of this Guild. Notification level determines when Members get notification
 * for messages. The value returned is the default level set for any new Members that join the Guild.
 * <br></br>For a short description of the different values, see [NotificationLevel][net.dv8tion.jda.api.entities.Guild.NotificationLevel].
 *
 *
 * This value can be modified using [GuildManager.setDefaultNotificationLevel].
 *
 * @return The default message Notification-Level of this Guild.
 */
@Nonnull
fun getDefaultNotificationLevel(): NotificationLevel?

/**
 * Returns the level of multifactor authentication required to execute administrator restricted functions in this guild.
 * <br></br>For a short description of the different values, see [MFALevel][net.dv8tion.jda.api.entities.Guild.MFALevel].
 *
 *
 * This value can be modified using [GuildManager.setRequiredMFALevel].
 *
 * @return The MFA-Level required by this Guild.
 */
@Nonnull
fun getRequiredMFALevel(): MFALevel?

/**
 * The level of content filtering enabled in this Guild.
 * <br></br>This decides which messages sent by which Members will be scanned for explicit content.
 *
 * @return [ExplicitContentLevel][net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel] for this Guild
 */
@Nonnull
fun getExplicitContentLevel(): ExplicitContentLevel?

/**
 * Retrieves and collects members of this guild into a list.
 * <br></br>This will use the configured [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to decide which members to retain in cache.
 *
 *
 * You can use [.findMembers] to filter specific members.
 *
 *
 * **This requires the privileged GatewayIntent.GUILD_MEMBERS to be enabled!**
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @throws IllegalStateException
 * If the [GatewayIntent.GUILD_MEMBERS] is not enabled
 *
 * @return [Task] - Type: [List] of [Member]
 */
@Nonnull
@CheckReturnValue
fun loadMembers(): Task<List<Member?>?>? {
    return findMembers { m: Member? -> true }
}

/**
 * Retrieves and collects members of this guild into a list.
 * <br></br>This will use the configured [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to decide which members to retain in cache.
 *
 *
 * **This requires the privileged GatewayIntent.GUILD_MEMBERS to be enabled!**
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  filter
 * Filter to decide which members to include
 *
 * @throws IllegalArgumentException
 * If the provided filter is null
 * @throws IllegalStateException
 * If the [GatewayIntent.GUILD_MEMBERS] is not enabled
 *
 * @return [Task] - Type: [List] of [Member]
 */
@Nonnull
@CheckReturnValue
fun findMembers(@Nonnull filter: Predicate<in Member>): Task<List<Member?>?>? {
    Checks.notNull(filter, "Filter")
    val list: MutableList<Member?> = ArrayList()
    val future = CompletableFuture<List<Member?>?>()
    val reference = loadMembers { member: Member -> if (filter.test(member)) list.add(member) }
    val task = GatewayTask(future) { reference.cancel() }
        .onSetTimeout { timeout: Long -> reference.setTimeout(Duration.ofMillis(timeout)) }
    reference.onSuccess { it: Void? -> future.complete(list) }
        .onError { ex: Throwable? -> future.completeExceptionally(ex) }
    return task
}

/**
 * Retrieves and collects members of this guild into a list.
 * <br></br>This will use the configured [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to decide which members to retain in cache.
 *
 *
 * **This requires the privileged GatewayIntent.GUILD_MEMBERS to be enabled!**
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  roles
 * Collection of all roles the members must have
 *
 * @throws IllegalArgumentException
 * If null is provided
 * @throws IllegalStateException
 * If the [GatewayIntent.GUILD_MEMBERS] is not enabled
 *
 * @return [Task] - Type: [List] of [Member]
 *
 * @since  4.2.1
 */
@Nonnull
@CheckReturnValue
fun findMembersWithRoles(@Nonnull roles: Collection<Role?>): Task<List<Member?>?>? {
    Checks.noneNull(roles, "Roles")
    for (role in roles) Checks.check(this == role.getGuild(), "All roles must be from the same guild!")
    if (isLoaded()) {
        val future = CompletableFuture.completedFuture(getMembersWithRoles(roles))
        return GatewayTask(future) {}
    }
    val rolesWithoutPublicRole = roles.stream().filter { role: Role? -> !role!!.isPublicRole() }
        .collect(Collectors.toList())
    return if (rolesWithoutPublicRole.isEmpty()) loadMembers() else findMembers { member: Member ->
        member.getRoles().containsAll(rolesWithoutPublicRole)
    }
}

/**
 * Retrieves and collects members of this guild into a list.
 * <br></br>This will use the configured [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to decide which members to retain in cache.
 *
 *
 * **This requires the privileged GatewayIntent.GUILD_MEMBERS to be enabled!**
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  roles
 * All roles the members must have
 *
 * @throws IllegalArgumentException
 * If null is provided
 * @throws IllegalStateException
 * If the [GatewayIntent.GUILD_MEMBERS] is not enabled
 *
 * @return [Task] - Type: [List] of [Member]
 *
 * @since  4.2.1
 */
@Nonnull
@CheckReturnValue
fun findMembersWithRoles(@Nonnull vararg roles: Role?): Task<List<Member?>?>? {
    Checks.noneNull(roles, "Roles")
    return findMembersWithRoles(Arrays.asList(*roles))
}

/**
 * Retrieves all members of this guild.
 * <br></br>This will use the configured [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to decide which members to retain in cache.
 *
 *
 * **This requires the privileged GatewayIntent.GUILD_MEMBERS to be enabled!**
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  callback
 * Consumer callback for each member
 *
 * @throws IllegalArgumentException
 * If the callback is null
 * @throws IllegalStateException
 * If the [GatewayIntent.GUILD_MEMBERS] is not enabled
 *
 * @return [Task] cancellable handle for this request
 */
@Nonnull
fun loadMembers(@Nonnull callback: Consumer<Member>?): Task<Void>

/**
 * Load the member for the specified [UserSnowflake].
 * <br></br>If the member is already loaded it will be retrieved from [.getMemberById]
 * and immediately provided if the member information is consistent. The cache consistency directly
 * relies on the enabled [GatewayIntents][GatewayIntent] as [GatewayIntent.GUILD_MEMBERS]
 * is required to keep the cache updated with the latest information. You can use [useCache(true)][CacheRestAction.useCache] to always
 * make a new request, which is the default behavior if the required intents are disabled.
 *
 *
 * Possible [ErrorResponseExceptions][net.dv8tion.jda.api.exceptions.ErrorResponseException] include:
 *
 *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified user is not a member of this guild
 *
 *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
 * <br></br>The specified user does not exist
 *
 *
 * @param  user
 * The [UserSnowflake] for the member to retrieve.
 * This can be a member or user instance or [User.fromId].
 *
 * @throws IllegalArgumentException
 * If provided with null
 *
 * @return [RestAction] - Type: [Member]
 *
 * @see .pruneMemberCache
 * @see .unloadMember
 */
@Nonnull
fun retrieveMember(@Nonnull user: UserSnowflake): CacheRestAction<Member?>? {
    Checks.notNull(user, "User")
    return retrieveMemberById(user.id)
}

/**
 * Shortcut for `guild.retrieveMemberById(guild.getOwnerIdLong())`.
 * <br></br>This will retrieve the current owner of the guild.
 * It is possible that the owner of a guild is no longer a registered discord user in which case this will fail.
 * <br></br>If the member is already loaded it will be retrieved from [.getMemberById]
 * and immediately provided if the member information is consistent. The cache consistency directly
 * relies on the enabled [GatewayIntents][GatewayIntent] as [GatewayIntent.GUILD_MEMBERS]
 * is required to keep the cache updated with the latest information. You can use [useCache(true)][CacheRestAction.useCache] to always
 * make a new request, which is the default behavior if the required intents are disabled.
 *
 *
 * Possible [ErrorResponseExceptions][net.dv8tion.jda.api.exceptions.ErrorResponseException] include:
 *
 *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified user is not a member of this guild
 *
 *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
 * <br></br>The specified user does not exist
 *
 *
 * @return [RestAction] - Type: [Member]
 *
 * @see .pruneMemberCache
 * @see .unloadMember
 * @see .getOwner
 * @see .getOwnerIdLong
 * @see .retrieveMemberById
 */
@Nonnull
fun retrieveOwner(): CacheRestAction<Member?>? {
    return retrieveMemberById(getOwnerIdLong())
}

/**
 * Load the member for the specified user.
 * <br></br>If the member is already loaded it will be retrieved from [.getMemberById]
 * and immediately provided if the member information is consistent. The cache consistency directly
 * relies on the enabled [GatewayIntents][GatewayIntent] as [GatewayIntent.GUILD_MEMBERS]
 * is required to keep the cache updated with the latest information. You can use [useCache(true)][CacheRestAction.useCache] to always
 * make a new request, which is the default behavior if the required intents are disabled.
 *
 *
 * Possible [ErrorResponseExceptions][net.dv8tion.jda.api.exceptions.ErrorResponseException] include:
 *
 *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified user is not a member of this guild
 *
 *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
 * <br></br>The specified user does not exist
 *
 *
 * @param  id
 * The user id to load the member from
 *
 * @throws IllegalArgumentException
 * If the provided id is empty or null
 * @throws NumberFormatException
 * If the provided id is not a snowflake
 *
 * @return [RestAction] - Type: [Member]
 *
 * @see .pruneMemberCache
 * @see .unloadMember
 */
@Nonnull
fun retrieveMemberById(@Nonnull id: String?): CacheRestAction<Member?>? {
    return retrieveMemberById(MiscUtil.parseSnowflake(id))
}

/**
 * Load the member for the specified user.
 * <br></br>If the member is already loaded it will be retrieved from [.getMemberById]
 * and immediately provided if the member information is consistent. The cache consistency directly
 * relies on the enabled [GatewayIntents][GatewayIntent] as [GatewayIntent.GUILD_MEMBERS]
 * is required to keep the cache updated with the latest information. You can use [useCache(false)][CacheRestAction.useCache] to always
 * make a new request, which is the default behavior if the required intents are disabled.
 *
 *
 * Possible [ErrorResponseExceptions][net.dv8tion.jda.api.exceptions.ErrorResponseException] include:
 *
 *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified user is not a member of this guild
 *
 *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
 * <br></br>The specified user does not exist
 *
 *
 * @param  id
 * The user id to load the member from
 *
 * @return [RestAction] - Type: [Member]
 *
 * @see .pruneMemberCache
 * @see .unloadMember
 */
@Nonnull
fun retrieveMemberById(id: Long): CacheRestAction<Member?>?

/**
 * Retrieves a list of members.
 * <br></br>If the user does not resolve to a member of this guild, then it will not appear in the resulting list.
 * It is possible that none of the users resolve to a member, in which case an empty list will be the result.
 *
 *
 * If the [GUILD_PRESENCES][GatewayIntent.GUILD_PRESENCES] intent is enabled,
 * this will load the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] and [Activities][Activity]
 * of the members. You can use [.retrieveMembers] to disable presences.
 *
 *
 * The requests automatically timeout after `10` seconds.
 * When the timeout occurs a [TimeoutException][java.util.concurrent.TimeoutException] will be used to complete exceptionally.
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  users
 * The users of the members (max 100)
 *
 * @throws IllegalArgumentException
 *
 *  * If the input contains null
 *  * If the input is more than 100 IDs
 *
 *
 * @return [Task] handle for the request
 */
@Nonnull
@CheckReturnValue
fun retrieveMembers(@Nonnull users: Collection<UserSnowflake?>): Task<List<Member>>? {
    Checks.noneNull(users, "Users")
    if (users.isEmpty()) return GatewayTask(CompletableFuture.completedFuture(emptyList())) {}
    val ids = users.stream().mapToLong { obj: UserSnowflake? -> obj.getIdLong() }.toArray()
    return retrieveMembersByIds(*ids)
}

/**
 * Retrieves a list of members by their user id.
 * <br></br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
 * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
 *
 *
 * If the [GUILD_PRESENCES][GatewayIntent.GUILD_PRESENCES] intent is enabled,
 * this will load the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] and [Activities][Activity]
 * of the members. You can use [.retrieveMembersByIds] to disable presences.
 *
 *
 * The requests automatically timeout after `10` seconds.
 * When the timeout occurs a [TimeoutException][java.util.concurrent.TimeoutException] will be used to complete exceptionally.
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  ids
 * The ids of the members (max 100)
 *
 * @throws IllegalArgumentException
 *
 *  * If the input contains null
 *  * If the input is more than 100 IDs
 *
 *
 * @return [Task] handle for the request
 */
@Nonnull
@CheckReturnValue
fun retrieveMembersByIds(@Nonnull ids: Collection<Long?>): Task<List<Member>>? {
    Checks.noneNull(ids, "IDs")
    if (ids.isEmpty()) return GatewayTask(CompletableFuture.completedFuture(emptyList())) {}
    val arr = ids.stream().mapToLong { obj: Long? -> obj!!.toLong() }.toArray()
    return retrieveMembersByIds(*arr)
}

/**
 * Retrieves a list of members by their user id.
 * <br></br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
 * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
 *
 *
 * If the [GUILD_PRESENCES][GatewayIntent.GUILD_PRESENCES] intent is enabled,
 * this will load the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] and [Activities][Activity]
 * of the members. You can use [.retrieveMembersByIds] to disable presences.
 *
 *
 * The requests automatically timeout after `10` seconds.
 * When the timeout occurs a [TimeoutException][java.util.concurrent.TimeoutException] will be used to complete exceptionally.
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  ids
 * The ids of the members (max 100)
 *
 * @throws IllegalArgumentException
 *
 *  * If the input contains null
 *  * If the input is more than 100 IDs
 *
 *
 * @return [Task] handle for the request
 */
@Nonnull
@CheckReturnValue
fun retrieveMembersByIds(@Nonnull vararg ids: String?): Task<List<Member>>? {
    Checks.notNull(ids, "Array")
    if (ids.size == 0) return GatewayTask(CompletableFuture.completedFuture(emptyList())) {}
    val arr = LongArray(ids.size)
    for (i in ids.indices) arr[i] = MiscUtil.parseSnowflake(ids[i])
    return retrieveMembersByIds(*arr)
}

/**
 * Retrieves a list of members by their user id.
 * <br></br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
 * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
 *
 *
 * If the [GUILD_PRESENCES][GatewayIntent.GUILD_PRESENCES] intent is enabled,
 * this will load the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] and [Activities][Activity]
 * of the members. You can use [.retrieveMembersByIds] to disable presences.
 *
 *
 * The requests automatically timeout after `10` seconds.
 * When the timeout occurs a [TimeoutException][java.util.concurrent.TimeoutException] will be used to complete exceptionally.
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  ids
 * The ids of the members (max 100)
 *
 * @throws IllegalArgumentException
 *
 *  * If the input contains null
 *  * If the input is more than 100 IDs
 *
 *
 * @return [Task] handle for the request
 */
@Nonnull
@CheckReturnValue
fun retrieveMembersByIds(@Nonnull vararg ids: Long): Task<List<Member>>? {
    val presence = getJDA().gatewayIntents.contains(GatewayIntent.GUILD_PRESENCES)
    return retrieveMembersByIds(presence, *ids)
}

/**
 * Retrieves a list of members.
 * <br></br>If the user does not resolve to a member of this guild, then it will not appear in the resulting list.
 * It is possible that none of the users resolve to a member, in which case an empty list will be the result.
 *
 *
 * You can only load presences with the [GUILD_PRESENCES][GatewayIntent.GUILD_PRESENCES] intent enabled.
 *
 *
 * The requests automatically timeout after `10` seconds.
 * When the timeout occurs a [TimeoutException][java.util.concurrent.TimeoutException] will be used to complete exceptionally.
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  includePresence
 * Whether to load presences of the members (online status/activity)
 * @param  users
 * The users of the members (max 100)
 *
 * @throws IllegalArgumentException
 *
 *  * If includePresence is `true` and the GUILD_PRESENCES intent is disabled
 *  * If the input contains null
 *  * If the input is more than 100 users
 *
 *
 * @return [Task] handle for the request
 */
@Nonnull
@CheckReturnValue
fun retrieveMembers(includePresence: Boolean, @Nonnull users: Collection<UserSnowflake?>): Task<List<Member>>? {
    Checks.noneNull(users, "Users")
    if (users.isEmpty()) return GatewayTask(CompletableFuture.completedFuture(emptyList())) {}
    val ids = users.stream().mapToLong { obj: UserSnowflake? -> obj.getIdLong() }.toArray()
    return retrieveMembersByIds(includePresence, *ids)
}

/**
 * Retrieves a list of members by their user id.
 * <br></br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
 * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
 *
 *
 * You can only load presences with the [GUILD_PRESENCES][GatewayIntent.GUILD_PRESENCES] intent enabled.
 *
 *
 * The requests automatically timeout after `10` seconds.
 * When the timeout occurs a [TimeoutException][java.util.concurrent.TimeoutException] will be used to complete exceptionally.
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  includePresence
 * Whether to load presences of the members (online status/activity)
 * @param  ids
 * The ids of the members (max 100)
 *
 * @throws IllegalArgumentException
 *
 *  * If includePresence is `true` and the GUILD_PRESENCES intent is disabled
 *  * If the input contains null
 *  * If the input is more than 100 IDs
 *
 *
 * @return [Task] handle for the request
 */
@Nonnull
@CheckReturnValue
fun retrieveMembersByIds(includePresence: Boolean, @Nonnull ids: Collection<Long?>): Task<List<Member>>? {
    Checks.noneNull(ids, "IDs")
    if (ids.isEmpty()) return GatewayTask(CompletableFuture.completedFuture(emptyList())) {}
    val arr = ids.stream().mapToLong { obj: Long? -> obj!!.toLong() }.toArray()
    return retrieveMembersByIds(includePresence, *arr)
}

/**
 * Retrieves a list of members by their user id.
 * <br></br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
 * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
 *
 *
 * You can only load presences with the [GUILD_PRESENCES][GatewayIntent.GUILD_PRESENCES] intent enabled.
 *
 *
 * The requests automatically timeout after `10` seconds.
 * When the timeout occurs a [TimeoutException][java.util.concurrent.TimeoutException] will be used to complete exceptionally.
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  includePresence
 * Whether to load presences of the members (online status/activity)
 * @param  ids
 * The ids of the members (max 100)
 *
 * @throws IllegalArgumentException
 *
 *  * If includePresence is `true` and the GUILD_PRESENCES intent is disabled
 *  * If the input contains null
 *  * If the input is more than 100 IDs
 *
 *
 * @return [Task] handle for the request
 */
@Nonnull
@CheckReturnValue
fun retrieveMembersByIds(includePresence: Boolean, @Nonnull vararg ids: String?): Task<List<Member>>? {
    Checks.notNull(ids, "Array")
    if (ids.size == 0) return GatewayTask(CompletableFuture.completedFuture(emptyList())) {}
    val arr = LongArray(ids.size)
    for (i in ids.indices) arr[i] = MiscUtil.parseSnowflake(ids[i])
    return retrieveMembersByIds(includePresence, *arr)
}

/**
 * Retrieves a list of members by their user id.
 * <br></br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
 * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
 *
 *
 * You can only load presences with the [GUILD_PRESENCES][GatewayIntent.GUILD_PRESENCES] intent enabled.
 *
 *
 * The requests automatically timeout after `10` seconds.
 * When the timeout occurs a [TimeoutException][java.util.concurrent.TimeoutException] will be used to complete exceptionally.
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  includePresence
 * Whether to load presences of the members (online status/activity)
 * @param  ids
 * The ids of the members (max 100)
 *
 * @throws IllegalArgumentException
 *
 *  * If includePresence is `true` and the GUILD_PRESENCES intent is disabled
 *  * If the input contains null
 *  * If the input is more than 100 IDs
 *
 *
 * @return [Task] handle for the request
 */
@Nonnull
@CheckReturnValue
fun retrieveMembersByIds(includePresence: Boolean, @Nonnull vararg ids: Long): Task<List<Member>>?

/**
 * Queries a list of members using a radix tree based on the provided name prefix.
 * <br></br>This will check both the username and the nickname of the members.
 * Additional filtering may be required. If no members with the specified prefix exist, the list will be empty.
 *
 *
 * The requests automatically timeout after `10` seconds.
 * When the timeout occurs a [TimeoutException][java.util.concurrent.TimeoutException] will be used to complete exceptionally.
 *
 *
 * **You MUST NOT use blocking operations such as [Task.get]!**
 * The response handling happens on the event thread by default.
 *
 * @param  prefix
 * The case-insensitive name prefix
 * @param  limit
 * The max amount of members to retrieve (1-100)
 *
 * @throws IllegalArgumentException
 *
 *  * If the provided prefix is null or empty.
 *  * If the provided limit is not in the range of [1, 100]
 *
 *
 * @return [Task] handle for the request
 *
 * @see .getMembersByName
 * @see .getMembersByNickname
 * @see .getMembersByEffectiveName
 */
@Nonnull
@CheckReturnValue
fun retrieveMembersByPrefix(@Nonnull prefix: String?, limit: Int): Task<List<Member?>?>?

@Nonnull
@CheckReturnValue
fun retrieveActiveThreads(): RestAction<List<ThreadChannel?>?>?

/**
 * Retrieves a [ScheduledEvent] by its ID.
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [ErrorResponse.UNKNOWN_SCHEDULED_EVENT][net.dv8tion.jda.api.requests.ErrorResponse.SCHEDULED_EVENT]
 * <br></br>A scheduled event with the specified ID does not exist in the guild, or the currently logged in user does not
 * have access to it.
 *
 *
 * @param  id
 * The ID of the [ScheduledEvent]
 *
 * @return [RestAction] - Type: [ScheduledEvent]
 *
 * @see .getScheduledEventById
 */
@Nonnull
@CheckReturnValue
fun retrieveScheduledEventById(id: Long): CacheRestAction<ScheduledEvent?>? {
    return retrieveScheduledEventById(java.lang.Long.toUnsignedString(id))
}

/**
 * Retrieves a [ScheduledEvent] by its ID.
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [ErrorResponse.UNKNOWN_SCHEDULED_EVENT][net.dv8tion.jda.api.requests.ErrorResponse.SCHEDULED_EVENT]
 * <br></br>A scheduled event with the specified ID does not exist in this guild, or the currently logged in user does not
 * have access to it.
 *
 *
 * @param  id
 * The ID of the [ScheduledEvent]
 *
 * @throws IllegalArgumentException
 * If the specified ID is `null` or empty
 * @throws NumberFormatException
 * If the specified ID cannot be parsed by [Long.parseLong]
 *
 * @return [RestAction] - Type: [ScheduledEvent]
 *
 * @see .getScheduledEventById
 */
@Nonnull
@CheckReturnValue
fun retrieveScheduledEventById(@Nonnull id: String?): CacheRestAction<ScheduledEvent?>?
/* From GuildController */ /**
 * Used to move a [Member][net.dv8tion.jda.api.entities.Member] from one [AudioChannel]
 * to another [AudioChannel].
 * <br></br>As a note, you cannot move a Member that isn't already in a AudioChannel. Also they must be in a AudioChannel
 * in the same Guild as the one that you are moving them to.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be moved due to a permission discrepancy
 *
 *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
 * <br></br>The [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission was removed
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
 * <br></br>The specified channel was deleted before finishing the task
 *
 *
 * @param  member
 * The [Member][net.dv8tion.jda.api.entities.Member] that you are moving.
 * @param  audioChannel
 * The destination [AudioChannel] to which the member is being
 * moved to. Or null to perform a voice kick.
 *
 * @throws IllegalStateException
 * If the Member isn't currently in a AudioChannel in this Guild, or [net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE] is disabled.
 * @throws IllegalArgumentException
 *
 *  * If the provided member is `null`
 *  * If the provided Member isn't part of this [Guild][net.dv8tion.jda.api.entities.Guild]
 *  * If the provided AudioChannel isn't part of this [Guild][net.dv8tion.jda.api.entities.Guild]
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 *
 *  * If this account doesn't have [net.dv8tion.jda.api.Permission.VOICE_MOVE_OTHERS]
 * in the AudioChannel that the Member is currently in.
 *  * If this account **AND** the Member being moved don't have
 * [net.dv8tion.jda.api.Permission.VOICE_CONNECT] for the destination AudioChannel.
 *
 *
 * @return [RestAction]
 */
@Nonnull
@CheckReturnValue
fun moveVoiceMember(@Nonnull member: Member?, audioChannel: AudioChannel?): RestAction<Void?>?

/**
 * Used to kick a [Member][net.dv8tion.jda.api.entities.Member] from a [AudioChannel].
 * <br></br>As a note, you cannot kick a Member that isn't already in a AudioChannel. Also they must be in a AudioChannel
 * in the same Guild.
 *
 *
 * Equivalent to `moveVoiceMember(member, null)`.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be moved due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
 * <br></br>The specified channel was deleted before finishing the task
 *
 *
 * @param  member
 * The [Member][net.dv8tion.jda.api.entities.Member] that you are moving.
 *
 * @throws IllegalStateException
 * If the Member isn't currently in a AudioChannel in this Guild, or [net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE] is disabled.
 * @throws IllegalArgumentException
 *
 *  * If any of the provided arguments is `null`
 *  * If the provided Member isn't part of this [Guild][net.dv8tion.jda.api.entities.Guild]
 *  * If the provided AudioChannel isn't part of this [Guild][net.dv8tion.jda.api.entities.Guild]
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If this account doesn't have [net.dv8tion.jda.api.Permission.VOICE_MOVE_OTHERS]
 * in the AudioChannel that the Member is currently in.
 *
 * @return [RestAction]
 */
@Nonnull
@CheckReturnValue
fun kickVoiceMember(@Nonnull member: Member?): RestAction<Void?>? {
    return moveVoiceMember(member, null)
}

/**
 * Changes the Member's nickname in this guild.
 * The nickname is visible to all members of this guild.
 *
 *
 * To change the nickname for the currently logged in account
 * only the Permission [NICKNAME_CHANGE][net.dv8tion.jda.api.Permission.NICKNAME_CHANGE] is required.
 * <br></br>To change the nickname of **any** [Member][net.dv8tion.jda.api.entities.Member] for this [Guild][net.dv8tion.jda.api.entities.Guild]
 * the Permission [NICKNAME_MANAGE][net.dv8tion.jda.api.Permission.NICKNAME_MANAGE] is required.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The nickname of the target Member is not modifiable due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *
 * @param  member
 * The [Member][net.dv8tion.jda.api.entities.Member] for which the nickname should be changed.
 * @param  nickname
 * The new nickname of the [Member][net.dv8tion.jda.api.entities.Member], provide `null` or an
 * empty String to reset the nickname
 *
 * @throws IllegalArgumentException
 * If the specified [Member][net.dv8tion.jda.api.entities.Member]
 * is not from the same [Guild][net.dv8tion.jda.api.entities.Guild].
 * Or if the provided member is `null`
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 *
 *  * If attempting to set nickname for self and the logged in account has neither [net.dv8tion.jda.api.Permission.NICKNAME_CHANGE]
 * or [net.dv8tion.jda.api.Permission.NICKNAME_MANAGE]
 *  * If attempting to set nickname for another member and the logged in account does not have [net.dv8tion.jda.api.Permission.NICKNAME_MANAGE]
 *
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If attempting to set nickname for another member and the logged in account cannot manipulate the other user due to permission hierarchy position.
 * <br></br>See [Member.canInteract]
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun modifyNickname(@Nonnull member: Member?, nickname: String?): AuditableRestAction<Void?>?

/**
 * This method will prune (kick) all members who were offline for at least *days* days.
 * <br></br>The RestAction returned from this method will return the amount of Members that were pruned.
 * <br></br>You can use [Guild.retrievePrunableMemberCount] to determine how many Members would be pruned if you were to
 * call this method.
 *
 *
 * This might timeout when pruning many members.
 * You can use `prune(days, false)` to ignore the prune count and avoid a timeout.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The prune cannot finished due to a permission discrepancy
 *
 *
 * @param  days
 * Minimum number of days since a member has been offline to get affected.
 * @param  roles
 * Optional roles to include in prune filter
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the account doesn't have [KICK_MEMBER][net.dv8tion.jda.api.Permission.KICK_MEMBERS] Permission.
 * @throws IllegalArgumentException
 *
 *  * If the provided days are not in the range from 1 to 30 (inclusive)
 *  * If null is provided
 *  * If any of the provided roles is not from this guild
 *
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction] - Type: Integer
 * <br></br>The amount of Members that were pruned from the Guild.
 */
@Nonnull
@CheckReturnValue
fun prune(days: Int, @Nonnull vararg roles: Role?): AuditableRestAction<Int?>? {
    return prune(days, true, *roles)
}

/**
 * This method will prune (kick) all members who were offline for at least *days* days.
 * <br></br>The RestAction returned from this method will return the amount of Members that were pruned.
 * <br></br>You can use [Guild.retrievePrunableMemberCount] to determine how many Members would be pruned if you were to
 * call this method.
 *
 *
 * This might timeout when pruning many members with `wait=true`.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The prune cannot finished due to a permission discrepancy
 *
 *
 * @param  days
 * Minimum number of days since a member has been offline to get affected.
 * @param  wait
 * Whether to calculate the number of pruned members and wait for the response (timeout for too many pruned)
 * @param  roles
 * Optional roles to include in prune filter
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the account doesn't have [KICK_MEMBER][net.dv8tion.jda.api.Permission.KICK_MEMBERS] Permission.
 * @throws IllegalArgumentException
 *
 *  * If the provided days are not in the range from 1 to 30 (inclusive)
 *  * If null is provided
 *  * If any of the provided roles is not from this guild
 *
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction] - Type: Integer
 * <br></br>Provides the amount of Members that were pruned from the Guild, if wait is true.
 */
@Nonnull
@CheckReturnValue
fun prune(days: Int, wait: Boolean, @Nonnull vararg roles: Role?): AuditableRestAction<Int?>?

/**
 * Kicks the [UserSnowflake] from the [Guild][net.dv8tion.jda.api.entities.Guild].
 *
 *
 * **Note:** [net.dv8tion.jda.api.entities.Guild.getMembers] will still contain the [User][net.dv8tion.jda.api.entities.User]
 * until Discord sends the [GuildMemberRemoveEvent][net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent].
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be kicked due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *
 * @param  user
 * The [UserSnowflake] for the user to kick.
 * This can be a member or user instance or [User.fromId].
 * @param  reason
 * The reason for this action or `null` if there is no specified reason
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.KICK_MEMBERS] permission.
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the logged in account cannot kick the other member due to permission hierarchy position. (See [Member.canInteract])
 * @throws java.lang.IllegalArgumentException
 *
 *  * If the user cannot be kicked from this Guild or the provided `user` is null.
 *  * If the provided reason is longer than 512 characters
 *
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 *
 */
@Nonnull
@CheckReturnValue
@ForRemoval
@ReplaceWith("kick(user).reason(reason)")
@DeprecatedSince("5.0.0")
@Deprecated("         Use {@link #kick(UserSnowflake)} and {@link AuditableRestAction#reason(String)} instead.")
fun kick(@Nonnull user: UserSnowflake?, reason: String?): AuditableRestAction<Void?>? {
    return kick(user).reason(reason)
}

/**
 * Kicks a [Member][net.dv8tion.jda.api.entities.Member] from the [Guild][net.dv8tion.jda.api.entities.Guild].
 *
 *
 * **Note:** [net.dv8tion.jda.api.entities.Guild.getMembers] will still contain the [User][net.dv8tion.jda.api.entities.User]
 * until Discord sends the [GuildMemberRemoveEvent][net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent].
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be kicked due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *
 * @param  user
 * The [UserSnowflake] for the user to kick.
 * This can be a member or user instance or [User.fromId].
 *
 * @throws java.lang.IllegalArgumentException
 * If the user cannot be kicked from this Guild or the provided `user` is null.
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.KICK_MEMBERS] permission.
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the logged in account cannot kick the other member due to permission hierarchy position. (See [Member.canInteract])
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 * Kicks the provided Member from the current Guild
 */
@Nonnull
@CheckReturnValue
fun kick(@Nonnull user: UserSnowflake?): AuditableRestAction<Void?>

/**
 * Bans the user specified by the provided [UserSnowflake] and deletes messages sent by the user based on the `deletionTimeframe`.
 * <br></br>If you wish to ban a user without deleting any messages, provide `deletionTimeframe` with a value of 0.
 * To set a ban reason, use [AuditableRestAction.reason].
 *
 *
 * You can unban a user with [Guild.unban(UserReference)][net.dv8tion.jda.api.entities.Guild.unban].
 *
 *
 * **Note:** [net.dv8tion.jda.api.entities.Guild.getMembers] will still contain the [User&#39;s][net.dv8tion.jda.api.entities.User]
 * [Member][net.dv8tion.jda.api.entities.Member] object (if the User was in the Guild)
 * until Discord sends the [GuildMemberRemoveEvent][net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent].
 *
 *
 * **Examples**<br></br>
 * Banning a user without deleting any messages:
 * <pre>`guild.ban(user, 0, TimeUnit.SECONDS)
 * .reason("Banned for rude behavior")
 * .queue();
`</pre> *
 * Banning a user and deleting messages from the past hour:
 * <pre>`guild.ban(user, 1, TimeUnit.HOURS)
 * .reason("Banned for spamming")
 * .queue();
`</pre> *
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be banned due to a permission discrepancy
 *
 *  * [UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
 * <br></br>The user does not exist
 *
 *
 * @param  user
 * The [UserSnowflake] for the user to ban.
 * This can be a member or user instance or [User.fromId].
 * @param  deletionTimeframe
 * The timeframe for the history of messages that will be deleted. (seconds precision)
 * @param  unit
 * Timeframe unit as a [TimeUnit] (for example `ban(user, 7, TimeUnit.DAYS)`).
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.BAN_MEMBERS] permission.
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the logged in account cannot ban the other user due to permission hierarchy position.
 * <br></br>See [Member.canInteract]
 * @throws java.lang.IllegalArgumentException
 *
 *  * If the provided deletionTimeframe is negative.
 *  * If the provided deletionTimeframe is longer than 7 days.
 *  * If the provided user or time unit is `null`
 *
 *
 * @return [AuditableRestAction]
 *
 * @see AuditableRestAction.reason
 */
@Nonnull
@CheckReturnValue
fun ban(@Nonnull user: UserSnowflake?, deletionTimeframe: Int, @Nonnull unit: TimeUnit?): AuditableRestAction<Void?>?

/**
 * Bans up to 200 of the provided users.
 * <br></br>To set a ban reason, use [AuditableRestAction.reason].
 *
 *
 * The [BulkBanResponse] includes a list of [failed users][BulkBanResponse.getFailedUsers],
 * which is populated with users that could not be banned, for instance due to some internal server error or permission issues.
 * This list of failed users also includes all users that were already banned.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be banned due to a permission discrepancy
 *
 *  * [FAILED_TO_BAN_USERS][net.dv8tion.jda.api.requests.ErrorResponse.FAILED_TO_BAN_USERS]
 * <br></br>None of the users could be banned
 *
 *
 * @param  users
 * The users to ban
 * @param  deletionTime
 * Delete recent messages of the given timeframe (for instance the last hour with `Duration.ofHours(1)`)
 *
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If any of the provided users is the guild owner or has a higher or equal role position
 * @throws InsufficientPermissionException
 * If the bot does not have [Permission.BAN_MEMBERS] or [Permission.MANAGE_SERVER]
 * @throws IllegalArgumentException
 *
 *  * If the users collection is null or contains null
 *  * If the deletionTime is negative
 *
 *
 * @return [AuditableRestAction] - Type: [BulkBanResponse]
 */
@Nonnull
@CheckReturnValue
fun ban(@Nonnull users: Collection<UserSnowflake?>?, deletionTime: Duration?): AuditableRestAction<BulkBanResponse?>?

/**
 * Bans up to 200 of the provided users.
 * <br></br>To set a ban reason, use [AuditableRestAction.reason].
 *
 *
 * The [BulkBanResponse] includes a list of [failed users][BulkBanResponse.getFailedUsers],
 * which is populated with users that could not be banned, for instance due to some internal server error or permission issues.
 * This list of failed users also includes all users that were already banned.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be banned due to a permission discrepancy
 *
 *  * [FAILED_TO_BAN_USERS][net.dv8tion.jda.api.requests.ErrorResponse.FAILED_TO_BAN_USERS]
 * <br></br>None of the users could be banned
 *
 *
 * @param  users
 * The users to ban
 * @param  deletionTimeframe
 * The timeframe for the history of messages that will be deleted. (seconds precision)
 * @param  unit
 * Timeframe unit as a [TimeUnit] (for example `ban(user, 7, TimeUnit.DAYS)`).
 *
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If any of the provided users is the guild owner or has a higher or equal role position
 * @throws InsufficientPermissionException
 * If the bot does not have [Permission.BAN_MEMBERS] or [Permission.MANAGE_SERVER]
 * @throws IllegalArgumentException
 *
 *  * If null is provided
 *  * If the deletionTimeframe is negative
 *
 *
 * @return [AuditableRestAction] - Type: [BulkBanResponse]
 */
@Nonnull
@CheckReturnValue
fun ban(
    @Nonnull users: Collection<UserSnowflake?>?,
    deletionTimeframe: Int,
    @Nonnull unit: TimeUnit
): AuditableRestAction<BulkBanResponse?>? {
    Checks.notNull(unit, "TimeUnit")
    return ban(users, Duration.ofSeconds(unit.toSeconds(deletionTimeframe.toLong())))
}

/**
 * Unbans the specified [UserSnowflake] from this Guild.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be unbanned due to a permission discrepancy
 *
 *  * [UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
 * <br></br>The specified User does not exist
 *
 *
 * @param  user
 * The [UserSnowflake] to unban.
 * This can be a member or user instance or [User.fromId].
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.BAN_MEMBERS] permission.
 * @throws IllegalArgumentException
 * If the provided user is null
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun unban(@Nonnull user: UserSnowflake?): AuditableRestAction<Void?>?

/**
 * Puts the specified Member in time out in this [Guild][net.dv8tion.jda.api.entities.Guild] for a specific amount of time.
 * <br></br>While a Member is in time out, they cannot send messages, reply, react, or speak in voice channels.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be put into time out due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *
 * @param  user
 * The [UserSnowflake] to timeout.
 * This can be a member or user instance or [User.fromId].
 * @param  amount
 * The amount of the provided [unit][TimeUnit] to put the specified Member in time out for
 * @param  unit
 * The [Unit][TimeUnit] type of `amount`
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MODERATE_MEMBERS] permission.
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the logged in account cannot put a timeout on the other Member due to permission hierarchy position. (See [Member.canInteract])
 * @throws IllegalArgumentException
 * If any of the following checks are true
 *
 *  * The provided `user` is null
 *  * The provided `amount` is lower than or equal to `0`
 *  * The provided `unit` is null
 *  * The provided `amount` with the `unit` results in a date that is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future
 *
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun timeoutFor(@Nonnull user: UserSnowflake?, amount: Long, @Nonnull unit: TimeUnit): AuditableRestAction<Void?>? {
    Checks.check(amount >= 1, "The amount must be more than 0")
    Checks.notNull(unit, "TimeUnit")
    return timeoutUntil(user, Helpers.toOffset(System.currentTimeMillis() + unit.toMillis(amount)))
}

/**
 * Puts the specified Member in time out in this [Guild][net.dv8tion.jda.api.entities.Guild] for a specific amount of time.
 * <br></br>While a Member is in time out, all permissions except [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] and
 * [MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] are removed from them.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be put into time out due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *
 * @param  user
 * The [UserSnowflake] to timeout.
 * This can be a member or user instance or [User.fromId].
 * @param  duration
 * The duration to put the specified Member in time out for
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MODERATE_MEMBERS] permission.
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the logged in account cannot put a timeout on the other Member due to permission hierarchy position.
 * <br></br>See [Member.canInteract]
 * @throws IllegalArgumentException
 * If any of the following checks are true
 *
 *  * The provided `user` is null
 *  * The provided `duration` is null
 *  * The provided `duration` results in a date that is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future
 *
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun timeoutFor(@Nonnull user: UserSnowflake?, @Nonnull duration: Duration): AuditableRestAction<Void?>? {
    Checks.notNull(duration, "Duration")
    return timeoutUntil(user, Helpers.toOffset(System.currentTimeMillis() + duration.toMillis()))
}

/**
 * Puts the specified Member in time out in this [Guild][net.dv8tion.jda.api.entities.Guild] until the specified date.
 * <br></br>While a Member is in time out, all permissions except [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] and
 * [MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] are removed from them.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be put into time out due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *
 * @param  user
 * The [UserSnowflake] to timeout.
 * This can be a member or user instance or [User.fromId].
 * @param  temporal
 * The time the specified Member will be released from time out
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MODERATE_MEMBERS] permission.
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the logged in account cannot put a timeout on the other Member due to permission hierarchy position. (See [Member.canInteract])
 * @throws IllegalArgumentException
 * If any of the following are true
 *
 *  * The provided `user` is null
 *  * The provided `temporal` is null
 *  * The provided `temporal` is in the past
 *  * The provided `temporal` is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future
 *
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun timeoutUntil(@Nonnull user: UserSnowflake?, @Nonnull temporal: TemporalAccessor?): AuditableRestAction<Void?>?

/**
 * Removes a time out from the specified Member in this [Guild][net.dv8tion.jda.api.entities.Guild].
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The time out cannot be removed due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *
 * @param  user
 * The [UserSnowflake] to timeout.
 * This can be a member or user instance or [User.fromId].
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MODERATE_MEMBERS] permission.
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the logged in account cannot remove the timeout from the other Member due to permission hierarchy position. (See [Member.canInteract])
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
fun removeTimeout(@Nonnull user: UserSnowflake?): AuditableRestAction<Void?>?

/**
 * Sets the Guild Deafened state of the [Member][net.dv8tion.jda.api.entities.Member] based on the provided
 * boolean.
 *
 *
 * **Note:** The Member's [GuildVoiceState.isGuildDeafened()][net.dv8tion.jda.api.entities.GuildVoiceState.isGuildDeafened] value won't change
 * until JDA receives the [GuildVoiceGuildDeafenEvent][net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent] event related to this change.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be deafened due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *  * [USER_NOT_CONNECTED][net.dv8tion.jda.api.requests.ErrorResponse.USER_NOT_CONNECTED]
 * <br></br>The specified Member is not connected to a voice channel
 *
 *
 * @param  user
 * The [UserSnowflake] who's [GuildVoiceState] to change.
 * This can be a member or user instance or [User.fromId].
 * @param  deafen
 * Whether this [Member][net.dv8tion.jda.api.entities.Member] should be deafened or undeafened.
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.VOICE_DEAF_OTHERS] permission.
 * @throws IllegalArgumentException
 * If the provided user is null.
 * @throws java.lang.IllegalStateException
 * If the provided user is not currently connected to a voice channel.
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun deafen(@Nonnull user: UserSnowflake?, deafen: Boolean): AuditableRestAction<Void?>?

/**
 * Sets the Guild Muted state of the [Member][net.dv8tion.jda.api.entities.Member] based on the provided
 * boolean.
 *
 *
 * **Note:** The Member's [GuildVoiceState.isGuildMuted()][net.dv8tion.jda.api.entities.GuildVoiceState.isGuildMuted] value won't change
 * until JDA receives the [GuildVoiceGuildMuteEvent][net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent] event related to this change.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The target Member cannot be muted due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The specified Member was removed from the Guild before finishing the task
 *
 *  * [USER_NOT_CONNECTED][net.dv8tion.jda.api.requests.ErrorResponse.USER_NOT_CONNECTED]
 * <br></br>The specified Member is not connected to a voice channel
 *
 *
 * @param  user
 * The [UserSnowflake] who's [GuildVoiceState] to change.
 * This can be a member or user instance or [User.fromId].
 * @param  mute
 * Whether this [Member][net.dv8tion.jda.api.entities.Member] should be muted or unmuted.
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.VOICE_DEAF_OTHERS] permission.
 * @throws java.lang.IllegalArgumentException
 * If the provided user is null.
 * @throws java.lang.IllegalStateException
 * If the provided user is not currently connected to a voice channel.
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun mute(@Nonnull user: UserSnowflake?, mute: Boolean): AuditableRestAction<Void?>?

/**
 * Atomically assigns the provided [Role] to the specified [Member][net.dv8tion.jda.api.entities.Member].
 * <br></br>**This can be used together with other role modification methods as it does not require an updated cache!**
 *
 *
 * If multiple roles should be added/removed (efficiently) in one request
 * you may use [modifyMemberRoles(Member, Collection, Collection)][.modifyMemberRoles] or similar methods.
 *
 *
 * If the specified role is already present in the member's set of roles this does nothing.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The Members Roles could not be modified due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The target Member was removed from the Guild before finishing the task
 *
 *  * [UNKNOWN_ROLE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_ROLE]
 * <br></br>If the specified Role does not exist
 *
 *
 * @param  user
 * The [UserSnowflake] to change roles for.
 * This can be a member or user instance or [User.fromId].
 * @param  role
 * The role which should be assigned atomically
 *
 * @throws java.lang.IllegalArgumentException
 *
 *  * If the specified member or role are not from the current Guild
 *  * Either member or role are `null`
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account does not have [Permission.MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES]
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the provided roles are higher in the Guild's hierarchy
 * and thus cannot be modified by the currently logged in account
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun addRoleToMember(@Nonnull user: UserSnowflake?, @Nonnull role: Role?): AuditableRestAction<Void?>?

/**
 * Atomically removes the provided [Role] from the specified [Member][net.dv8tion.jda.api.entities.Member].
 * <br></br>**This can be used together with other role modification methods as it does not require an updated cache!**
 *
 *
 * If multiple roles should be added/removed (efficiently) in one request
 * you may use [modifyMemberRoles(Member, Collection, Collection)][.modifyMemberRoles] or similar methods.
 *
 *
 * If the specified role is not present in the member's set of roles this does nothing.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The Members Roles could not be modified due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The target Member was removed from the Guild before finishing the task
 *
 *  * [UNKNOWN_ROLE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_ROLE]
 * <br></br>If the specified Role does not exist
 *
 *
 * @param  user
 * The [UserSnowflake] to change roles for.
 * This can be a member or user instance or [User.fromId].
 * @param  role
 * The role which should be removed atomically
 *
 * @throws java.lang.IllegalArgumentException
 *
 *  * If the specified member or role are not from the current Guild
 *  * Either member or role are `null`
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account does not have [Permission.MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES]
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the provided roles are higher in the Guild's hierarchy
 * and thus cannot be modified by the currently logged in account
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun removeRoleFromMember(@Nonnull user: UserSnowflake?, @Nonnull role: Role?): AuditableRestAction<Void?>?

/**
 * Modifies the [Roles][Role] of the specified [Member][net.dv8tion.jda.api.entities.Member]
 * by adding and removing a collection of roles.
 * <br></br>None of the provided roles may be the <u>Public Role</u> of the current Guild.
 * <br></br>If a role is both in `rolesToAdd` and `rolesToRemove` it will be removed.
 *
 *
 * **Example**<br></br>
 * <pre>`public static void promote(Member member) {
 * Guild guild = member.getGuild();
 * List<Role> pleb = guild.getRolesByName("Pleb", true); // remove all roles named "pleb"
 * List<Role> knight = guild.getRolesByName("Knight", true); // add all roles named "knight"
 * // update roles in single request
 * guild.modifyMemberRoles(member, knight, pleb).queue();
 * }
`</pre> *
 *
 *
 * **Warning**<br></br>
 * **This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
 * within one event listener cycle! The changes made by this require cache updates which are triggered by
 * lifecycle events which are received later. This may only be called again once the specific Member has been updated
 * by a [GenericGuildMemberEvent][net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent] targeting the same Member.**
 *
 *
 * This is logically equivalent to:
 * <pre>`Set<Role> roles = new HashSet<>(member.getRoles());
 * roles.addAll(rolesToAdd);
 * roles.removeAll(rolesToRemove);
 * RestAction<Void> action = guild.modifyMemberRoles(member, roles);
`</pre> *
 *
 *
 * You can use [.addRoleToMember] and [.removeRoleFromMember] to make updates
 * independent of the cache.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The Members Roles could not be modified due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The target Member was removed from the Guild before finishing the task
 *
 *
 * @param  member
 * The [Member][net.dv8tion.jda.api.entities.Member] that should be modified
 * @param  rolesToAdd
 * A [Collection][java.util.Collection] of [Roles][Role]
 * to add to the current Roles the specified [Member][net.dv8tion.jda.api.entities.Member] already has, or null
 * @param  rolesToRemove
 * A [Collection][java.util.Collection] of [Roles][Role]
 * to remove from the current Roles the specified [Member][net.dv8tion.jda.api.entities.Member] already has, or null
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account does not have [Permission.MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES]
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the provided roles are higher in the Guild's hierarchy
 * and thus cannot be modified by the currently logged in account
 * @throws IllegalArgumentException
 *
 *  * If the target member is `null`
 *  * If any of the specified Roles is managed or is the `Public Role` of the Guild
 *
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun modifyMemberRoles(
    @Nonnull member: Member?,
    rolesToAdd: Collection<Role?>?,
    rolesToRemove: Collection<Role?>?
): AuditableRestAction<Void?>?

/**
 * Modifies the complete [Role] set of the specified [Member][net.dv8tion.jda.api.entities.Member]
 * <br></br>The provided roles will replace all current Roles of the specified Member.
 *
 *
 * **Warning**<br></br>
 * **This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
 * within one event listener cycle! The changes made by this require cache updates which are triggered by
 * lifecycle events which are received later. This may only be called again once the specific Member has been updated
 * by a [GenericGuildMemberEvent][net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent] targeting the same Member.**
 *
 *
 * **The new roles <u>must not</u> contain the Public Role of the Guild**
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The Members Roles could not be modified due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The target Member was removed from the Guild before finishing the task
 *
 *
 *
 * **Example**<br></br>
 * <pre>`public static void removeRoles(Member member) {
 * Guild guild = member.getGuild();
 * // pass no role, this means we set the roles of the member to an empty array.
 * guild.modifyMemberRoles(member).queue();
 * }
`</pre> *
 *
 * @param  member
 * A [Member][net.dv8tion.jda.api.entities.Member] of which to override the Roles of
 * @param  roles
 * New collection of [Roles][Role] for the specified Member
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account does not have [Permission.MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES]
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the provided roles are higher in the Guild's hierarchy
 * and thus cannot be modified by the currently logged in account
 * @throws IllegalArgumentException
 *
 *  * If any of the provided arguments is `null`
 *  * If any of the provided arguments is not from this Guild
 *  * If any of the specified [Roles][Role] is managed
 *  * If any of the specified [Roles][Role] is the `Public Role` of this Guild
 *
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 *
 * @see .modifyMemberRoles
 */
@Nonnull
@CheckReturnValue
fun modifyMemberRoles(@Nonnull member: Member?, @Nonnull vararg roles: Role?): AuditableRestAction<Void?>? {
    return modifyMemberRoles(member, Arrays.asList(*roles))
}

/**
 * Modifies the complete [Role] set of the specified [Member][net.dv8tion.jda.api.entities.Member]
 * <br></br>The provided roles will replace all current Roles of the specified Member.
 *
 *
 * <u>The new roles **must not** contain the Public Role of the Guild</u>
 *
 *
 * **Warning**<br></br>
 * **This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
 * within one event listener cycle! The changes made by this require cache updates which are triggered by
 * lifecycle events which are received later. This may only be called again once the specific Member has been updated
 * by a [GenericGuildMemberEvent][net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent] targeting the same Member.**
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The Members Roles could not be modified due to a permission discrepancy
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The target Member was removed from the Guild before finishing the task
 *
 *
 *
 * **Example**<br></br>
 * <pre>`public static void makeModerator(Member member) {
 * Guild guild = member.getGuild();
 * List<Role> roles = new ArrayList<>(member.getRoles()); // modifiable copy
 * List<Role> modRoles = guild.getRolesByName("moderator", true); // get roles with name "moderator"
 * roles.addAll(modRoles); // add new roles
 * // update the member with new roles
 * guild.modifyMemberRoles(member, roles).queue();
 * }
`</pre> *
 *
 * @param  member
 * A [Member][net.dv8tion.jda.api.entities.Member] of which to override the Roles of
 * @param  roles
 * New collection of [Roles][Role] for the specified Member
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account does not have [Permission.MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES]
 * @throws net.dv8tion.jda.api.exceptions.HierarchyException
 * If the provided roles are higher in the Guild's hierarchy
 * and thus cannot be modified by the currently logged in account
 * @throws IllegalArgumentException
 *
 *  * If any of the provided arguments is `null`
 *  * If any of the provided arguments is not from this Guild
 *  * If any of the specified [Roles][Role] is managed
 *  * If any of the specified [Roles][Role] is the `Public Role` of this Guild
 *
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 *
 * @see .modifyMemberRoles
 */
@Nonnull
@CheckReturnValue
fun modifyMemberRoles(@Nonnull member: Member?, @Nonnull roles: Collection<Role?>?): AuditableRestAction<Void?>?

/**
 * Transfers the Guild ownership to the specified [Member][net.dv8tion.jda.api.entities.Member]
 * <br></br>Only available if the currently logged in account is the owner of this Guild
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The currently logged in account lost ownership before completing the task
 *
 *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
 * <br></br>The target Member was removed from the Guild before finishing the task
 *
 *
 * @param  newOwner
 * Not-null Member to transfer ownership to
 *
 * @throws net.dv8tion.jda.api.exceptions.PermissionException
 * If the currently logged in account is not the owner of this Guild
 * @throws IllegalArgumentException
 *
 *  * If the specified Member is `null` or not from the same Guild
 *  * If the specified Member already is the Guild owner
 *
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun transferOwnership(@Nonnull newOwner: Member?): AuditableRestAction<Void?>?

/**
 * Creates a new [TextChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the TextChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters
 *
 * @return A specific [ChannelAction][net.dv8tion.jda.api.requests.restaction.ChannelAction]
 * <br></br>This action allows to set fields for the new TextChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createTextChannel(@Nonnull name: String?): ChannelAction<TextChannel?>? {
    return createTextChannel(name, null)
}

/**
 * Creates a new [TextChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the TextChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 * @param  parent
 * The optional parent category for this channel, or null
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters;
 * or the provided parent is not in the same guild.
 *
 * @return A specific [ChannelAction][net.dv8tion.jda.api.requests.restaction.ChannelAction]
 * <br></br>This action allows to set fields for the new TextChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createTextChannel(@Nonnull name: String?, parent: Category?): ChannelAction<TextChannel?>?

/**
 * Creates a new [NewsChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the NewsChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters
 *
 * @return A specific [ChannelAction][net.dv8tion.jda.api.requests.restaction.ChannelAction]
 * <br></br>This action allows to set fields for the new NewsChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createNewsChannel(@Nonnull name: String?): ChannelAction<NewsChannel?>? {
    return createNewsChannel(name, null)
}

/**
 * Creates a new [NewsChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the NewsChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 * @param  parent
 * The optional parent category for this channel, or null
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters;
 * or the provided parent is not in the same guild.
 *
 * @return A specific [ChannelAction][net.dv8tion.jda.api.requests.restaction.ChannelAction]
 * <br></br>This action allows to set fields for the new NewsChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createNewsChannel(@Nonnull name: String?, parent: Category?): ChannelAction<NewsChannel?>?

/**
 * Creates a new [VoiceChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the VoiceChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters
 *
 * @return A specific [ChannelAction]
 * <br></br>This action allows to set fields for the new VoiceChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createVoiceChannel(@Nonnull name: String?): ChannelAction<VoiceChannel?>? {
    return createVoiceChannel(name, null)
}

/**
 * Creates a new [VoiceChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the VoiceChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 * @param  parent
 * The optional parent category for this channel, or null
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters;
 * or the provided parent is not in the same guild.
 *
 * @return A specific [ChannelAction]
 * <br></br>This action allows to set fields for the new VoiceChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createVoiceChannel(@Nonnull name: String?, parent: Category?): ChannelAction<VoiceChannel?>?

/**
 * Creates a new [StageChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the StageChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters
 *
 * @return A specific [ChannelAction]
 * <br></br>This action allows to set fields for the new StageChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createStageChannel(@Nonnull name: String?): ChannelAction<StageChannel?>? {
    return createStageChannel(name, null)
}

/**
 * Creates a new [StageChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the StageChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 * @param  parent
 * The optional parent category for this channel, or null
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters;
 * or the provided parent is not in the same guild.
 *
 * @return A specific [ChannelAction]
 * <br></br>This action allows to set fields for the new StageChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createStageChannel(@Nonnull name: String?, parent: Category?): ChannelAction<StageChannel?>?

/**
 * Creates a new [ForumChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the ForumChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters
 *
 * @return A specific [ChannelAction]
 * <br></br>This action allows to set fields for the new ForumChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createForumChannel(@Nonnull name: String?): ChannelAction<ForumChannel?>? {
    return createForumChannel(name, null)
}

/**
 * Creates a new [ForumChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the ForumChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 * @param  parent
 * The optional parent category for this channel, or null
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters;
 * or the provided parent is not in the same guild.
 *
 * @return A specific [ChannelAction]
 * <br></br>This action allows to set fields for the new ForumChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createForumChannel(@Nonnull name: String?, parent: Category?): ChannelAction<ForumChannel?>?

/**
 * Creates a new [MediaChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the MediaChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters
 *
 * @return A specific [ChannelAction]
 * <br></br>This action allows to set fields for the new MediaChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createMediaChannel(@Nonnull name: String?): ChannelAction<MediaChannel?>? {
    return createMediaChannel(name, null)
}

/**
 * Creates a new [MediaChannel] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the MediaChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 * @param  parent
 * The optional parent category for this channel, or null
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters;
 * or the provided parent is not in the same guild.
 *
 * @return A specific [ChannelAction]
 * <br></br>This action allows to set fields for the new MediaChannel before creating it
 */
@Nonnull
@CheckReturnValue
fun createMediaChannel(@Nonnull name: String?, parent: Category?): ChannelAction<MediaChannel?>?

/**
 * Creates a new [Category] in this Guild.
 * For this to be successful, the logged in account has to have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  name
 * The name of the Category to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
 * @throws IllegalArgumentException
 * If the provided name is `null`, blank, or longer than {@value Channel#MAX_NAME_LENGTH} characters
 *
 * @return A specific [ChannelAction]
 * <br></br>This action allows to set fields for the new Category before creating it
 */
@Nonnull
@CheckReturnValue
fun createCategory(@Nonnull name: String?): ChannelAction<Category?>?

/**
 * Creates a copy of the specified [GuildChannel]
 * in this [Guild][net.dv8tion.jda.api.entities.Guild].
 * <br></br>The provided channel need not be in the same Guild for this to work!
 *
 *
 * This copies the following elements:
 *
 *  1. Name
 *  1. Parent Category (if present)
 *  1. Voice Elements (Bitrate, Userlimit)
 *  1. Text Elements (Topic, NSFW)
 *  1. All permission overrides for Members/Roles
 *
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The channel could not be created due to a permission discrepancy
 *
 *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
 * <br></br>The maximum number of channels were exceeded
 *
 *
 * @param  <T>
 * The channel type
 * @param  channel
 * The [GuildChannel] to use for the copy template
 *
 * @throws java.lang.IllegalArgumentException
 * If the provided channel is `null`
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account does not have the [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission
 *
 * @return A specific [ChannelAction]
 * <br></br>This action allows to set fields for the new GuildChannel before creating it!
 *
 * @since  3.1
 *
 * @see .createTextChannel
 * @see .createVoiceChannel
 * @see ChannelAction ChannelAction
</T> */
@Nonnull
@CheckReturnValue
fun <T : ICopyableChannel?> createCopyOfChannel(@Nonnull channel: T): ChannelAction<T?>? {
    Checks.notNull(channel, "Channel")
    return channel!!.createCopy(this) as ChannelAction<T?>?
}

/**
 * Creates a new [Role] in this Guild.
 * <br></br>It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
 * <br></br>For this to be successful, the logged in account has to have the [MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES] Permission
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The role could not be created due to a permission discrepancy
 *
 *  * [MAX_ROLES_PER_GUILD][net.dv8tion.jda.api.requests.ErrorResponse.MAX_ROLES_PER_GUILD]
 * <br></br>There are too many roles in this Guild
 *
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_ROLES] Permission
 *
 * @return [RoleAction][net.dv8tion.jda.api.requests.restaction.RoleAction]
 * <br></br>Creates a new role with previously selected field values
 */
@Nonnull
@CheckReturnValue
fun createRole(): RoleAction?

/**
 * Creates a new [Role] in this [Guild][net.dv8tion.jda.api.entities.Guild]
 * with the same settings as the given [Role].
 * <br></br>The position of the specified Role does not matter in this case!
 *
 *
 * It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
 * <br></br>For this to be successful, the logged in account has to have the [MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES] Permission
 * and all [Permissions][net.dv8tion.jda.api.Permission] the given [Role] has.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The role could not be created due to a permission discrepancy
 *
 *  * [MAX_ROLES_PER_GUILD][net.dv8tion.jda.api.requests.ErrorResponse.MAX_ROLES_PER_GUILD]
 * <br></br>There are too many roles in this Guild
 *
 *
 * @param  role
 * The [Role] that should be copied
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_ROLES] Permission and every Permission the provided Role has
 * @throws java.lang.IllegalArgumentException
 * If the specified role is `null`
 *
 * @return [RoleAction]
 * <br></br>RoleAction with already copied values from the specified [Role]
 */
@Nonnull
@CheckReturnValue
fun createCopyOfRole(@Nonnull role: Role): RoleAction? {
    Checks.notNull(role, "Role")
    return role.createCopy(this)
}

/**
 * Creates a new [RichCustomEmoji] in this Guild.
 * <br></br>If one or more Roles are specified the new emoji will only be available to Members with any of the specified Roles (see [Member.canInteract])
 * <br></br>For this to be successful, the logged in account has to have the [MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS] Permission.
 *
 *
 * **<u>Unicode emojis are not included as [RichCustomEmoji]!</u>**
 *
 *
 * Note that a guild is limited to 50 normal and 50 animated emojis by default.
 * Some guilds are able to add additional emojis beyond this limitation due to the
 * `MORE_EMOJI` feature (see [Guild.getFeatures()][net.dv8tion.jda.api.entities.Guild.getFeatures]).
 * <br></br>Due to simplicity we do not check for these limits.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The emoji could not be created due to a permission discrepancy
 *
 *
 * @param  name
 * The name for the new emoji
 * @param  icon
 * The [Icon] for the new emoji
 * @param  roles
 * The [Roles][Role] the new emoji should be restricted to
 * <br></br>If no roles are provided the emoji will be available to all Members of this Guild
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the logged in account does not have the [MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS] Permission
 *
 * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction] - Type: [RichCustomEmoji]
 */
@Nonnull
@CheckReturnValue
fun createEmoji(
    @Nonnull name: String?,
    @Nonnull icon: Icon?,
    @Nonnull vararg roles: Role?
): AuditableRestAction<RichCustomEmoji?>?

/**
 * Creates a new [GuildSticker] in this Guild.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [INVALID_FILE_UPLOADED][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_FILE_UPLOADED]
 * <br></br>The sticker file asset is not in a supported file format
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The sticker could not be created due to a permission discrepancy
 *
 *
 * @param  name
 * The sticker name (2-30 characters)
 * @param  description
 * The sticker description (2-100 characters, or empty)
 * @param  file
 * The sticker file containing the asset (png/apng/gif/lottie) with valid file extension (png, gif, or json)
 * @param  tags
 * The tags to use for auto-suggestions (Up to 200 characters in total)
 *
 * @throws InsufficientPermissionException
 * If the currently logged in account does not have the [MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS] permission
 * @throws IllegalArgumentException
 *
 *  * If the name is not between 2 and 30 characters long
 *  * If the description is more than 100 characters long or exactly 1 character long
 *  * If the asset file is null or of an invalid format (must be PNG, GIF, or LOTTIE)
 *  * If anything is `null`
 *
 *
 * @return [AuditableRestAction] - Type: [GuildSticker]
 */
@Nonnull
@CheckReturnValue
fun createSticker(
    @Nonnull name: String?,
    @Nonnull description: String?,
    @Nonnull file: FileUpload?,
    @Nonnull tags: Collection<String>?
): AuditableRestAction<GuildSticker?>?

/**
 * Creates a new [GuildSticker] in this Guild.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [INVALID_FILE_UPLOADED][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_FILE_UPLOADED]
 * <br></br>The sticker file asset is not in a supported file format
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>The sticker could not be created due to a permission discrepancy
 *
 *
 * @param  name
 * The sticker name (2-30 characters)
 * @param  description
 * The sticker description (2-100 characters, or empty)
 * @param  file
 * The sticker file containing the asset (png/apng/gif/lottie) with valid file extension (png, gif, or json)
 * @param  tag
 * The sticker tag used for suggestions (emoji or tag words)
 * @param  tags
 * Additional tags to use for suggestions
 *
 * @throws InsufficientPermissionException
 * If the currently logged in account does not have the [MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS] permission
 * @throws IllegalArgumentException
 *
 *  * If the name is not between 2 and 30 characters long
 *  * If the description is more than 100 characters long or exactly 1 character long
 *  * If the asset file is null or of an invalid format (must be PNG, GIF, or LOTTIE)
 *  * If anything is `null`
 *
 *
 * @return [AuditableRestAction] - Type: [GuildSticker]
 */
@Nonnull
@CheckReturnValue
fun createSticker(
    @Nonnull name: String?,
    @Nonnull description: String?,
    @Nonnull file: FileUpload?,
    @Nonnull tag: String,
    @Nonnull vararg tags: String?
): AuditableRestAction<GuildSticker?>? {
    val list: MutableList<String> = ArrayList(tags.size + 1)
    list.add(tag)
    Collections.addAll(list, *tags)
    return createSticker(name, description, file, list)
}

/**
 * Deletes a sticker from the guild.
 *
 *
 * The returned [RestAction][net.dv8tion.jda.api.requests.RestAction] can encounter the following Discord errors:
 *
 *  * [UNKNOWN_STICKER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_STICKER]
 * <br></br>Occurs when the provided id does not refer to a sticker known by Discord.
 *
 *
 * @throws IllegalStateException
 * If null is provided
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account does not have [MANAGE_GUILD_EXPRESSIONS][Permission.MANAGE_GUILD_EXPRESSIONS] in the guild.
 *
 * @return [AuditableRestAction]
 */
@Nonnull
@CheckReturnValue
fun deleteSticker(@Nonnull id: StickerSnowflake?): AuditableRestAction<Void?>?

/**
 * Creates a new [ScheduledEvent].
 * Events created with this method will be of [Type.EXTERNAL][ScheduledEvent.Type.EXTERNAL].
 * These events are set to take place at an external location.
 *
 *
 * **Requirements**<br></br>
 *
 * Events are required to have a name, location and start time.
 * Additionally, an end time *must* also be specified for events of [Type.EXTERNAL][ScheduledEvent.Type.EXTERNAL].
 * [Permission.MANAGE_EVENTS] is required on the guild level in order to create this type of event.
 *
 *
 * **Example**<br></br>
 * <pre>`guild.createScheduledEvent("Cactus Beauty Contest", "Mike's Backyard", OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusHours(3))
 * .setDescription("Come and have your cacti judged! _Must be spikey to enter_")
 * .queue();
`</pre> *
 *
 * @param  name
 * the name for this scheduled event, 1-100 characters
 * @param  location
 * the external location for this scheduled event, 1-100 characters
 * @param  startTime
 * the start time for this scheduled event, can't be in the past or after the end time
 * @param  endTime
 * the end time for this scheduled event, has to be later than the start time
 *
 * @throws java.lang.IllegalArgumentException
 *
 *  * If a required parameter is `null` or empty
 *  * If the start time is in the past
 *  * If the end time is before the start time
 *  * If the name is longer than 100 characters
 *  * If the description is longer than 1000 characters
 *  * If the location is longer than 100 characters
 *
 *
 * @return [ScheduledEventAction]
 */
@Nonnull
@CheckReturnValue
fun createScheduledEvent(
    @Nonnull name: String?,
    @Nonnull location: String?,
    @Nonnull startTime: OffsetDateTime?,
    @Nonnull endTime: OffsetDateTime?
): ScheduledEventAction?

/**
 * Creates a new [ScheduledEvent].
 *
 *
 * **Requirements**<br></br>
 *
 * Events are required to have a name, channel and start time. Depending on the
 * type of channel provided, an event will be of one of two different [Types][ScheduledEvent.Type]:
 *
 *  1.
 * [Type.STAGE_INSTANCE][ScheduledEvent.Type.STAGE_INSTANCE]
 * <br></br>These events are set to take place inside of a [StageChannel]. The
 * following permissions are required in the specified stage channel in order to create an event there:
 *
 *  * [Permission.MANAGE_EVENTS]
 *  * [Permission.MANAGE_CHANNEL]
 *  * [Permission.VOICE_MUTE_OTHERS]
 *  * [Permission.VOICE_MOVE_OTHERS]}
 *
 *
 *  1.
 * [Type.VOICE][ScheduledEvent.Type.VOICE]
 * <br></br>These events are set to take place inside of a [VoiceChannel]. The
 * following permissions are required in the specified voice channel in order to create an event there:
 *
 *  * [Permission.MANAGE_EVENTS]
 *  * [Permission.VIEW_CHANNEL]
 *  * [Permission.VOICE_CONNECT]
 *
 *
 *
 *
 *
 * **Example**<br></br>
 * <pre>`guild.createScheduledEvent("Cactus Beauty Contest", guild.getGuildChannelById(channelId), OffsetDateTime.now().plusHours(1))
 * .setDescription("Come and have your cacti judged! _Must be spikey to enter_")
 * .queue();
`</pre> *
 *
 * @param  name
 * the name for this scheduled event, 1-100 characters
 * @param  channel
 * the voice or stage channel where this scheduled event will take place
 * @param  startTime
 * the start time for this scheduled event, can't be in the past
 *
 * @throws java.lang.IllegalArgumentException
 *
 *  * If a required parameter is `null` or empty
 *  * If the start time is in the past
 *  * If the name is longer than 100 characters
 *  * If the description is longer than 1000 characters
 *  * If the channel is not a Stage or Voice channel
 *  * If the channel is not from the same guild as the scheduled event
 *
 *
 * @return [ScheduledEventAction]
 */
@Nonnull
@CheckReturnValue
fun createScheduledEvent(
    @Nonnull name: String?,
    @Nonnull channel: GuildChannel?,
    @Nonnull startTime: OffsetDateTime?
): ScheduledEventAction?

/**
 * Modifies the positional order of [Guild.getCategories()][net.dv8tion.jda.api.entities.Guild.getCategories]
 * using a specific [RestAction] extension to allow moving Channels
 * [up][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveUp]/[down][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveDown]
 * or [to][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveTo] a specific position.
 * <br></br>This uses **ascending** order with a 0 based index.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [UNNKOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
 * <br></br>One of the channels has been deleted before the completion of the task
 *
 *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
 * <br></br>The currently logged in account was removed from the Guild
 *
 *
 * @return [ChannelOrderAction][net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction] - Type: [Category]
 */
@Nonnull
@CheckReturnValue
fun modifyCategoryPositions(): ChannelOrderAction?

/**
 * Modifies the positional order of [Guild.getTextChannels()][net.dv8tion.jda.api.entities.Guild.getTextChannels]
 * using a specific [RestAction] extension to allow moving Channels
 * [up][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveUp]/[down][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveDown]
 * or [to][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveTo] a specific position.
 * <br></br>This uses **ascending** order with a 0 based index.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [UNNKOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
 * <br></br>One of the channels has been deleted before the completion of the task
 *
 *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
 * <br></br>The currently logged in account was removed from the Guild
 *
 *
 * @return [ChannelOrderAction] - Type: [TextChannel]
 */
@Nonnull
@CheckReturnValue
fun modifyTextChannelPositions(): ChannelOrderAction?

/**
 * Modifies the positional order of [Guild.getVoiceChannels()][net.dv8tion.jda.api.entities.Guild.getVoiceChannels]
 * using a specific [RestAction] extension to allow moving Channels
 * [up][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveUp]/[down][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveDown]
 * or [to][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveTo] a specific position.
 * <br></br>This uses **ascending** order with a 0 based index.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [UNNKOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
 * <br></br>One of the channels has been deleted before the completion of the task
 *
 *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
 * <br></br>The currently logged in account was removed from the Guild
 *
 *
 * @return [ChannelOrderAction] - Type: [VoiceChannel]
 */
@Nonnull
@CheckReturnValue
fun modifyVoiceChannelPositions(): ChannelOrderAction?

/**
 * Modifies the positional order of [Category#getTextChannels()][Category.getTextChannels]
 * using an extension of [ChannelOrderAction]
 * specialized for ordering the nested [TextChannels][TextChannel] of this
 * [Category].
 * <br></br>Like `ChannelOrderAction`, the returned [CategoryOrderAction][net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction]
 * can be used to move TextChannels [up][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveUp],
 * [down][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveDown], or
 * [to][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveTo] a specific position.
 * <br></br>This uses **ascending** order with a 0 based index.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [UNNKOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
 * <br></br>One of the channels has been deleted before the completion of the task.
 *
 *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
 * <br></br>The currently logged in account was removed from the Guild.
 *
 *
 * @param  category
 * The [Category] to order
 * [TextChannels][TextChannel] from.
 *
 * @return [CategoryOrderAction][net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction] - Type: [TextChannel]
 */
@Nonnull
@CheckReturnValue
fun modifyTextChannelPositions(@Nonnull category: Category?): CategoryOrderAction?

/**
 * Modifies the positional order of [Category#getVoiceChannels()][Category.getVoiceChannels]
 * using an extension of [ChannelOrderAction]
 * specialized for ordering the nested [VoiceChannels][VoiceChannel] of this
 * [Category].
 * <br></br>Like `ChannelOrderAction`, the returned [CategoryOrderAction]
 * can be used to move VoiceChannels [up][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveUp],
 * [down][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveDown], or
 * [to][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveTo] a specific position.
 * <br></br>This uses **ascending** order with a 0 based index.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [UNNKOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
 * <br></br>One of the channels has been deleted before the completion of the task.
 *
 *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
 * <br></br>The currently logged in account was removed from the Guild.
 *
 *
 * @param  category
 * The [Category] to order
 * [VoiceChannels][VoiceChannel] from.
 *
 * @return [CategoryOrderAction] - Type: [VoiceChannels][VoiceChannel]
 */
@Nonnull
@CheckReturnValue
fun modifyVoiceChannelPositions(@Nonnull category: Category?): CategoryOrderAction?

/**
 * Modifies the positional order of [Guild.getRoles()][net.dv8tion.jda.api.entities.Guild.getRoles]
 * using a specific [RestAction] extension to allow moving Roles
 * [up][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveUp]/[down][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveDown]
 * or [to][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveTo] a specific position.
 *
 *
 * You can also move roles to a position relative to another role, by using [moveBelow(...)][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveBelow]
 * and [moveAbove(...)][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveAbove].
 *
 *
 * This uses **descending** ordering which means the highest role is first!
 * <br></br>This means the lowest role appears at index `n - 1` and the highest role at index `0`.
 * <br></br>Providing `true` to [.modifyRolePositions] will result in the ordering being
 * in ascending order, with the highest role at index `n - 1` and the lowest at index `0`.
 *
 * <br></br>As a note: [Member.getRoles()][net.dv8tion.jda.api.entities.Member.getRoles]
 * and [Guild.getRoles()][net.dv8tion.jda.api.entities.Guild.getRoles] are both in descending order, just like this method.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [UNKNOWN_ROLE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_ROLE]
 * <br></br>One of the roles was deleted before the completion of the task
 *
 *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
 * <br></br>The currently logged in account was removed from the Guild
 *
 *
 * @return [RoleOrderAction]
 */
@Nonnull
@CheckReturnValue
fun modifyRolePositions(): RoleOrderAction? {
    return modifyRolePositions(false)
}

/**
 * Modifies the positional order of [Guild.getRoles()][net.dv8tion.jda.api.entities.Guild.getRoles]
 * using a specific [RestAction] extension to allow moving Roles
 * [up][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveUp]/[down][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveDown]
 * or [to][net.dv8tion.jda.api.requests.restaction.order.OrderAction.moveTo] a specific position.
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
 *
 *  * [UNKNOWN_ROLE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_ROLE]
 * <br></br>One of the roles was deleted before the completion of the task
 *
 *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
 * <br></br>The currently logged in account was removed from the Guild
 *
 *
 * @param  useAscendingOrder
 * Defines the ordering of the OrderAction. If `false`, the OrderAction will be in the ordering
 * defined by Discord for roles, which is Descending. This means that the highest role appears at index `0`
 * and the lowest role at index `n - 1`. Providing `true` will result in the ordering being
 * in ascending order, with the lower role at index `0` and the highest at index `n - 1`.
 * <br></br>As a note: [Member.getRoles()][net.dv8tion.jda.api.entities.Member.getRoles]
 * and [Guild.getRoles()][net.dv8tion.jda.api.entities.Guild.getRoles] are both in descending order.
 *
 * @return [RoleOrderAction]
 */
@Nonnull
@CheckReturnValue
fun modifyRolePositions(useAscendingOrder: Boolean): RoleOrderAction?

/**
 * The [Manager][GuildWelcomeScreenManager] for this guild's welcome screen, used to modify
 * properties of the welcome screen like if the welcome screen is enabled, the description and welcome channels.
 * <br></br>You modify multiple fields in one request by chaining setters before calling [RestAction.queue()][net.dv8tion.jda.api.requests.RestAction.queue].
 *
 * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
 * If the currently logged in account does not have [Permission.MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER]
 *
 * @return The GuildWelcomeScreenManager for this guild's welcome screen
 */
@Nonnull
@CheckReturnValue
fun modifyWelcomeScreen(): GuildWelcomeScreenManager?
//////////////////////////
/**
 * Represents the idle time allowed until a user is moved to the
 * AFK [VoiceChannel] if one is set
 * ([Guild.getAfkChannel()][net.dv8tion.jda.api.entities.Guild.getAfkChannel]).
 */
enum class Timeout(
    /**
     * The amount of seconds represented by this [Timeout].
     *
     * @return An positive non-negative int representing the timeout amount in seconds.
     */
    val seconds: Int
) {
    SECONDS_60(60),
    SECONDS_300(300),
    SECONDS_900(900),
    SECONDS_1800(1800),
    SECONDS_3600(3600);

    companion object {
        /**
         * Retrieves the [Timeout][net.dv8tion.jda.api.entities.Guild.Timeout] based on the amount of seconds requested.
         * <br></br>If the `seconds` amount provided is not valid for Discord, an IllegalArgumentException will be thrown.
         *
         * @param  seconds
         * The amount of seconds before idle timeout.
         *
         * @throws java.lang.IllegalArgumentException
         * If the provided `seconds` is an invalid timeout amount.
         *
         * @return The [Timeout][net.dv8tion.jda.api.entities.Guild.Timeout] related to the amount of seconds provided.
         */
        @Nonnull
        fun fromKey(seconds: Int): Guild.Timeout {
            for (t in Guild.Timeout.entries) {
                if (t.getSeconds() == seconds) return t
            }
            throw IllegalArgumentException("Provided key was not recognized. Seconds: $seconds")
        }
    }
}

/**
 * Represents the Verification-Level of the Guild.
 * The Verification-Level determines what requirement you have to meet to be able to speak in this Guild.
 *
 *
 * <br></br>**None**      -&gt; everyone can talk.
 * <br></br>**Low**       -&gt; verified email required.
 * <br></br>**Medium**    -&gt; you have to be member of discord for at least 5min.
 * <br></br>**High**      -&gt; you have to be member of this guild for at least 10min.
 * <br></br>**Very High** -&gt; you must have a verified phone on your discord account.
 */
enum class VerificationLevel(
    /**
     * The Discord id key for this Verification Level.
     *
     * @return Integer id key for this VerificationLevel.
     */
    val key: Int
) {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    VERY_HIGH(4),
    UNKNOWN(-1);

    companion object {
        /**
         * Used to retrieve a [VerificationLevel][net.dv8tion.jda.api.entities.Guild.VerificationLevel] based
         * on the Discord id key.
         *
         * @param  key
         * The Discord id key representing the requested VerificationLevel.
         *
         * @return The VerificationLevel related to the provided key, or [VerificationLevel.UNKNOWN][.UNKNOWN] if the key is not recognized.
         */
        @Nonnull
        fun fromKey(key: Int): VerificationLevel {
            for (level in entries) {
                if (level.key == key) return level
            }
            return UNKNOWN
        }
    }
}

/**
 * Represents the Notification-level of the Guild.
 * The Verification-Level determines what messages you receive pings for.
 *
 *
 * <br></br>**All_Messages**   -&gt; Every message sent in this guild will result in a message ping.
 * <br></br>**Mentions_Only**  -&gt; Only messages that specifically mention will result in a ping.
 */
enum class NotificationLevel(
    /**
     * The Discord id key used to represent this NotificationLevel.
     *
     * @return Integer id for this NotificationLevel.
     */
    val key: Int
) {
    ALL_MESSAGES(0),
    MENTIONS_ONLY(1),
    UNKNOWN(-1);

    companion object {
        /**
         * Used to retrieve a [NotificationLevel][net.dv8tion.jda.api.entities.Guild.NotificationLevel] based
         * on the Discord id key.
         *
         * @param  key
         * The Discord id key representing the requested NotificationLevel.
         *
         * @return The NotificationLevel related to the provided key, or [NotificationLevel.UNKNOWN][.UNKNOWN] if the key is not recognized.
         */
        @Nonnull
        fun fromKey(key: Int): NotificationLevel {
            for (level in entries) {
                if (level.key == key) return level
            }
            return UNKNOWN
        }
    }
}

/**
 * Represents the Multifactor Authentication level required by the Guild.
 * <br></br>The MFA Level restricts administrator functions to account with MFA Level equal to or higher than that set by the guild.
 *
 *
 * <br></br>**None**             -&gt; There is no MFA level restriction on administrator functions in this guild.
 * <br></br>**Two_Factor_Auth**  -&gt; Users must have 2FA enabled on their account to perform administrator functions.
 */
enum class MFALevel(
    /**
     * The Discord id key used to represent this MFALevel.
     *
     * @return Integer id for this MFALevel.
     */
    val key: Int
) {
    NONE(0),
    TWO_FACTOR_AUTH(1),
    UNKNOWN(-1);

    companion object {
        /**
         * Used to retrieve a [MFALevel][net.dv8tion.jda.api.entities.Guild.MFALevel] based
         * on the Discord id key.
         *
         * @param  key
         * The Discord id key representing the requested MFALevel.
         *
         * @return The MFALevel related to the provided key, or [MFALevel.UNKNOWN][.UNKNOWN] if the key is not recognized.
         */
        @Nonnull
        fun fromKey(key: Int): MFALevel {
            for (level in entries) {
                if (level.key == key) return level
            }
            return UNKNOWN
        }
    }
}

/**
 * The Explicit-Content-Filter Level of a Guild.
 * <br></br>This decides whom's messages should be scanned for explicit content.
 */
enum class ExplicitContentLevel(
    /**
     * The key for this level
     *
     * @return key
     */
    val key: Int,
    /**
     * Description of this level in the official Discord Client (as of 5th May, 2017)
     *
     * @return Description for this level
     */
    @get:Nonnull val description: String
) {
    OFF(0, "Don't scan any messages."),
    NO_ROLE(1, "Scan messages from members without a role."),
    ALL(2, "Scan messages sent by all members."),
    UNKNOWN(-1, "Unknown filter level!");

    companion object {
        @Nonnull
        fun fromKey(key: Int): ExplicitContentLevel {
            for (level in entries) {
                if (level.key == key) return level
            }
            return UNKNOWN
        }
    }
}

/**
 * Represents the NSFW level for this guild.
 */
enum class NSFWLevel(
    /**
     * The Discord id key used to represent this NSFW level.
     *
     * @return Integer id for this NSFW level.
     */
    val key: Int
) {
    /**
     * Discord has not rated this guild.
     */
    DEFAULT(0),

    /**
     * Is classified as a NSFW server
     */
    EXPLICIT(1),

    /**
     * Doesn't classify as a NSFW server
     */
    SAFE(2),

    /**
     * Is classified as NSFW and has an age restriction in place
     */
    AGE_RESTRICTED(3),

    /**
     * Placeholder for unsupported levels.
     */
    UNKNOWN(-1);

    companion object {
        /**
         * Used to retrieve a [NSFWLevel][net.dv8tion.jda.api.entities.Guild.NSFWLevel] based
         * on the Discord id key.
         *
         * @param  key
         * The Discord id key representing the requested NSFWLevel.
         *
         * @return The NSFWLevel related to the provided key, or [NSFWLevel.UNKNOWN][.UNKNOWN] if the key is not recognized.
         */
        @Nonnull
        fun fromKey(key: Int): NSFWLevel {
            for (level in entries) {
                if (level.key == key) return level
            }
            return UNKNOWN
        }
    }
}

/**
 * The boost tier for this guild.
 * <br></br>Each tier unlocks new perks for a guild that can be seen in the [features][.getFeatures].
 *
 * @since  4.0.0
 */
enum class BoostTier(
    /**
     * The API key used to represent this tier, identical to the ordinal.
     *
     * @return The key
     */
    val key: Int,
    /**
     * The maximum bitrate that can be applied to voice channels when this tier is reached.
     *
     * @return The maximum bitrate
     *
     * @see net.dv8tion.jda.api.entities.Guild.getMaxBitrate
     */
    val maxBitrate: Int,
    /**
     * The maximum amount of custom emojis a guild can have when this tier is reached.
     *
     * @return The maximum emojis
     *
     * @see net.dv8tion.jda.api.entities.Guild.getMaxEmojis
     */
    val maxEmojis: Int
) {
    /**
     * The default tier.
     * <br></br>Unlocked at 0 boosters.
     */
    NONE(0, 96000, 50),

    /**
     * The first tier.
     * <br></br>Unlocked at 2 boosters.
     */
    TIER_1(1, 128000, 100),

    /**
     * The second tier.
     * <br></br>Unlocked at 7 boosters.
     */
    TIER_2(2, 256000, 150),

    /**
     * The third tier.
     * <br></br>Unlocked at 14 boosters.
     */
    TIER_3(3, 384000, 250),

    /**
     * Placeholder for future tiers.
     */
    UNKNOWN(-1, Int.MAX_VALUE, Int.MAX_VALUE);

    val maxFileSize: Long
        /**
         * The maximum size for files that can be uploaded to this Guild.
         *
         * @return The maximum file size of this Guild
         *
         * @see net.dv8tion.jda.api.entities.Guild.getMaxFileSize
         */
        get() {
            if (key == 2) return 50 shl 20 else if (key == 3) return 100 shl 20
            return Message.Companion.MAX_FILE_SIZE.toLong()
        }

    companion object {
        /**
         * Resolves the provided API key to the boost tier.
         *
         * @param  key
         * The API key
         *
         * @return The BoostTier or [.UNKNOWN]
         */
        @Nonnull
        fun fromKey(key: Int): BoostTier {
            for (tier in entries) {
                if (tier.key == key) return tier
            }
            return UNKNOWN
        }
    }
}

/**
 * Represents a Ban object.
 *
 * @see .retrieveBanList
 * @see [Discord Docs: Ban Object](https://discord.com/developers/docs/resources/guild.ban-object)
 */
class Ban(
    /**
     * The [User][net.dv8tion.jda.api.entities.User] that was banned
     *
     * @return The banned User
     */
    @get:Nonnull val user: User, protected val reason: String
) {
    /**
     * The reason why this user was banned
     *
     * @return The reason for this ban, or `null`
     */
    fun getReason(): String? {
        return reason
    }

    override fun toString(): String {
        return EntityString(this)
            .addMetadata("user", user)
            .addMetadata("reason", reason)
            .toString()
    }
}

/**
 * Meta-Data for a Guild
 *
 * @since 4.2.0
 */
class MetaData(
    /**
     * The active member limit for this guild.
     * <br></br>This limit restricts how many users can be member for this guild at once.
     *
     * @return The member limit
     */
    val memberLimit: Int,
    /**
     * The active presence limit for this guild.
     * <br></br>This limit restricts how many users can be connected/online for this guild at once.
     *
     * @return The presence limit
     */
    val presenceLimit: Int,
    /**
     * The approximate number of online members in this guild.
     *
     * @return The approximate presence count
     */
    val approximatePresences: Int,
    /**
     * The approximate number of members in this guild.
     *
     * @return The approximate member count
     */
    val approximateMembers: Int
)

companion object {
    /** Template for [.getIconUrl].  */
    const val ICON_URL = "https://cdn.discordapp.com/icons/%s/%s.%s"

    /** Template for [.getSplashUrl].  */
    const val SPLASH_URL = "https://cdn.discordapp.com/splashes/%s/%s.png"

    /** Template for [.getBannerUrl].  */
    const val BANNER_URL = "https://cdn.discordapp.com/banners/%s/%s.%s"
}
}

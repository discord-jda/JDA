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
import net.dv8tion.jda.api.managers.RoleManager
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.RoleAction
import java.awt.Color
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a [Guild][net.dv8tion.jda.api.entities.Guild]'s Role. Used to control permissions for Members.
 *
 * @see Guild.getRoleCache
 * @see Guild.getRoleById
 * @see Guild.getRolesByName
 * @see Guild.getRoles
 * @see JDA.getRoleCache
 * @see JDA.getRoleById
 * @see JDA.getRolesByName
 * @see JDA.getRoles
 */
open interface Role : IMentionable, IPermissionHolder, Comparable<Role?> {
    /**
     * The hierarchical position of this [Role][net.dv8tion.jda.api.entities.Role]
     * in the [Guild][net.dv8tion.jda.api.entities.Guild] hierarchy. (higher value means higher role).
     * <br></br>The [net.dv8tion.jda.api.entities.Guild.getPublicRole]'s getPosition() always returns -1.
     *
     * @throws IllegalStateException
     * If this role is not in the guild cache
     *
     * @return The position of this [Role][net.dv8tion.jda.api.entities.Role] as integer.
     */
    @JvmField
    val position: Int

    /**
     * The actual position of the [Role][net.dv8tion.jda.api.entities.Role] as stored and given by Discord.
     * <br></br>Role positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more roles share the same position then they are sorted based on their creation date.
     * <br></br>The more recent a role was created, the lower it is in the hierarchy. This is handled by [.getPosition]
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * Role then this method will give you that value.
     *
     * @return The true, Discord stored, position of the [Role][net.dv8tion.jda.api.entities.Role].
     */
    @JvmField
    val positionRaw: Int

    @JvmField
    @get:Nonnull
    val name: String?

    /**
     * Whether this [Role][net.dv8tion.jda.api.entities.Role] is managed by an integration
     *
     * @return True, if this [Role][net.dv8tion.jda.api.entities.Role] is managed.
     */
    @JvmField
    val isManaged: Boolean

    /**
     * Whether this [Role][net.dv8tion.jda.api.entities.Role] is hoisted
     * <br></br>Members in a hoisted role are displayed in their own grouping on the user-list
     *
     * @return True, if this [Role][net.dv8tion.jda.api.entities.Role] is hoisted.
     */
    val isHoisted: Boolean

    /**
     * Whether or not this Role is mentionable
     *
     * @return True, if Role is mentionable.
     */
    val isMentionable: Boolean

    /**
     * The `long` representation of the literal permissions that this [Role][net.dv8tion.jda.api.entities.Role] has.
     * <br></br>**NOTE:** these do not necessarily represent the permissions this role will have in a [GuildChannel][net.dv8tion.jda.api.entities.channel.middleman.GuildChannel].
     *
     * @return Never-negative long containing offset permissions of this role.
     */
    @JvmField
    val permissionsRaw: Long

    /**
     * The color this [Role][net.dv8tion.jda.api.entities.Role] is displayed in.
     *
     * @return Color value of Role-color
     *
     * @see .getColorRaw
     */
    val color: Color?

    /**
     * The raw color RGB value used for this role
     * <br></br>Defaults to [.DEFAULT_COLOR_RAW] if this role has no set color
     *
     * @return The raw RGB color value or default
     */
    @JvmField
    val colorRaw: Int

    /**
     * Whether this role is the @everyone role for its [Guild][net.dv8tion.jda.api.entities.Guild],
     * which is assigned to everyone who joins the [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @return True, if and only if this [Role][net.dv8tion.jda.api.entities.Role] is the public role
     * for the [Guild][net.dv8tion.jda.api.entities.Guild] provided by [.getGuild].
     *
     * @see net.dv8tion.jda.api.entities.Guild.getPublicRole
     */
    @JvmField
    val isPublicRole: Boolean

    /**
     * Whether this Role can interact with the specified Role.
     * (move/manage/etc.)
     *
     * @param  role
     * The not-null role to compare to
     *
     * @throws IllegalArgumentException
     * if the provided Role is null or not from the same [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @return True, if this role can interact with the specified role
     */
    fun canInteract(@Nonnull role: Role?): Boolean

    @get:Nonnull
    abstract override val guild: Guild

    /**
     * Creates a new [Role][net.dv8tion.jda.api.entities.Role] in the specified [Guild][net.dv8tion.jda.api.entities.Guild]
     * with the same settings as the given [Role][net.dv8tion.jda.api.entities.Role].
     * <br></br>The position of the specified Role does not matter in this case!
     * <br></br>**If this [Role] has an [Icon][RoleIcon] set, only its emoji can be copied over.**
     *
     *
     * It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br></br>For this to be successful, the logged in account has to have the [MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES] Permission
     * and all [Permissions][net.dv8tion.jda.api.Permission] the given [Role][net.dv8tion.jda.api.entities.Role] has.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The role could not be created due to a permission discrepancy
     *
     *  * [MAX_ROLES_PER_GUILD][net.dv8tion.jda.api.requests.ErrorResponse.MAX_ROLES_PER_GUILD]
     * <br></br>There are too many roles in this Guild
     *
     *
     * @param  guild
     * The [Role][net.dv8tion.jda.api.entities.Role] that should be copied
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_ROLES] Permission and every Permission the provided Role has
     * @throws java.lang.IllegalArgumentException
     * If the specified guild is `null`
     *
     * @return [RoleAction]
     * <br></br>RoleAction with already copied values from the specified [Role][net.dv8tion.jda.api.entities.Role]
     */
    @Nonnull
    @CheckReturnValue
    fun createCopy(@Nonnull guild: Guild?): RoleAction?

    /**
     * Creates a new [Role][net.dv8tion.jda.api.entities.Role] in this [Guild][net.dv8tion.jda.api.entities.Guild]
     * with the same settings as the given [Role][net.dv8tion.jda.api.entities.Role].
     * <br></br>The position of the specified Role does not matter in this case!
     * <br></br>**If this [Role] has an [Icon][RoleIcon] set, only its emoji can be copied over.**
     *
     *
     * It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br></br>For this to be successful, the logged in account has to have the [MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES] Permission
     * and all [Permissions][net.dv8tion.jda.api.Permission] the given [Role][net.dv8tion.jda.api.entities.Role] has.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The role could not be created due to a permission discrepancy
     *
     *  * [MAX_ROLES_PER_GUILD][net.dv8tion.jda.api.requests.ErrorResponse.MAX_ROLES_PER_GUILD]
     * <br></br>There are too many roles in this Guild
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_ROLES] Permission and every Permission the provided Role has
     *
     * @return [RoleAction]
     * <br></br>RoleAction with already copied values from the specified [Role][net.dv8tion.jda.api.entities.Role]
     */
    @Nonnull
    @CheckReturnValue
    fun createCopy(): RoleAction? {
        return createCopy(guild)
    }

    @JvmField
    @get:Nonnull
    val manager: RoleManager?

    /**
     * Deletes this Role.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_ROLE]
     * <br></br>If the the role was already deleted.
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The send request was attempted after the account lost
     * [Permission.MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES] in the channel.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>If we were removed from the Guild
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If we don't have the permission to [MANAGE_ROLES][net.dv8tion.jda.api.Permission.MANAGE_ROLES]
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     * If the role is too high in the role hierarchy to be deleted
     *
     * @return [net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): AuditableRestAction<Void?>?

    @JvmField
    @get:Nonnull
    val jDA: JDA?

    @JvmField
    @get:Nonnull
    val tags: RoleTags

    /**
     * The [Icon][RoleIcon] of this role or `null` if no custom image or emoji is set.
     * This icon will be displayed next to the role's name in the members tab and in chat.
     *
     * @return Possibly-null [Icon][RoleIcon] of this role
     *
     * @since  4.3.1
     */
    @JvmField
    val icon: RoleIcon?

    /**
     * Tags associated with this role.
     *
     * @since  4.2.1
     */
    open interface RoleTags {
        /**
         * Whether this role is associated with a bot.
         *
         * @return True, if this role is for a bot
         */
        val isBot: Boolean

        /**
         * The id for the bot associated with this role.
         *
         * @return The bot id, or 0 if this role is not for a bot
         *
         * @see .isBot
         */
        val botIdLong: Long
        val botId: String?
            /**
             * The id for the bot associated with this role.
             *
             * @return The bot id, or null if this role is not for a bot
             *
             * @see .isBot
             */
            get() {
                return if (isBot) java.lang.Long.toUnsignedString(botIdLong) else null
            }

        /**
         * Whether this role is the boost role of this guild.
         *
         * @return True, if this role is the boost role
         */
        val isBoost: Boolean

        /**
         * Whether this role is managed by an integration.
         * <br></br>This is usually true for roles such as those created for twitch subscribers.
         *
         * @return True, if this role is managed by an integration
         */
        val isIntegration: Boolean

        /**
         * The id for the integration associated with this role.
         *
         * @return The integration id, or 0 if this role is not for an integration
         *
         * @see .isIntegration
         */
        val integrationIdLong: Long
        val integrationId: String?
            /**
             * The id for the integration associated with this role.
             *
             * @return The integration id, or null if this role is not for an integration
             *
             * @see .isIntegration
             */
            get() {
                return if (isIntegration) java.lang.Long.toUnsignedString(integrationIdLong) else null
            }

        /**
         * Whether this role can be acquired through a premium subscription purchase.
         * A role would also need [.isAvailableForPurchase] to also be true for a user to actually be
         * able to purchase the role.
         *
         * @return True, if this is a subscription role
         *
         * @see .getSubscriptionIdLong
         * @see .isAvailableForPurchase
         */
        fun hasSubscriptionListing(): Boolean {
            return subscriptionIdLong != 0L
        }

        /**
         * The subscription listing id for this role. If a role has a subscription id then it is a premium role that
         * can be acquired by users via purchase.
         *
         * @return The listing id, or 0 if this role is not for a subscription listing
         *
         * @see .isAvailableForPurchase
         */
        val subscriptionIdLong: Long
        val subscriptionId: String?
            /**
             * The subscription listing id for this role. If a role has a subscription id then it is a premium role that
             * can be acquired by users via purchase.
             *
             * @return The listing id, or null if this role is not for a subscription listing
             *
             * @see .isAvailableForPurchase
             */
            get() {
                return if (hasSubscriptionListing()) java.lang.Long.toUnsignedString(subscriptionIdLong) else null
            }

        /**
         * Whether this role has been published for user purchasing. Only [premium roles][.hasSubscriptionListing]
         * can be purchased. However, a premium role must be published before it can be purchased.
         * Additionally, a premium role can be unpublished after it has been published. Doing so will make it
         * no longer available for purchase but will not remove the role from users who have already purchased it.
         *
         * @return True, if this role is purchasable
         *
         * @see .hasSubscriptionListing
         */
        val isAvailableForPurchase: Boolean

        /**
         * Whether this role is acquired through a user connection.
         * <br></br>Such as external services like twitter or reddit.
         * This also includes custom third-party applications, such as those managed by bots via [RoleConnectionMetadata].
         *
         * @return True, if this role is acquired through a user connection
         *
         * @see [Configuring App Metadata for Linked Roles](https://discord.com/developers/docs/tutorials/configuring-app-metadata-for-linked-roles)
         */
        val isLinkedRole: Boolean
    }

    companion object {
        /** Used to keep consistency between color values used in the API  */
        @JvmField
        val DEFAULT_COLOR_RAW: Int =
            0x1FFFFFFF // java.awt.Color fills the MSB with FF, we just use 1F to provide better consistency
    }
}

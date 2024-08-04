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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.detached.IDetachableEntity;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * Represents a {@link net.dv8tion.jda.api.entities.Guild Guild}'s Role. Used to control permissions for Members.
 *
 * @see Guild#getRoleCache()
 * @see Guild#getRoleById(long)
 * @see Guild#getRolesByName(String, boolean)
 * @see Guild#getRoles()
 *
 * @see JDA#getRoleCache()
 * @see JDA#getRoleById(long)
 * @see JDA#getRolesByName(String, boolean)
 * @see JDA#getRoles()
 */
public interface Role extends IMentionable, IPermissionHolder, IDetachableEntity, Comparable<Role>
{
    /** Used to keep consistency between color values used in the API */
    int DEFAULT_COLOR_RAW = 0x1FFFFFFF; // java.awt.Color fills the MSB with FF, we just use 1F to provide better consistency

    /**
     * The hierarchical position of this {@link net.dv8tion.jda.api.entities.Role Role}
     * in the {@link net.dv8tion.jda.api.entities.Guild Guild} hierarchy. (higher value means higher role).
     * <br>The {@link net.dv8tion.jda.api.entities.Guild#getPublicRole()}'s getPosition() always returns -1.
     *
     * @throws IllegalStateException
     *         If this role is not in the guild cache
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}, use {@link #getPermissionsRaw()} instead.
     *
     * @return The position of this {@link net.dv8tion.jda.api.entities.Role Role} as integer.
     */
    int getPosition();

    /**
     * The actual position of the {@link net.dv8tion.jda.api.entities.Role Role} as stored and given by Discord.
     * <br>Role positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more roles share the same position then they are sorted based on their creation date.
     * <br>The more recent a role was created, the lower it is in the hierarchy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * Role then this method will give you that value.
     *
     * @return The true, Discord stored, position of the {@link net.dv8tion.jda.api.entities.Role Role}.
     */
    int getPositionRaw();

    /**
     * The Name of this {@link net.dv8tion.jda.api.entities.Role Role}.
     *
     * @return Never-null String containing the name of this {@link net.dv8tion.jda.api.entities.Role Role}.
     */
    @Nonnull
    String getName();

    /**
     * Whether this {@link net.dv8tion.jda.api.entities.Role Role} is managed by an integration
     *
     * @return True, if this {@link net.dv8tion.jda.api.entities.Role Role} is managed.
     */
    boolean isManaged();

    /**
     * Whether this {@link net.dv8tion.jda.api.entities.Role Role} is hoisted
     * <br>Members in a hoisted role are displayed in their own grouping on the user-list
     *
     * @return True, if this {@link net.dv8tion.jda.api.entities.Role Role} is hoisted.
     */
    boolean isHoisted();

    /**
     * Whether or not this Role is mentionable
     *
     * @return True, if Role is mentionable.
     */
    boolean isMentionable();

    /**
     * The {@code long} representation of the literal permissions that this {@link net.dv8tion.jda.api.entities.Role Role} has.
     * <br><b>NOTE:</b> these do not necessarily represent the permissions this role will have in a {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel GuildChannel}.
     *
     * @return Never-negative long containing offset permissions of this role.
     */
    long getPermissionsRaw();

    /**
     * The color this {@link net.dv8tion.jda.api.entities.Role Role} is displayed in.
     *
     * @return Color value of Role-color
     *
     * @see    #getColorRaw()
     */
    @Nullable
    Color getColor();

    /**
     * The raw color RGB value used for this role
     * <br>Defaults to {@link #DEFAULT_COLOR_RAW} if this role has no set color
     *
     * @return The raw RGB color value or default
     */
    int getColorRaw();

    /**
     * Whether this role is the @everyone role for its {@link net.dv8tion.jda.api.entities.Guild Guild},
     * which is assigned to everyone who joins the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @return True, if and only if this {@link net.dv8tion.jda.api.entities.Role Role} is the public role
     * for the {@link net.dv8tion.jda.api.entities.Guild Guild} provided by {@link #getGuild()}.
     *
     * @see net.dv8tion.jda.api.entities.Guild#getPublicRole()
     */
    boolean isPublicRole();

    /**
     * Whether this Role can interact with the specified Role.
     * (move/manage/etc.)
     *
     * @param  role
     *         The not-null role to compare to
     *
     * @throws IllegalArgumentException
     *         if the provided Role is null or not from the same {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @return True, if this role can interact with the specified role
     */
    boolean canInteract(@Nonnull Role role);

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Guild Guild} this Role exists in
     *
     * @return the Guild containing this Role
     */
    @Nonnull
    Guild getGuild();

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.Role Role} in the specified {@link net.dv8tion.jda.api.entities.Guild Guild}
     * with the same settings as the given {@link net.dv8tion.jda.api.entities.Role Role}.
     * <br>The position of the specified Role does not matter in this case!
     * <br><b>If this {@link Role} has an {@link RoleIcon Icon} set, only its emoji can be copied over.</b>
     *
     * <p>It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} Permission
     * and all {@link net.dv8tion.jda.api.Permission Permissions} the given {@link net.dv8tion.jda.api.entities.Role Role} has.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The role could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_ROLES_PER_GUILD MAX_ROLES_PER_GUILD}
     *     <br>There are too many roles in this Guild</li>
     * </ul>
     *
     * @param  guild
     *         The {@link net.dv8tion.jda.api.entities.Role Role} that should be copied
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES} Permission and every Permission the provided Role has
     * @throws java.lang.IllegalArgumentException
     *         If the specified guild is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild} the role should be copied to.
     *
     * @return {@link RoleAction RoleAction}
     *         <br>RoleAction with already copied values from the specified {@link net.dv8tion.jda.api.entities.Role Role}
     */
    @Nonnull
    @CheckReturnValue
    RoleAction createCopy(@Nonnull Guild guild);

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.Role Role} in this {@link net.dv8tion.jda.api.entities.Guild Guild}
     * with the same settings as the given {@link net.dv8tion.jda.api.entities.Role Role}.
     * <br>The position of the specified Role does not matter in this case!
     * <br><b>If this {@link Role} has an {@link RoleIcon Icon} set, only its emoji can be copied over.</b>
     *
     * <p>It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} Permission
     * and all {@link net.dv8tion.jda.api.Permission Permissions} the given {@link net.dv8tion.jda.api.entities.Role Role} has.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The role could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_ROLES_PER_GUILD MAX_ROLES_PER_GUILD}
     *     <br>There are too many roles in this Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES} Permission and every Permission the provided Role has
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link RoleAction RoleAction}
     *         <br>RoleAction with already copied values from the specified {@link net.dv8tion.jda.api.entities.Role Role}
     */
    @Nonnull
    @CheckReturnValue
    default RoleAction createCopy()
    {
        return createCopy(getGuild());
    }

    /**
     * The {@link RoleManager RoleManager} for this Role.
     * In the RoleManager, you can modify all its values.
     * <br>You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the currently logged in account does not have the required position to modify this role
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return The RoleManager of this Role
     */
    @Nonnull
    RoleManager getManager();

    /**
     * Deletes this Role.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE}
     *     <br>If the the role was already deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES} in the channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If we don't have the permission to {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the role is too high in the role hierarchy to be deleted
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this Role
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * The tags of this role.
     * <br>This is useful to determine the purpose of a managed role.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#ROLE_TAGS CacheFlag.ROLE_TAGS}
     * to be enabled.
     * See {@link net.dv8tion.jda.api.JDABuilder#enableCache(CacheFlag, CacheFlag...) JDABuilder.enableCache(...)}.
     *
     * @return {@link RoleTags}
     *
     * @since  4.2.1
     */
    @Nonnull
    RoleTags getTags();

    /**
     * The {@link RoleIcon Icon} of this role or {@code null} if no custom image or emoji is set.
     * This icon will be displayed next to the role's name in the members tab and in chat.
     *
     * @return Possibly-null {@link RoleIcon Icon} of this role
     *
     * @since  4.3.1
     */
    @Nullable
    RoleIcon getIcon();

    /**
     * Tags associated with this role.
     *
     * @since  4.2.1
     */
    interface RoleTags
    {
        /**
         * Whether this role is associated with a bot.
         *
         * @return True, if this role is for a bot
         */
        boolean isBot();

        /**
         * The id for the bot associated with this role.
         *
         * @return The bot id, or 0 if this role is not for a bot
         *
         * @see    #isBot()
         */
        long getBotIdLong();

        /**
         * The id for the bot associated with this role.
         *
         * @return The bot id, or null if this role is not for a bot
         *
         * @see    #isBot()
         */
        @Nullable
        default String getBotId()
        {
            return isBot() ? Long.toUnsignedString(getBotIdLong()) : null;
        }

        /**
         * Whether this role is the boost role of this guild.
         *
         * @return True, if this role is the boost role
         */
        boolean isBoost();

        /**
         * Whether this role is managed by an integration.
         * <br>This is usually true for roles such as those created for twitch subscribers.
         *
         * @return True, if this role is managed by an integration
         */
        boolean isIntegration();

        /**
         * The id for the integration associated with this role.
         *
         * @return The integration id, or 0 if this role is not for an integration
         *
         * @see    #isIntegration()
         */
        long getIntegrationIdLong();

        /**
         * The id for the integration associated with this role.
         *
         * @return The integration id, or null if this role is not for an integration
         *
         * @see    #isIntegration()
         */
        @Nullable
        default String getIntegrationId()
        {
            return isIntegration() ? Long.toUnsignedString(getIntegrationIdLong()) : null;
        }

        /**
         * Whether this role can be acquired through a premium subscription purchase.
         * A role would also need {@link #isAvailableForPurchase()} to also be true for a user to actually be
         * able to purchase the role. 
         *
         * @return True, if this is a subscription role
         *
         * @see    #getSubscriptionIdLong()
         * @see    #isAvailableForPurchase()
         */
        default boolean hasSubscriptionListing()
        {
            return getSubscriptionIdLong() != 0;
        }

        /**
         * The subscription listing id for this role. If a role has a subscription id then it is a premium role that 
         * can be acquired by users via purchase.
         *
         * @return The listing id, or 0 if this role is not for a subscription listing
         *
         * @see    #isAvailableForPurchase()
         */
        long getSubscriptionIdLong();

        /**
         * The subscription listing id for this role. If a role has a subscription id then it is a premium role that 
         * can be acquired by users via purchase.
         *
         * @return The listing id, or null if this role is not for a subscription listing
         *
         * @see    #isAvailableForPurchase()
         */
        @Nullable
        default String getSubscriptionId()
        {
            return hasSubscriptionListing() ? Long.toUnsignedString(getSubscriptionIdLong()) : null;
        }

        /**
         * Whether this role has been published for user purchasing. Only {@link #hasSubscriptionListing() premium roles} 
         * can be purchased. However, a premium role must be published before it can be purchased. 
         * Additionally, a premium role can be unpublished after it has been published. Doing so will make it 
         * no longer available for purchase but will not remove the role from users who have already purchased it.
         *
         * @return True, if this role is purchasable
         *
         * @see    #hasSubscriptionListing()
         */
        boolean isAvailableForPurchase();

        /**
         * Whether this role is acquired through a user connection.
         * <br>Such as external services like twitter or reddit.
         * This also includes custom third-party applications, such as those managed by bots via {@link RoleConnectionMetadata}.
         *
         * @return True, if this role is acquired through a user connection
         *
         * @see    <a href="https://discord.com/developers/docs/tutorials/configuring-app-metadata-for-linked-roles" target="_blank">Configuring App Metadata for Linked Roles</a>
         */
        boolean isLinkedRole();
    }
}

/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel;
import net.dv8tion.jda.api.entities.Guild.NotificationLevel;
import net.dv8tion.jda.api.entities.Guild.Timeout;
import net.dv8tion.jda.api.managers.TemplateManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.entities.TemplateImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * Representation of a Discord Guild Template
 * <br>This class is immutable.
 *
 * @since  4.2.1
 *
 * @see    #resolve(JDA, String)
 *
 * @see    net.dv8tion.jda.api.entities.Guild#retrieveTemplates() Guild.retrieveTemplates()
 */
public interface Template
{
    /**
     * Retrieves a new {@link net.dv8tion.jda.api.entities.Template Template} instance for the given template code.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_GUILD_TEMPLATE Unknown Guild Template}
     *     <br>The template doesn't exist.</li>
     * </ul>
     *
     * @param  api
     *         The JDA instance
     * @param  code
     *         A valid template code
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Template Template}
     *         <br>The Template object
     */
    @Nonnull
    static RestAction<Template> resolve(@Nonnull final JDA api, @Nonnull final String code)
    {
        return TemplateImpl.resolve(api, code);
    }

    /**
     * Syncs this template.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the template's guild.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws IllegalStateException
     *         if the account is not in the template's guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the template's guild
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Template Template}
     *         <br>The synced Template object
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Template> sync();

    /**
     * Deletes this template.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the template's guild.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws IllegalStateException
     *         if the account is not in the template's guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the template's guild
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> delete();

    /**
     * The template code.
     *
     * @return The template code
     */
    @Nonnull
    String getCode();

    /**
     * The template name.
     *
     * @return The template name
     */
    @Nonnull
    String getName();

    /**
     * The template description.
     *
     * @return The template description
     */
    @Nullable
    String getDescription();

    /**
     * How often this template has been used.
     *
     * @return The uses of this template
     */
    int getUses();

    /**
     * The user who created this template.
     *
     * @return The user who created this template
     */
    @Nonnull
    User getCreator();

    /**
     * Returns creation date of this template.
     *
     * @return The creation date of this template
     */
    @Nonnull
    OffsetDateTime getTimeCreated();

    /**
     * Returns the last update date of this template.
     * If this template has never been updated, this returns the date of creation.
     *
     * @return The last update date of this template
     *
     * @see    #getTimeCreated()
     */
    @Nonnull
    OffsetDateTime getTimeUpdated();

    /**
     * A {@link net.dv8tion.jda.api.entities.Template.Guild Template.Guild} object
     * containing information about this template's origin guild.
     *
     * @return Information about this template's origin guild
     *
     * @see    net.dv8tion.jda.api.entities.Template.Guild
     */
    @Nonnull
    Guild getGuild();

    /**
     * Whether or not this template is synced.
     *
     * @return True, if this template matches the current guild structure
     */
    boolean isSynced();

    /**
     * Returns the {@link net.dv8tion.jda.api.managers.TemplateManager TemplateManager} for this Template.
     * <br>In the TemplateManager, you can modify the name or description of the template.
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws IllegalStateException
     *         if the account is not in the template's guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER}
     *
     * @return The TemplateManager of this Template
     */
    @Nonnull
    TemplateManager getManager();

    /**
     * The {@link net.dv8tion.jda.api.JDA JDA} instance used to create this Template instance.
     *
     * @return The corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * POJO for the guild information provided by a template.
     *
     * @see #getGuild()
     */
    interface Guild extends ISnowflake
    {
        /**
         * The name of this guild.
         *
         * @return The guild's name
         */
        @Nonnull
        String getName();

        /**
         * The description for this guild.
         * <br>This is displayed in the server browser below the guild name for verified guilds.
         *
         * @return The description
         */
        @Nullable
        String getDescription();

        /**
         * The Voice {@link net.dv8tion.jda.api.Region Region} that this Guild is using for audio connections.
         * <br>If the Region is not recognized, this returns {@link net.dv8tion.jda.api.Region#UNKNOWN UNKNOWN} but you
         * can still use the {@link #getRegionRaw()} to retrieve the raw name this region has.
         *
         * @return The the audio Region this Guild is using for audio connections. Can return Region.UNKNOWN.
         */
        @Nonnull
        default Region getRegion()
        {
            return Region.fromKey(getRegionRaw());
        }

        /**
         * The raw voice region name that this Guild is using for audio connections.
         * <br>This is resolved to an enum constant of {@link net.dv8tion.jda.api.Region Region} by {@link #getRegion()}!
         *
         * @return Raw region name
         */
        @Nonnull
        String getRegionRaw();

        /**
         * The icon id of this guild.
         *
         * @return The guild's icon id
         *
         * @see    #getIconUrl()
         */
        @Nullable
        String getIconId();

        /**
         * The icon url of this guild.
         *
         * @return The guild's icon url
         *
         * @see    #getIconId()
         */
        @Nullable
        String getIconUrl();

        /**
         * Returns the {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel} of this guild.
         *
         * @return the verification level of the guild
         */
        @Nonnull
        VerificationLevel getVerificationLevel();

        /**
         * Returns the {@link net.dv8tion.jda.api.entities.Guild.NotificationLevel NotificationLevel} of this guild.
         *
         * @return the notification level of the guild
         */
        @Nonnull
        NotificationLevel getDefaultNotificationLevel();

        /**
         * Returns the {@link net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel ExplicitContentLevel} of this guild.
         *
         * @return the explicit content level of the guild
         */
        @Nonnull
        ExplicitContentLevel getExplicitContentLevel();

        /**
         * The preferred locale for this guild.
         *
         * @return The preferred {@link Locale} for this guild
         */
        @Nonnull
        Locale getLocale();

        /**
         * Returns the {@link net.dv8tion.jda.api.entities.Guild.Timeout AFK Timeout} for this guild.
         *
         * @return the afk timeout for this guild
         */
        @Nonnull
        Timeout getAfkTimeout();

        /**
         * Gets all {@link net.dv8tion.jda.api.entities.Template.Role Roles} in this {@link net.dv8tion.jda.api.entities.Template.Guild Guild}.
         *
         * @return An immutable List of {@link net.dv8tion.jda.api.entities.Template.Role Roles}.
         */
        @Nonnull
        List<Role> getRoles();
    }

    /**
     * POJO for the roles information provided by a template.
     *
     * @see Guild#getRoles()
     */
    interface Role extends ISnowflake
    {
        /**
         * The Name of this {@link net.dv8tion.jda.api.entities.Template.Role Role}.
         *
         * @return Never-null String containing the name of this {@link net.dv8tion.jda.api.entities.Template.Role Role}.
         */
        @Nonnull
        String getName();

        /**
         * The color this {@link net.dv8tion.jda.api.entities.Template.Role Role} is displayed in.
         *
         * @return Color value of Role-color
         *
         * @see    #getColorRaw()
         */
        @Nullable
        Color getColor();

        /**
         * The raw color RGB value used for this role
         * <br>Defaults to {@link net.dv8tion.jda.api.entities.Role#DEFAULT_COLOR_RAW} if this role has no set color
         *
         * @return The raw RGB color value or default
         */
        int getColorRaw();

        /**
         * Whether this {@link net.dv8tion.jda.api.entities.Template.Role Role} is hoisted
         * <br>Members in a hoisted role are displayed in their own grouping on the user-list
         *
         * @return True, if this {@link net.dv8tion.jda.api.entities.Template.Role Role} is hoisted.
         */
        boolean isHoisted();

        /**
         * Whether or not this Role is mentionable
         *
         * @return True, if Role is mentionable.
         */
        boolean isMentionable();

        /**
         * The Guild-Wide Permissions this PermissionHolder holds.
         * <br><u>Changes to the returned set do not affect this entity directly.</u>
         *
         * @return An EnumSet of Permissions granted to this PermissionHolder.
         */
        @Nonnull
        EnumSet<Permission> getPermissions();

        /**
         * The {@code long} representation of the literal permissions that this {@link net.dv8tion.jda.api.entities.Template.Role Role} has.
         *
         * @return Never-negative long containing offset permissions of this role.
         */
        long getPermissionsRaw();
    }
}

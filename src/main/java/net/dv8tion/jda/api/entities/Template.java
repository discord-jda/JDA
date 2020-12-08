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
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.managers.TemplateManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.entities.TemplateImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Representation of a Discord Guild Template
 * This class is immutable.
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
     *
     * @return The the last update date of this template
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
         * The name of this guild.
         *
         * @return The guild's name
         */
        @Nonnull
        String getName();

        /**
         * Returns the {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel} of this guild.
         *
         * @return the verification level of the guild
         */
        @Nonnull
        VerificationLevel getVerificationLevel();
    }
}

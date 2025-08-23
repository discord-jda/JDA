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

package net.dv8tion.jda.api.entities.templates;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.TemplateManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.TemplateManagerImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Representation of a Discord Guild Template
 * <br>This class is immutable.
 *
 * @since  4.3.0
 *
 * @see    #resolve(JDA, String)
 * @see    net.dv8tion.jda.api.entities.Guild#retrieveTemplates() Guild.retrieveTemplates()
 */
public class Template
{
    private final JDAImpl api;
    private final String code;
    private final String name;
    private final String description;
    private final int uses;
    private final User creator;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final TemplateGuild guild;
    private final boolean synced;

    public Template(final JDAImpl api, final String code, final String name, final String description,
                    final int uses, final User creator, final OffsetDateTime createdAt, final OffsetDateTime updatedAt,
                    final TemplateGuild guild, final boolean synced)
    {
        this.api = api;
        this.code = code;
        this.name = name;
        this.description = description;
        this.uses = uses;
        this.creator = creator;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.guild = guild;
        this.synced = synced;
    }

    /**
     * Retrieves a new {@link net.dv8tion.jda.api.entities.templates.Template Template} instance for the given template code.
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
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided code is null or empty</li>
     *             <li>If the provided code contains a whitespace</li>
     *             <li>If the provided JDA object is null</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.templates.Template Template}
     *         <br>The Template object
     */
    @Nonnull
    @CheckReturnValue
    public static RestAction<Template> resolve(@Nonnull final JDA api, @Nonnull final String code)
    {
        Checks.notEmpty(code, "code");
        Checks.noWhitespace(code, "code");
        Checks.notNull(api, "api");

        Route.CompiledRoute route = Route.Templates.GET_TEMPLATE.compile(code);

        JDAImpl jda = (JDAImpl) api;
        return new RestActionImpl<>(api, route, (response, request) ->
                jda.getEntityBuilder().createTemplate(response.getObject()));
    }

    /**
     * Syncs this template.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the template's guild.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws IllegalStateException
     *         If the account is not in the template's guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the template's guild
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.templates.Template Template}
     *         <br>The synced Template object
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Template> sync()
    {
        checkInteraction();
        final Route.CompiledRoute route = Route.Templates.SYNC_TEMPLATE.compile(guild.getId(), this.code);
        return new RestActionImpl<>(api, route, (response, request) ->
                api.getEntityBuilder().createTemplate(response.getObject()));
    }

    /**
     * Deletes this template.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the template's guild.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws IllegalStateException
     *         If the account is not in the template's guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the template's guild
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Void> delete()
    {
        checkInteraction();
        final Route.CompiledRoute route = Route.Templates.DELETE_TEMPLATE.compile(guild.getId(), this.code);
        return new RestActionImpl<>(api, route);
    }

    /**
     * The template code.
     *
     * @return The template code
     */
    @Nonnull
    public String getCode()
    {
        return this.code;
    }

    /**
     * The template name.
     *
     * @return The template name
     */
    @Nonnull
    public String getName()
    {
        return this.name;
    }

    /**
     * The template description.
     *
     * @return The template description
     */
    @Nullable
    public String getDescription()
    {
        return this.description;
    }

    /**
     * How often this template has been used.
     *
     * @return The uses of this template
     */
    public int getUses()
    {
        return this.uses;
    }

    /**
     * The user who created this template.
     *
     * @return The user who created this template
     */
    @Nonnull
    public User getCreator()
    {
        return this.creator;
    }

    /**
     * Returns creation date of this template.
     *
     * @return The creation date of this template
     */
    @Nonnull
    public OffsetDateTime getTimeCreated()
    {
        return this.createdAt;
    }

    /**
     * Returns the last update date of this template.
     * <br>If this template has never been updated, this returns the date of creation.
     *
     * @return The last update date of this template
     *
     * @see    #getTimeCreated()
     */
    @Nonnull
    public OffsetDateTime getTimeUpdated()
    {
        return this.updatedAt;
    }

    /**
     * A {@link TemplateGuild Template.Guild} object
     * containing information about this template's origin guild.
     *
     * @return Information about this template's origin guild
     *
     * @see    TemplateGuild
     */
    @Nonnull
    public TemplateGuild getGuild()
    {
        return this.guild;
    }

    /**
     * Whether or not this template is synced.
     *
     * @return True, if this template matches the current guild structure
     */
    public boolean isSynced()
    {
        return this.synced;
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.managers.TemplateManager TemplateManager} for this Template.
     * <br>In the TemplateManager, you can modify the name or description of the template.
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws IllegalStateException
     *         If the account is not in the template's guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER}
     *
     * @return The TemplateManager of this Template
     */
    @Nonnull
    @CheckReturnValue
    public TemplateManager getManager()
    {
        checkInteraction();
        return new TemplateManagerImpl(this);
    }

    private void checkInteraction()
    {
        final net.dv8tion.jda.api.entities.Guild guild = this.api.getGuildById(this.guild.getIdLong());

        if (guild == null)
            throw new IllegalStateException("Cannot interact with a template without shared guild");
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(guild, Permission.MANAGE_SERVER);
    }

    /**
     * The {@link net.dv8tion.jda.api.JDA JDA} instance used to create this Template instance.
     *
     * @return The corresponding JDA instance
     */
    @Nonnull
    public JDA getJDA()
    {
        return this.api;
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof Template))
            return false;
        Template impl = (Template) obj;
        return impl.code.equals(this.code);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("code", code)
                .toString();
    }
}
